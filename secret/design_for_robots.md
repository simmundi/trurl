## Innovative ECS feature: entity system with a Chain-of-command variant. Implementation notes.

This feature is to be added to the Trurl ECS library.

The aim is to provide a unified way of composing pieces of logic that operate on entities, in a way similar to how servlet filter operate on servlet requests and responses.

the pieces of logic can be implemented in many different ways.

The basic ways assume that the chain will always continue with the next segment:
```java

(Entity e) -> { ... }
(Session s, int entityId) -> { ... }

```
Any such piece can be wrapped into an "entity processor":

```java
EntityProcessor ep = EntityProcessor.from((e) -> { });

```

The wrapped version can be run by either:

```java
ep.run(e);
// or:
ep.run(s, entityId);
```
Processors are composites, a chain can be built that executes multiple chains:

```java

EntityProcessor chain = EntityProcessor.chain(
    EntityProcessor.from((e) -> { }),
    EntityProcessor.from((s, id) -> { })
);
```

In the future, there might be a feature request for guards and suchlike, for now - we don't care about those.

Each operation can be defined to explicitly access the rest of the chain:

```java

EntityProcessor.from((Entity e, EntityProcessor next) -> { 
        // do something with the entity
        // ...
        next.run(e);
});

```

This allows for both filter-like scenarios, short-circuiting and - architecturally most importantly - fanning out to multiple entities:

```java

EntityProcessor.from((Entity e, EntityProcessor next) -> {
    Room room = e.getComponent(Room.class);
    if (room != null){
        for(Entity child : room.getContainedEntities()){
            next.run(child);
        }
   }    
});

```

The above should also be possible using the other call (with parameters: Session session, int entityId, EntityProcessor next).

Note that the difference between the calls is purely for optimization, because you can always:

```java
Entity e = session.getEntityById(entityId);

// or:

Session session = entity.getSession();

```

## Low-level "C-style" Java optimization and architecture

To ensure the `EntityProcessor` can run in the most demanding tight loops without heap pressure, we follow a "C-style" Java philosophy.

### Core Principles:
- **Zero Spurious Allocations**: No objects should be allocated during the execution of a processor chain.
- **No Capturing Closures**: Avoid functional idioms (lambdas) that capture local variables, as they trigger per-call heap allocations.
- **Static Dispatch**: Prefer virtual method calls on pre-allocated object structures over dynamic functional compositions.
- **Dual-Path Execution (Piggybacking)**: Allow both entity-level and identifier-level processing without redundant lookups or unnecessary hydration.

### Applicative vs. Monadic processing
The architecture distinguishes between two types of logic flow:
- **Applicative (Static)**: The "map" of the computation is fixed at construction time. Only the *cardinality* (0-N) of execution is dynamic. This is the goal for high-performance loops.
- **Monadic (Dynamic)**: The next step is determined by the result of the current step. This requires "capturing context" and usually involves spurious allocations (the "Monad tax"). We explicitly avoid this for hot paths.

### Chain Implementation: Pre-allocated Links and Flattening
To keep recursion without closures, the `EntityProcessor.chain()` must not use nested lambdas. Instead:
- **Links**: Represent the chain as a linked list of pre-allocated `Link` objects (effectively a linked list of function pointers).
- **Flattening**: The `chain()` factory must "flatten" any nested chains during construction. If `chain(chainA, chainB)` is called, it should produce a single flat sequence of links rather than a nested structure.
- **Top-level vs. Internal calls**: There is a distinction between the "Top-level" entry point and the "Internal" recursive calls. In a low-level implementation, the top-level call initializes the execution, while internal links ignore the `next` parameter of the interface, relying instead on their pre-baked `nextLink` field.

### Dual-Path Execution: Entity Piggybacking

To avoid the trade-off between "useless hydration" (passing entities everywhere) and "redundant lookups" (passing only IDs), we use a dual-path architecture.

#### The Problem:
- **If we only pass IDs**: Every processor that needs an `Entity` object must call `session.getEntity(id)`, which triggers a `HashMap` lookup even if the entity was already hydrated by a previous processor in the chain.
- **If we only pass Entities**: We force hydration even for "cold" processors (like Bloom filters) that only need the ID.

#### The Solution (Piggybacking):
Internal calls pass both the `id` AND an optional `entity` reference (which may be `null`).
1.  **First link needs ID**: It uses the ID directly. It passes `null` as the entity to the next link.
2.  **Second link needs Entity**: It checks if the passed entity is `null`. If so, it calls `session.getEntity(id)` and stores the result. When it calls the next link, it passes this newly fetched entity.
3.  **Third link needs Entity**: It sees the entity is already non-null and uses it directly, skipping the `HashMap` lookup.

This allows the entity reference to "piggyback" on the call stack once it has been fetched.

### Lifecycle Management: Reification and Hooks

To support stateful operations (like rendering buffers or thread-local scratchpads) while maintaining zero allocations in the hot path, the architecture includes a unified reification and lifecycle model.

#### 1. Reification (Per-Thread/Per-Execution State)
A processor chain can be **reified** before execution. This allows stateful links to provision their own private resources (e.g., a `Vector2D` or a vertex buffer) once per execution or per worker thread.

- **Immutable Links**: Return themselves during reification (shared across threads).
- **Stateful Links**: Return a new instance with a fresh context object (private to the thread).
- **Benefits**: Scratchpad objects are stored in link fields rather than allocated per entity, and they are passed implicitly without changing the stack signature.

#### 2. Lifecycle Hooks: `onBegin` and `onEnd`
Every `EntityProcessor` and `InternalLink` supports two lifecycle hooks that define the boundaries of a pass:

- **`onBegin(Session)`**: Called once before any entities are processed. Used for setup, e.g., capturing a timestamp from a global timer or calling `batch.begin()`.
- **`onEnd(Session)`**: Called once after the source is exhausted. Used for final batches (draining) and resource cleanup, e.g., calling `batch.end()`.

#### 3. Unified State and Lifecycle
Stateful links created via `EntityProcessor.from(StatefulProcessor<C>, Supplier<C>)` can implement `onBegin` and `onEnd` to operate on their provisioned context `C`. The `EntityExecutor` ensures these hooks are called on the **reified instance** of the processor, making it safe for parallel execution.

### The "Arrow" Approach (Future Feature)
To allow "limited choice" without falling into the Monadic trap, we can implement an **Arrow** pattern. This allows branching between a pre-defined set of options:
- **Branching Processors**: Instead of one `next`, a processor might have `goLeft` and `goRight` pre-allocated paths.
- **API Suggestion**: The `EntityProcessor` could be extended to support multiple next segments, or a specific `BranchingProcessor` class can be used to hard-wire conditional logic into the static graph.

### Structural Specification: TopLevelChain vs. InternalLink

To achieve the goal of zero-allocation hot paths, we distinguish between the public interface and the internal execution engine.

1.  **TopLevelChain**: The entry point.
    *   Exposes the public `run(Session, int)` and `run(Entity)` methods.
    *   Exposes `rawRun(Session, int, Entity)` for optimized entry.
    *   Holds the first `InternalLink` of the pre-baked execution graph.

2.  **InternalLink**: The recursive heart.
    *   Uses a specialized method: `void runInternal(Session session, int entityId, Entity entity)`.
    *   The `entity` parameter is optional (piggybacking).
    *   Does NOT take a `next` parameter; instead, it holds its own `nextLink` as a final field.
    *   By removing the dynamic `next`, we eliminate the need for closure capturing and the "Monad tax."

### Implementation Detail: Flattening and Baking

The `chain(EntityProcessor... processors)` factory must perform a static "baking" process:
- **Flattening**: If any of the `processors` are themselves chains, their internal links must be extracted and appended sequentially. The resulting graph should be a simple linear sequence of `InternalLink` objects.
- **Tail Connection**: The last `InternalLink` in a chain points to a static `FINAL_LINK` (a no-op that just returns).

### The "Arrow" Branching (Static Choice)

To implement branching (choice) without allocations:
- **BranchingLink**: An `InternalLink` that holds two fields: `ifTrueLink` and `ifFalseLink`.
- **Execution**: It performs its low-level check and calls the `runInternal` method of the appropriate pre-baked path. After the branch finishes, it continues with its own `nextLink`.
- **Why this works**: All possible paths are known and pre-allocated at construction time. No new objects are created when a branch is taken.

### Session as a Unit of Work
The `Session` acts as a Level 2 cache or a Unit of Work. When `session.getEntity(id)` is called:
- If the entity is already hydrated in the session, it is returned immediately.
- If not, the session fetches and hydrates it from the underlying cold store.
This mechanism is central to the ECS's data management, and while there is a cost to entity retrieval, it is the intended and necessary way to operate.

### Performance Constraints for Implementers

- **Hot Path (InternalLink)**: Must strictly use `(Session session, int entityId, Entity entity)`.
- **Entity Access**: Always check if the `entity` parameter is non-null before calling `session.getEntity(entityId)`.
- **Propagation**: Always pass the most "complete" context to `next.runInternal`. If you have the `Entity` object, pass it so the next link can benefit from piggybacking.
- **Memory**: All links must be immutable once "baked" into a chain, allowing for the most aggressive JIT inlining and optimization.

### Additional notes





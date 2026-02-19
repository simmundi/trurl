# Vertical Entity Processing in Trurl: Beyond the Loop

When you think about an Entity Component System (ECS), the first thing that usually comes to mind is the **Horizontal Loop**. You have System A, which iterates through all entities with a `Position` component. Then you have System B, which iterates through all entities with a `Velocity` component. It's simple, predictable, and—in many cases—completely suboptimal for modern CPUs.

In Trurl, we’ve taken a different path. We call it **Vertical Entity Processing**. This document explains why we made this choice, how the new execution engine works, and how you can use it to build high-performance simulations that respect the "C-style" Java philosophy of zero spurious allocations and cache locality.

### The Problem with Horizontal Loops

Traditional ECS architectures often look like this:
```
System A: Loop All (1M entities) -> Update Positions
System B: Loop All (1M entities) -> Check Collisions
System C: Loop All (1M entities) -> Render
```
Each system completes its entire pass before the next one starts. The problem? **Cache thrashing.** By the time System B starts processing Entity #1, its position data has likely been evicted from the L1/L2 cache by the rest of System A’s million-entity loop. Your CPU spends more time waiting for main memory than actually calculating physics.

### Our Solution: Vertical Composition

Trurl's execution engine flips this on its head. Instead of multiple horizontal passes, we aim for a single vertical journey for each entity:
```
Loop All:
  Entity #1 -> System A -> System B -> System C
  Entity #2 -> System A -> System B -> System C
  ...
```
By processing an entity through the entire logic chain at once, we keep its components "hot" in the cache. This **Vertical Composition** minimizes memory traffic and significantly improves performance in data-heavy simulations.

---

### 1. The Core: The `EntityProcessor`

The heart of this architecture is the `EntityProcessor`. It’s not just a function; it’s a **pre-baked execution graph**.

#### Basic Usage
Building a processor is as simple as providing a lambda:
```java
EntityProcessor update = EntityProcessor.from((Entity e) -> {
    Position pos = e.get(Position.class);
    Velocity vel = e.get(Velocity.class);
    pos.x += vel.dx;
});
```

You can then compose these processors into a single, flat execution chain:
```java
EntityProcessor fullChain = EntityProcessor.chain(
    update,
    checkBoundaries,
    render
);
```

#### Performance Optimization: Piggybacking
One of the most expensive operations in an ECS is the "Identity Map" lookup—finding the `Entity` object for a given ID. If every processor in a chain calls `session.getEntity(id)`, you pay a hashmap tax over and over.

Trurl solves this with **Piggybacking**. Internal calls pass both the `id` and an optional `Entity` reference. 
1. The first processor that needs an `Entity` fetches it from the `Session`.
2. It then "piggybacks" that reference to the next processor in the chain.
3. Subsequent processors see the non-null entity and use it directly, skipping the lookup.

This optimization happens automatically, but it requires a strict design: all processors must operate within the **Identifier (ID) Domain**.

---

### 2. Feeding the Machine: Sources and Executors

To run an `EntityProcessor`, you need two more components: a **Source** and an **Executor**.

*   **Source**: A low-level provider of entity IDs. For example, a `RootSource` iterates over every entity in the store. 
*   **Executor**: The orchestrator. It pulls IDs from the source, provides a `Session` (the Unit of Work), and ensures a final `flush()` happens when the job is done.

```java
EntityExecutor executor = new EntityExecutor(engine);
Source allEntities = new RootSource(engine.getRootStore());

executor.execute(allEntities, fullChain);
```

---

### 3. Advanced Logic: Fanning Out and Branching

The real power of the vertical model comes when you need to navigate entity relationships.

#### Fanning Out (Children and Collections)
Because the `EntityProcessor` is composable, you can easily "fan out" from a parent to its children within the same chain:
```java
EntityProcessor roomProcessor = EntityProcessor.from((Entity room, EntityProcessor.RawEntityProcessor next) -> {
    Children children = room.get(Children.class);
    for (int childId : children.ids()) {
        // Fan out: immediately process the child through the REST of the chain
        next.run(room.getSession(), childId, null);
    }
});
```

#### Branching (The "Arrow" Pattern)
Branching is handled via a static choice that avoids allocations:
```java
EntityProcessor branch = EntityProcessor.branch(
    (session, id) -> session.getEntity(id).has(Active.class),
    processActive,
    processInactive
);
```

---

### 4. Handling State: Reification, Draining, and Lifecycle Hooks

Modern simulations often require stateful resources, like a GPU vertex buffer or a temporary `Vector2D` for physics calculations. Creating these objects for every entity is a performance killer.

#### Reification (Thread-Local Scratchpads)
You can "reify" a processor chain before execution. This allows stateful links to provision their own private resources once per thread or per execution.
```java
EntityProcessor stateful = EntityProcessor.from(
    (session, id, entity, rect, next) -> {
        // Use 'rect' as a zero-allocation scratchpad!
        rect.set(0, 0, 10, 10);
        next.runInternal(session, id, entity);
    },
    () -> new Rectangle() // Provisioned once per reification
);
```

#### Lifecycle Hooks (`onBegin` and `onEnd`)
Processors can implement `onBegin()` and `onEnd()` to perform setup or summary tasks once per execution (or per thread in a parallel context). This is ideal for updating time-based state or opening/closing batch resources.
```java
@Override
public void onBegin(Session session) {
    this.timePassed = globalTimer.getTotalTimePassed();
}
```

#### Final Batches (Draining)
If your processor buffers data (like a renderer waiting to fill a vertex batch), you need to handle the "rest" after the main loop finishes. This is simply handled in the `onEnd()` hook, which is called after the source is exhausted.
```java
@Override
public void onEnd(Session session) {
    renderer.render(); // Flush any remaining data
    renderer.clear();
}
```

---

### Design Goals & Justifications

*   **Zero Spurious Allocations**: Once the chain is "baked" and execution starts, the hot path allocates nothing on the heap. All context is passed via stack parameters or stored in reified link fields.
*   **Strict Identity Domain**: By forcing the engine to think in terms of `RootStore` Identifiers, we ensure that a `Session` never suffers from identity collisions when crossing substore boundaries.
*   **Flattening**: When you call `EntityProcessor.chain()`, the engine recursively flattens the graph into a linear sequence of links. This keeps the call stack shallow and helps the JIT compiler inline the logic effectively.

### Summary

Trurl's entity processing engine is designed for the most demanding simulations. By moving from horizontal loops to vertical composition, and by embracing a "C-style" Java philosophy, it allows you to write high-level, composable logic that runs with the efficiency of a low-level engine. 

Whether you are fanning out from a chunk to its entities, or reifying thread-local buffers for parallel rendering, the `EntityProcessor` provides a unified, zero-allocation foundation for your ECS logic.

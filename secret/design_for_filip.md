## Execution Engine Design: Composable Vertical Processing

This document outlines the architecture for the next generation of the Trurl ECS execution engine, following a "Strict Identity" philosophy.

### 1. The Core Philosophy: Vertical Composition
Traditional ECS systems use **Horizontal Loops**:
`System A (Loop All) -> System B (Loop All) -> System C (Loop All)`

Trurl aims for **Vertical Composition**:
`Loop All (Entity X -> System A -> System B -> System C)`

#### Benefits:
- **Cache Locality**: Entity data (components) stays in L1/L2 cache while the entire processing chain executes.
- **Reduced Traffic**: Minimizes main memory bandwidth usage.
- **Unified Logic**: Allows seamless nesting (e.g., iterating over child entities within a parent's logic) without breaking the loop structure.

---

### 2. Architectural Components

#### A. The `EntityProcessor` (The Logic)
A "baked" zero-allocation execution graph.
- **Strict Identity Domain**: It *always* operates within the context of **Identifiers (IDs)**. An ID is a row index specifically in the `RootStore`.
- **Dual-Path Support**: While the domain is strictly Identifiers, the implementation supports both `(Session, id)` and `(Entity)` calls to optimize lookups (piggybacking).
- **Recursion**: A processor can fan-out by plucking new Identifiers (e.g., from a `Reference` or a `Join` with backreferences) and calling `next.run(session, childId)` or `next.run(childEntity)`.

#### B. The `Source` (The Data Feed)
A `Source` is a low-level provider of Entity Identifiers. To align with the "C-style" philosophy, it avoids high-level abstractions like `Stream`.
- **RootSource**: Iterates through all valid IDs in the `RootStore`.
- **BackreferenceSource**: Iterates through a **Substore** and resolves each row to its owner's Identifier via a backreference. This acts as an "Implicit Index" (Archetype-like processing).

#### C. The `Executor` (The Orchestrator)
The `Executor` connects a `Source` to an `EntityProcessor`.
- **Session Management**: It provides a `Session` to the chain.
- **Lifecycle**: It ensures a clean boundary for setup and cleanup via `reified.onBegin(session)` and `reified.onEnd(session)`.
- **Optimization**: It is free to perform intermediary flushes or clears to manage memory pressure.

---

### 3. Execution Flow and Identity

By restricting `EntityProcessor` to the Identity domain, the system maintains total safety:
1.  **Entry**: The `Executor` plucks an Identifier from the `Source`.
2.  **Processing**: The `EntityProcessor` chain executes logic for that Identifier.
3.  **Fan-out**: If the logic finds a child entity (e.g., `parent.get(Children.class)`), it obtains that child's **Identifier** and continues the chain.
4.  **Consistency**: Since only Identifiers (and their corresponding Entities) are passed, `session.getEntity(id)` always refers to the correct global entity.

### 5. Advanced Pattern: Chunk-as-Entity

While the engine has moved away from hard-coded "Chunks" and "Horizontal Loops," some problems (like spatial partitioning or complex parallel dependencies) still benefit from a chunked structure.

Instead of introducing a new abstraction, Trurl treats **Chunks as Entities**:
1.  **Reification**: A chunk is simply an entity with a component (e.g., `ChunkComponent`) that stores a list or range of IDs for the "real" entities it contains.
2.  **Processing**: To process entities chunk-by-chunk, you use an `EntityProcessor` that operates on the Chunk entities.
3.  **Fan-out**: Inside the Chunk processor, you iterate over the contained IDs and call `next.run(session, childId)` to process each entity.

#### Benefits:
- **Unified Infrastructure**: Chunks can be saved, loaded, and queried using the same Daos and Stores as any other entity.
- **Parallelism**: A "Parallel Processor" can be applied to the Chunk entities, naturally distributing work while keeping entities within a chunk on the same thread for better cache locality.
- **Flexibility**: Chunks can have their own properties (e.g., bounding boxes, aggregate statistics) without special-casing the engine.

1.  **Source Pattern**:
    To avoid `Stream` overhead, a `Source` should provide a low-level iteration mechanism (e.g., a `forEach` with a primitive consumer or a simple `while/next` pattern).

2.  **Processor Chain**:
    Logic propagates the Identifier domain using `next.run(session, id)`, `next.run(entity)`, or `next.rawRun(session, id, entity)`.

3.  **Executor**:
    A simple orchestrator that bridges the `Source` and `Processor`. It deals only with the sequence of Identifiers and the final flush.

4.  **No Legacy Indexes/Chunks**:
    The legacy `Index`, `Chunk`, and `EngineIndexes` abstractions have been removed. All data streaming is now handled by the `Source` interface, which is more robust and fits the vertical processing model.

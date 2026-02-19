# Navigating model interfaces and executing logic

The high-level ECS API in Trurl is centered around four main abstractions: `Engine`, `Session`, `Entity`, and `EntityProcessor`.

## Engine and Session

The `Engine` is the root of your simulation. It manages the `RootStore` (where all data is kept) and the `DaoManager`.

To interact with entities, you need a `Session`. A `Session` is a Unit of Work and an Identity Map. It keeps track of hydrated component objects to avoid redundant loads from the store and to ensure that for any entity identifier, there is only one `Entity` object in memory.

```java
Engine engine = // ... usually acquired via DI
Session session = engine.getSession();
```

## Entity

An `Entity` is a lightweight handle that combines a `Session` and an ID. It provides an object-oriented way to access component data.

```java
Entity person = session.getEntity(123);
Person data = person.get(Person.class);
data.setAge(45);
```

When you are done with a session, you should `flush()` it to save all changed components back to the store.

## Entity Processing

Trurl uses a **Vertical Processing** model for executing simulation logic. Instead of iterating through all entities one system at a time (Horizontal), we compose multiple processors into a single chain and process each entity through the entire chain at once.

### EntityProcessor

An `EntityProcessor` represents a piece of logic that operates on a single entity. You can create them from lambdas or by implementing the interface:

```java
EntityProcessor physics = EntityProcessor.from((Entity e) -> {
    Position p = e.get(Position.class);
    Velocity v = e.get(Velocity.class);
    p.x += v.dx;
});
```

### Composition and Execution

Processors can be composed into a flat, efficient execution chain:

```java
EntityProcessor logic = EntityProcessor.chain(physics, rendering);

EntityExecutor executor = new EntityExecutor(engine);
Source all = new RootSource(engine.getRootStore());

executor.execute(all, logic);
```

### Lifecycle and Reification

For more complex scenarios, such as stateful rendering or using thread-local scratchpads, Trurl supports **Reification**. This allows the engine to provision resources (like a `Rectangle` or a `SpriteBatch`) once per execution/thread instead of once per entity.

Processors can also hook into the execution lifecycle using `onBegin(Session)` and `onEnd(Session)`.

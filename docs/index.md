# Trurl - Scalable simulations (and games) for Java developers

## Is it cool?

Yes.

## Will it work for my needs?

Trurl is a library for writing simulations and games in Java. Is it for you? Could be, if you are a Java developer and:

- you are writing a scientific simulation, model or a game (no matter whether it's a Metroidvania, a roguelike or a bullet hell) and want some structure;
- you need a dataframe implementation;
- you need a Entity-Component-System (ECS) framework;

### Do I need an Entity-Component-System (ECS) framework?

If you are writing a simulation or a game, that could benefit from clear architecture, code reuse and scalability - then probably yes.

Some foundational texts about the ECS philosophy are:

- [Entity Systems are the future of MMOG development](https://t-machine.org/index.php/2007/09/03/entity-systems-are-the-future-of-mmog-development-part-1/)

- [Data Oriented Design (Or why you might be shooting yourself in the foot)](https://gamesfromwithin.com/data-oriented-design)

Trurl is a state-of-the-art ECS implementation, sharing many features with other Java ECS frameworks, such as Ashley, Artemis-DB, Dominion, Entitas,... .

In an ECS - every modifiable object is represented by an `Entity`. In a Covid19 model, entities could represent people, places, hospitals, etc. In a bullet hell - bullets, enemies, players, etc. In a roguelike - monsters, items, walls, doors, etc.

Entities by themselves are just generic containers with an identifier. The data is stored in `Component`s. Components are simple DTO-style classes, containing cohesive bits of information - they are strongly typed and can be reused across simulations / games. By using different sets of components (much like mix-ins) - entities can represent different things, without falling into the many traps of OO inheritance.

In Trurl, a component is represented by a simple Java class, like:

```java
// component for Entities that have a position in 2D space
class Position {
    float x;
    float y;
    // getters, setters
}
```

Or:

```java
// component for Entities that represent people
class Person {
    int age;
    Sex sex; // an enum
    // getters, setters
}
```

Or:

```java
// component for Entities that can be displayed as a sprite
class SpriteRepresentation {
    String imagePath;
    // getters, setters    
}
```

Or:

```java
// Component for Entities that have state
class Stateful {
    enum State {
        IDLE, ENGAGED
    }
    State state;
    // getters, setters
}
```

Simulation logic in ECS frameworks is expressed in terms of _Systems_. In a slight departure from the naming tradition - Trurl represents them by instances of `Step`. A step is a piece of code that reads or changes the state of any number of entities, usually iterating through their subset. Steps depend on components, and can be packaged alongside them to provide cross-cutting functionalities.

We could imagine a step that iterates through all entities that have both a `SpriteRepresentation` and a `Position` - and displays them on the screen (and indeed, such steps and components are already available off-the-shelf in Trurl, as add-ons).

Or a step that implements random walk for any stateful entity which is `IDLE` and is applicable to any component that has a `Position` and `Stateful`.

Or we could imagine a step that verifies if two entities with a `Person` and `Position` component being close enough - simulate a chat, based on their age and sex.

Thanks to the steps being generic and composable - we could create a composit step, running the three steps above in sequence. After seeding the simulation with some people (i.e., entities with `Position`, `Representation` and `Person` components), we could see them walking around, stopping and chatting.

From the software engineering point of view, the crucial aspect of the above is that the `Steps` don't need to depend on each other, but only on the fact that entities contain certain components.

### Do I need a dataframe implementation?

Trurl components are not persisted in memory as heap objects - instead, they are stored in a dataframe-like structure, similar to the one used by Pandas, R, Spark, etc.

The dataframe allows for easy mapping between components and tabular files (CSV, ORC, VisNow) or even more complex data stores, like SQL databases or TMX files - this means that the results of a simulation (and even snapshots of their state) can be easily exported and inspected using tools like Jupyter Notebook, VisNow, QGIS, Tiled etc.

Also, the underlying dataframe is a first-class citizen in Trurl. It can be accessed directly in a number of ways, allowing bare-metal optimizations, like using the Vector API.

In fact, the expected workflow is to start building simulations using mostly the object oriented API, and - if the need of optimization arises and in the scope established using a profiler - optimize parts of the functionality to use dataframe-level constructs.

### Will I feel at home with Trurl?

The dataframe aspect and the ECS aspect of Trurl are integrated via abstractions evolved from the classical Patterns Of Enterprise Architecture, as described by Martin Fowler. Via the use of Identity Map (akin to a Hibernate Session) ECS entities can be used in a way similar to JPA entities.

In practice this means that in large simulations most of the data resides in the dataframe, but the part that is currently processed is loaded into memory as a set of objects, which can be used in a natural, Object Oriented way. This allows for balancing between performance and interpretability of the code. Also, there's no danger of paining oneself into a corner - any code using OO access can be easily rewritten to use the dataframe directly, with minimum changes - and still benefiting from static typing.

## What do I need to know to use Trurl?

We don't know, it would be a leap of faith on your side - and we assume no responsibility for it.

You can try browsing through the docs (very much in flux):

- [Store API](./store.md) describes how to use the dataframe aspect of Trurl, read and write data.
- [Dependency Injection and configuration](./di.md) describes how to use Bento, Trurl's DI container.
- [Engine API](./ecs.md) describes how to use the ECS aspect of Trurl, read and write data.

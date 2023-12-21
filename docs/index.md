# Trurl - Scalable simulations (and games) for Java developers

## Is it cool?

Yes. Try the examples.

## Will it work for my needs?

Trurl is a library for writing simulations and games in Java. Is it for you? Could be, if you are a Java developer and:

- you are writing a scientific simulation, model or a game (no matter whether it's a Metroidvania, a roguelike or a bullet hell) and want some structure;
- you need a dataframe implementation;
- you need a Entity-Component-System (ECS) framework;

### Need an Entity-Component-System (ECS) frameworks?

If you are writing a simulation or a game, that could benefit from clear architecture, code reuse and scalability - then probably yes.

Some foundational texts about the ECS philosophy are:

- [Entity Systems are the future of MMOG development](https://t-machine.org/index.php/2007/09/03/entity-systems-are-the-future-of-mmog-development-part-1/)

- [Data Oriented Design (Or why you might be shooting yourself in the foot)](https://gamesfromwithin.com/data-oriented-design)

Trurl is a state-of-the-art ECS implementation, sharing many features with other popular ECS frameworks, such as Ashley, Artemis-DB, Dominion, Entitas,... .

In a Trurl program, every model object that can potentially change is an `Entity`. They are little more than wrappers around a `long` value, serving as the identifier.

In a Covid19 model, entities could represent people, places, hospitals, etc. In a bullet hell - bullets, enemies, players, etc. In a roguelike - monsters, items, walls, doors, etc.

Entities by themselves are just generic containers with an identifier - the data is stored in `Component`s. Components are simple DTO-style classes, containing cohesive bits of information - they are strongly typed and can be reused across simulations / games. By using different sets of components (much like mix-ins) - entities can represent different things, without falling into the many traps of OO inheritance.

In Trurl, a component is a simple Java class, like:

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

Simulation logic is expressed in terms of `System`s. A system is a piece of code that reads and modifies components, usually iterating through entities. Systems depend on components, and can be packaged alongside them to provide cross-cutting functionalities.

We could imagine a system that iterates through all entities that have both a `SpriteRepresentation` and a `Position` - and displays them on the screen (and indeed, such systems and components are already available off-the-shelf in Trurl, as add-ons).

Or a system that implements random walk and is applicable to any component that has a `Position`.

Or we could imagine a system that verifies if two entities with a Person component being close enough - will first approach each other, and then stop to simulate a chat, based on their age and sex.

Thanks to the systems being generic and composable - we could run all the three systems above at the same time. After seeding the simulation with some people, we could see them walking around, stopping and chatting.

### Need a dataframe implementation?

Trurl components are stored in memory as heap objects - instead, they are stored in a dataframe-like structure, similar to the one used by Pandas, R, Spark, etc.

The dataframe allows for easy mapping between components and tabular files (CSV, ORC, VisNow) or even more complex data stores, like SQL databases or TMX files - this means that the results of a simulation (and even snapshots of their state) can be easily exported and inspected using tools like Jupyter Notebook, VisNow, QGIS, Tiled etc.

Also, the dataframe is a first-class citizen in Trurl. It can be accessed directly in a number of ways, allowing bare-metal optimizations, like using the Vector API.

### Are you a Java programmer?

The dataframe and ECS aspects of Trurl are clearly connected via abstractions evolved from the Patterns Of Enterprise Architecture. Via the use of Identity Map (akin to a Hibernate Session), ECS entities can be used in a way similar to JPA entities.

In practice this means that in large simulations most of the data resides in the dataframe, but the part that is currently processed is loaded into memory as a set of objects, which can be used in a natural, Object Oriented way. This allows for balancing between performance and interpretability of the code. Also, there's no danger of paining oneself into a corner - any code using OO access can be easily rewritten to use the dataframe directly, with minimum changes - and still benefiting from static typing.

## What do I need to know to use Trurl?

You can start hacking on the examples. But we recommend first reading the docs, starting from the ground up - to get an overview. Trurl is simple enough to understand thoroughly, and knowing exactly how it works will help you to use it effectively.

- [Store API](./store.md) describes how to use the dataframe aspect of Trurl, read and write data.
- [Dependency Injection and configuration](./di.md) describes how to use Bento, Trurl's DI container.
- [Engine API](./ecs.md) describes how to use the ECS aspect of Trurl, read and write data.

- 
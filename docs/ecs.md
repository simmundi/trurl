

# Navigating model interfaces and executing logic

### Mappers - ODM - Object-Dataframe Mapping

While dataframes are state-of-the-art solution for analytics and aggregation,
logic for simulations is usually better expressed within the Object-Oriented
Programming paradigm; indeed the first OO language was Simula, designed expressly
for simulations.

`Mapper<Component>` establishes a link between a `Component` (a DTO-style class storing pieces of data,
like `Person`, `Place`...) and a set of `Attribute`s. The component itself is a POJO (_Plain Old Java Object_),
adhering to some additional, unobtrusive conventions.

Mappers are used not only to save components to a `Store` and load them back, but also configure a store
by creating the necessary attributes. In a way, components and their mappers serve as schemas for dataframes.

Mappers can be written by hand, but generally are autogenerated by `trurl-generator` (an annotation processor).
A mapper is generated for each class annotated with `@WithMapper`.

Things that should be described in depth in a future release of the docs:
- mappers contain hooks for acting upon changes, but these are meant for low-level code, like building indexes.
- mappers contain functionality for concurrent access (both read and write)
- for some features (like dirty marking) component needs to implement additional interfaces.


## Engine and Session

Engine stores all the simulation data as a single dataframe implemented as a columnar store and organized into
a hierarchy of column groups, describing various components.

Assuming our model is described by these two classes:

```java

@WithMapper
public class Person {
    private String name;
    private String lastName;

    // ... getters, setters,
}

@WithMapper
public class Position {
    private float x;
    private float y;

    // ... getters, setters
}

```

The in-memory representation of the dataframe can be thought of as this table:

| name  | lastName | x                   | y                   |
|-------|----------|---------------------|---------------------|
| Filip | Dreger   | 12                  | 45                  |
| Jan   | Kowalski | 4                   | 14                  |
| Anna  | Kowalska | 4                   | 14                  |
| Jon   | Jonsson  | `Integer.MIN_VALUE` | `Integer.MIN_VALUE` |
| Jan   | Kowalski | 4                   | 14                  |

And it will actually consist of four arrays: one for names, one for last names, one for each of the coordinates.

This means that even if the model contains millions of entities, it can be stored in four allocated arrays (although
yes, strings and - generally - variable size fields are another story, described later).

## Basic operations: iteration, chunks and selectors

Selector (an interface)
: Selector describes a set of entities, grouped into `Chunk`s. The grouping suggests affinity
between entities sharing one chunk, clients of the `Selector`s should use chunks as units for scatter/gather-type
scenarios. Selector does not contain (or even need to know about) actual entities, only their identifiers.

Chunk (an interface)
: describes a set of entities that are meant to be processed together. A chunk has an integer identifier (valid within a
single selector), an optional label - and an integer stream of entity identifiers.


# Engine

`Engine` is the object coordinating storing and managing:
- components (in turn, managed by `Mapper` instances)
- sessions (it uses a stored `SessionFactory`)
- listeners

It allows for running pieces of logic, called *systems*, but engine does not store or manage
`System` instances.

The list of components available and session configuration are **immutable** and
initialized at construction time. On the other hand, different objects can
require existence of certain components.

This is why `Engine` is not instantiated directly, but by `EngineConfiguration`.

## Objects that depend on language - HOWTO

No object should depend directly on `Engine` but rather on `EngineConfiguration`, which acts like a lazy loader and
allows all the objects to affect things like list of components mapped by the `Engine`.

Consequently, objects depending on `EngineConfiguration` shouldn't call `getEngine()` in their constructors,
because that would instantiate the `Engine` prematurely and  freeze its configuration.

## Objects that really need `Engine` as part of their setup

When `Engine` is **really** needed during object's setup, there are two ways:
- make the whole object lazy instantiated, never depend on it directly, but rather on its factory. `@BentoWrapper` can be used to implement such factory with minimum of fuss.
- defer object's setup - constructor calls `EngineConfiguration#addListener` to add a listener to perform the actual setup. The setup is guaranteed to be triggered before the engine is returned from the first `getEngine` call.

# Configuration

`Engine` and its parts require some setup, but the defaults might suffice. The configurable properties and their default values
are as follows:

```properties
trurl.engine.storeFactory=array
trurl.engine.componentAccessor=dynamic
trurl.engine.initialCapacity=1024
trurl.engine.capacityHeadroom=128
trurl.engine.sharedSession=false
```


`Engine` is the central point of Trurl's configuration; the assumption is that each simulation uses a single
engine. The engine contains a single `Store` and a set of mappers. Hence, the engine
instance acts as a data container, enabling usage and persistence of specific components.

Engine acts as the point of entry for most of the code: it can load and save data to external systems / files.
Finally, an engine manages execution of logic that affects the data - the `System`s.

## Queries

Query
: interface for logic meant for filtering, grouping and gathering of entities

A `Query` could be written for cases like:

- filtering out households,
- filtering out households and grouping them (i.e. "tagged" or "chunked") into poviats
- filtering out primary school kids living in the same household with at least one person older than 70

A more precise definition of a `Query` is: it is an visit, which describes how a **set of tagged Entities** can be derived from **one
tagged Entity**. To use a query, typically a client must be created (implementing `Query.Result`) and the query itself needs to be
called per each entity in the `Engine`.

The tag, in this context, is a plain string, which can be accompanied by an additional classifier (which is also a string).
The intention of tags is to directly turn them into chunks (entities with the same tag are expected to be processed together).
The tag classifier is an extension point - another level of entity classification. The intended scenario is to enable
efficient creation of multiple selectors in one pass.

Queries are free to use null tags, if that makes sense. Results are free to throw `NullPointerExceptions` upon
receiving a null tag, if that makes sense. This only means that some query/results combination don't make sense.

```java
public interface Query {
    void process(Entity entity, Query.Result result, String tag);

    interface Result {
        default void add(Entity entity, String tag) {
            add(entity, tag, null);
        }
        void add(Entity entity, String tag, String tagClassifier);
    }
}
```

The implementation of `Query` is expected to inspect incoming `Entity` and - based on it - `add` any number of entities
to the `Results`. The implementation is free to include the input `Entity` in the results, ignore it, add any other
entities etc.

The main use case for the `Query` implementations is to call them with each `Entity` from the engine, and gather all the
results in a single `Result` implementation, which could be:

- an aggregator for some sort of statistics (e.g. counting an average of some property)
- a set of entities
- a `Selector` implemetation, allowing us to run a system on the selected entities.

### Query examples

Some examples:

```jshelllanguage
        // a query filtering out households:

        Query query = (e, results, label) -> {
            Household household = e.get(Household.class);
            if (household != null) {
                results.add(e, label);
            }
        };
```

```jshelllanguage
        // filter out households, chunk them by teryt

        Query query = (e, results, label) -> {
            var household = e.get(Household.class);
            if (household != null) {
                var unit = e.get(AdministrationUnit.class);
                results.add(e, unit == null ? "unclassified" : unit.getTeryt());
            }
        };
```

```jshelllanguage
        // find all the kids of 10 and less, if they share household with a senior

        Query query = (e, results, label) -> {
            var household = e.get(Household.class);
            if (household != null) {
                boolean containsSenior = household.getMembers().stream()
                        .anyMatch(member -> member.get(Person.class).getAge() >= 70);
                if (containsSenior) {
                    household.getMembers().forEach(member -> {
                        if (member.get(Person.class).getAge() <= 10) {
                            results.add(member, label);
                        }
                    });
                }
            }
        };

```

## Direct uses

### Creating Selectors

A `Query` can be used to create a selector.

# Navigating model interfaces and executing logic

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

## Queries

Query
: interface for logic meant for filtering, grouping and gathering of entities

A `Query` could be written for cases like:

- filtering out households,
- filtering out households and grouping them (i.e. "labelled" or "chunked") into poviats
- filtering out primary school kids living in the same household with at least one person older than 70

A more precise definition of a `Query` is: it is an operation, which describes how a **set of labeled Entities** can be derived from **one
labeled Entity**. To use a query, typically a client must be created (implementing `Query.Result`) and the query itself needs to be 
called per each entity in the `Engine`.

The `label` in this context is any `String` (or a `null`), but the intention is to use them to group entities into
chunks.

```java
public interface Query {
    void process(Entity entity, Query.Result result, String label);

    interface Result {
        void add(Entity entity, String label);
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
- a `Selector` implenetation, allowing us to run a system on the selected entities.

### Query examples

Some examples:

```java
        // a query filtering out households:

        Query query = (e, results, label) -> {
            Household household = e.get(Household.class);
            if (household != null) {
                results.add(e, label);
            }
        };
```

```java
        // filter out households, chunk them by teryt

        Query query = (e, results, label) -> {
            var household = e.get(Household.class);
            if (household != null) {
                var unit = e.get(AdministrationUnit.class);
                results.add(e, unit == null ? "unclassified" : unit.getTeryt());
            }
        };
```

```java
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
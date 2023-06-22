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
# Store

Store is a dataframe implementation optimized for simulations (i.e. mutable, without strict partitioning).
Each Store can be imagined as a database table with named columns. Each column is an `Attribute` of a specific type.

Primitive attributes are strongly typed:
It is a collection of named `Attribute`s of different subtypes: strings (`StringAttribute`), numbers (`IntAttribute`, `ByteAttribute`... ), booleans, enums etc.

If we think of `Store` in terms of tables or relations, then an attribute represents a single column.

Each row of the store can reference , 
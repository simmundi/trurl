# Store: a mutable, typed data frame

`Store` instance represents a data frame. It manages a collection of named `Attribute`s of different subtypes: strings
(`StringAttribute`), numbers (`IntAttribute`, `ByteAttribute`... ), booleans, enums etc. If we think of `Store` in
terms of tables or relations, then an attribute represents a single column.

A store is usually acquired using dependency injection, but it's simple enough to create one by hand:
```
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;

// ...
int CAPACITY = 1000_000
Store store = new Store(new ArrayAttributeFactory(), CAPACITY);
store.addString("name");
store.addInt("yearOfBirth");
```

The attribute factory is responsible for the low-level storage implementation. The `ArrayAttributeFactory` uses
simple Java arrays, while the `TablesawAttributeFactory` uses the Tablesaw library's `Table` class. Internally, we also
experimented with implementations using memory-mapped buffers, off-heap memory, and JSciC,  but they are not yet available.

Store offers columns of primitive types, as well as strings and enums:

```
// ...
enum Sex {
    MALE, FEMALE
}

store.addEnum("sex", Sex.class);
```

Once a schema is established (by calling the various `add*` methods), the store is ready to accept data. The simplest way is to load data from a CSV or an ORC file - in both cases any columns present in a file but not defined on the store - will be ignored. In the case of CSV files, the store will handle conversions from strings to the appropriate types. In the case of ORC files, the types of the columns present must match the ones defined on the store.

```
// read data from CSV
CsvReader reader = new CsvReader();
reader.read(new File("data.csv"), store);

// save data to ORC
OrcWriter orcWriter = new OrcWriter();
orcWriter.write(new File("data.orc"), store);
```

To access the contents of any of the columns (single attributes), an instance of the attribute must be acquired first, and its simple methods used to access the data:

```
StringAttribute name = store.get("name");
EnumAttribute sex = store.get("sex");
IntAttribute yearOfBirth = store.get("yearOfBirth");

name.setString(0, "Filip");
yearOfBirth.setInt(0, 1977);
sex.setEnum(0, Sex.MALE);
```

The layout of the store is columnar, for optimum performance. It would be tempting to say it uses Struct-of-Arrays instead of Array-Of-Structures, approach, but in reality - the store is generic, so the exact structure required is not know in compile time, so the arrays are stored in a map. This would be far-from-optimal for accessing single cells, so the idea is to use the apprach we call Bring-Your-Own-Struct:

```java
class PeopleData {
    private final StringAttribute names;
    private final EnumAttribute<Sex> sexes;
    private final IntAttribute yearsOfBirth;
    
    public PeopleData(Store store) {
        // set up store
        store.addString("name");
        store.addInt("yearOfBirth");
        store.addEnum("sex", Sex.class);
    
        // get references to attributes
        names = store.get("name");
        sexes = store.get("sex");
        yearsOfBirth = store.get("yearOfBirth");
    }
    
    // methods for data access, reading, writing, low-level business logic etc.

    // low-level, direct array access allows for using optimized array-based algorithms
    // (the code below uses Arrays.* utils, and therefore will use SIMD operations if available)
    public void clearYearsOfBirth() {
        Arrays.fill(yearsOfBirth.liveArray());
    }
    
    // high-level, object-oriented access
    public Person fetchPerson(int row) {
        return new Person(names.getString(row), sexes.getEnum(row), yearsOfBirth.getInt(row));
    }
    
    // ... many other possibilities ...

```

### Side note about generated mappers

In fact, the core Trurl feature - [described in a separate chapter](ecs.md) - is the ability to generate such classes at compile-time, based on DTOs, and the ability of combining multiple such schemas in a single store, where they describe different aspects of the same entities. 

If we start from creating a simple DTO and add the `@WithMapper` annotation, the Trurl annotation processor will pick it up and generate a proper helper. 

```java
@WithMapper
class Person {
    private String name;
    private int yearOfBirth;
    private Sex sex;
    
    public Person() {}
    
    public Person(String name, Sex sex, int yearOfBirth) {
        this.name = name;
        this.yearOfBirth = yearOfBirth;
        this.sex = sex;
    }
    // ... getters, setters
}
```

Some of the generated methods will be:

// TODO

## Store API

// TODO

### Columns and empty values

// TODO

### Substores - one-to-many and one-to-one relationships between rows

#### hidden attributes

#### array-typed

#### range-typed

// TODO

### References to other rows

// TODO

### Categories

### Dynamic Active Record API

// TODO

### Parallel execution - chunks and locks

// TODO
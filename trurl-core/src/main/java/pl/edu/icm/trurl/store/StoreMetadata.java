package pl.edu.icm.trurl.store;

/**
 * This is usually used as superinterface of Store.
 * <p>
 * It is used by store clients to configure the store (i.e. associate
 * column names with column types).
 */
public interface StoreMetadata {
    void addBoolean(String name);

    void addByte(String name);

    void addDouble(String name);

    void addEntity(String name);

    void addEntityList(String name);

    void addValueObjectList(String name);

    <E extends Enum<E>> void addEnum(String name, Class<E> enumType);

    void addFloat(String name);

    void addInt(String name);

    void addShort(String name);

    void addString(String name);
}

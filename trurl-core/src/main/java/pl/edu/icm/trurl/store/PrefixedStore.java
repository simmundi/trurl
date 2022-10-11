package pl.edu.icm.trurl.store;

import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.stream.Stream;

public class PrefixedStore implements Store {
    private final Store wrapped;
    private final StoreConfigurer wrappedMeta;
    private final String prefix;

    public static Store wrap(Store wrappedStore, String prefix) {
        return new PrefixedStore(wrappedStore, prefix);
    }

    public static Store wrap(StoreConfigurer wrappedMeta, String prefix) {
        return new PrefixedStore(wrappedMeta, prefix);
    }

    private PrefixedStore(Store wrappedStore, String prefix) {
        this.wrapped = wrappedStore;
        this.wrappedMeta = wrappedStore;
        this.prefix = prefix;
    }

    private PrefixedStore(StoreConfigurer wrappedMeta, String prefix) {
        this.wrappedMeta = wrappedMeta;
        this.wrapped = null;
        this.prefix = prefix;
    }

    @Override
    public <T extends Attribute> T get(String name) {
        return wrapped.get(wrap(name));
    }

    @Override
    public void addStoreListener(StoreListener storeListener) {
        wrapped.addStoreListener(storeListener);
    }

    @Override
    public void fireUnderlyingDataChanged(int fromInclusive, int toExclusive, StoreListener... excluded) {
        wrapped.fireUnderlyingDataChanged(fromInclusive, toExclusive, excluded);
    }

    @Override
    public Stream<Attribute> attributes() {
        return wrapped.attributes();
    }

    @Override
    public int getCount() {
        return wrapped.getCount();
    }

    @Override
    public void addBoolean(String name) {
        wrappedMeta.addBoolean(wrap(name));
    }

    @Override
    public void addByte(String name) {
        wrappedMeta.addByte(wrap(name));
    }

    @Override
    public void addDouble(String name) {
        wrappedMeta.addDouble(wrap(name));
    }

    @Override
    public void addEntity(String name) {
        wrappedMeta.addEntity(wrap(name));
    }

    @Override
    public void addEntityList(String name) {
        wrappedMeta.addEntityList(wrap(name));
    }

    @Override
    public void addValueObjectList(String name) {
        wrappedMeta.addValueObjectList(wrap(name));
    }

    @Override
    public <E extends Enum<E>> void addEnum(String name, Class<E> enumType) {
        wrappedMeta.addEnum(wrap(name), enumType);
    }

    @Override
    public void addFloat(String name) {
        wrappedMeta.addFloat(wrap(name));
    }

    @Override
    public void addInt(String name) {
        wrappedMeta.addInt(wrap(name));
    }

    @Override
    public void addShort(String name) {
        wrappedMeta.addShort(wrap(name));
    }

    @Override
    public void addString(String name) {
        wrappedMeta.addString(wrap(name));
    }

    private String wrap(String name) {
        return prefix + "." + name;
    }
}

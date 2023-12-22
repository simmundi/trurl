package pl.edu.icm.trurl.ecs;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public class ReadWriteSession extends AbstractSession {
    private final Long2IntOpenHashMap idToSessionIndex;
    private final MapperSet mapperSet;
    private final Engine engine;
    private final Object[][] components;
    private final long[] ids;

    public ReadWriteSession(int capacity, Engine engine) {
        idToSessionIndex = new Long2IntOpenHashMap(capacity);
        this.mapperSet = engine.getMapperSet();
        int componentCount = mapperSet.componentCount();
        components = new Object[componentCount][capacity];
        ids = new long[capacity];
        this.engine = engine;
    }

    <T> T get(Class<T> componentClass, int sessionIndex) {
        return (T) components[mapperSet.classToIndex(componentClass)][sessionIndex];
    }

    <T> T getOrCreate(Class<T> componentClass, int sessionIndex) {
        int i = mapperSet.classToIndex(componentClass);
        T existing = (T) components[mapperSet.classToIndex(componentClass)][sessionIndex];
        if (existing == null) {
//            T created = mapperSet.
        }
        return existing;
    }

    <T> T add(T component, int sessionIndex) {

    }

    long getId(int sessionIndex) {
        return ids[sessionIndex];
    }

    <T> T get(ComponentToken<T> token, int sessionIndex, boolean createIfDoesntExist) {
        if (components[token.index][sessionIndex] == null) {
            long id = ids[sessionIndex];
            if (token.mapper.isPresent((int) id)) {
                T component = token.mapper.createAndLoad(this, id);
            }

        }
        return (T) components[token.index][sessionIndex];
    }

    @Override
    public void close() {

    }

    @Override
    public Entity getEntity(int id) {
        return null;
    }

    @Override
    public Entity createEntity(Object... components) {
        return null;
    }

    @Override
    public void deleteEntity(Entity entity) {

    }

    @Override
    public Engine getEngine() {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public int getOwnerId() {
        return 0;
    }
}

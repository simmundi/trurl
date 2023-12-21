package pl.edu.icm.trurl.ecs;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public class ReadWriteSession extends AbstractSession {
    private final Long2IntOpenHashMap idToSessionIndex;
    private final MapperSet mapperSet;
    private final Engine engine;
    private final Object[][] components;
    private final long[] ids;
    private final int capacity;

    public ReadWriteSession(int capacity, Engine engine) {
        this.capacity = capacity;
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
        T existing = get(componentClass, sessionIndex);
        if (existing == null) {

        }
    }

    <T> T add(T component, int sessionIndex) {

    }

    public int getId(int sessionIndex) {
        return (int) ids[sessionIndex];
    }

    public <T> T get(ComponentToken<T> token, int sessionIndex) {
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

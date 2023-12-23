package pl.edu.icm.trurl.ecs;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public class Session {
    private final Long2IntOpenHashMap idToSessionIndex;
    private final DaoManager daoManager;
    private final Engine engine;
    private final Object[][] components;
    private final Entity[] entities;
    private final int[] ids;
    private int ownerId;
    private int counter;

    Session(Engine engine, int capacity) {
        idToSessionIndex = new Long2IntOpenHashMap(capacity);
        this.daoManager = engine.getDaoManager();
        int componentCount = daoManager.componentCount();
        components = new Object[componentCount][capacity];
        ids = new int[capacity];
        entities = new Entity[capacity];
        this.engine = engine;
        this.counter = 0;
    }

    public void clear() {
        idToSessionIndex.clear();
        for (int i = 0; i < components.length; i++) {
            Object[] component = components[i];
            for (int j = 0; j < component.length; j++) {
                component[j] = null;
            }
        }
        for (int i = 0; i < entities.length; i++) {
            entities[i] = null;
        }
        counter = 0;
    }

    public void flush() {
        flush(daoManager.allTokens());
    }

    public void close() {
        flush();
        clear();
    }

    public void flush(ComponentToken<?>... tokens) {
        for (ComponentToken token : tokens) {
            for (int i = 0; i < counter; i++) {
                if (components[token.index][i] != null) {
                    token.dao.save(this, components[token.index][i], (int) ids[i]);
                }
            }
        }
    }

    <T> T get(Class<T> componentClass, int sessionIndex) {
        return get(daoManager.classToToken(componentClass), sessionIndex, false);
    }

    <T> T getOrCreate(Class<T> componentClass, int sessionIndex) {
        return get(daoManager.classToToken(componentClass), sessionIndex, true);
    }

    <T> T add(T component, int sessionIndex) {
        return add((ComponentToken<T>) daoManager.classToToken(component.getClass()), component, sessionIndex);
    }

    <T> T add(ComponentToken<T> token, T component, int sessionIndex) {
        components[token.index][sessionIndex] = component;
        return component;
    }

    int getId(int sessionIndex) {
        return ids[sessionIndex];
    }

    <T> T get(ComponentToken<T> token, int sessionIndex, boolean createIfDoesntExist) {
        T existing = (T) components[token.index][sessionIndex];
        if (existing != null) {
            return existing;
        } else {
            long id = ids[sessionIndex];
            if (token.dao.isPresent((int) id)) {
                T component = token.dao.createAndLoad(this, (int) id);
                components[token.index][sessionIndex] = component;
                return component;
            } else if (createIfDoesntExist) {
                T component = token.dao.create();
                components[token.index][sessionIndex] = component;
                return component;
            } else {
                return null;
            }
        }
    }

    public Entity getEntity(int id) {
        int sessionIndex = idToSessionIndex.getOrDefault(id, -1);
        if (sessionIndex >= 0) {
            return entities[sessionIndex];
        } else {
            return createEmptyEntity(id);
        }
    }

    private Entity createEmptyEntity(int id) {
        int newIndex = counter++;
        idToSessionIndex.put(id, newIndex);
        ids[newIndex] = id;
        Entity entity = new Entity(this, newIndex);
        entities[newIndex] = entity;
        return entity;
    }

    public Entity createEntity(Object... components) {
        int id = engine.allocateNextId();
        Entity entity = createEmptyEntity(id);
        for (Object component : components) {
            entity.add(component);
        }
        return entity;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public Engine getEngine() {
        return engine;
    }
}

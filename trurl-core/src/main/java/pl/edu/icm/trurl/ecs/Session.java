package pl.edu.icm.trurl.ecs;

import pl.edu.icm.trurl.collection.IntMap;

import java.util.HashMap;
import java.util.HashSet;

public class Session {
    private final IntMap<Entity> idToEntity;
    private final DaoManager daoManager;
    private final Engine engine;
    private final Object[][] components;
    private final boolean[] toDelete;
    private final Entity[] entities;
    private final int[] ids;
    private int ownerId;
    private int counter;

    Session(Engine engine, int capacity) {
        idToEntity = new IntMap<>(capacity);
        toDelete = new boolean[capacity];
        this.daoManager = engine.getDaoManager();
        int componentCount = daoManager.componentCount();
        components = new Object[componentCount][capacity];
        ids = new int[capacity];
        entities = new Entity[capacity];
        this.engine = engine;
        this.counter = 0;
    }

    public void clear() {
        idToEntity.clear();
        for (int i = 0; i < counter; i++) {
            if (toDelete[i]) {
                engine.getRootStore().freeIndex(ids[i]);
            }
            toDelete[i] = false;
            Object[] component = components[i];
            for (int j = 0; j < component.length; j++) {
                component[j] = null;
            }
        }
        for (int i = 0; i < counter; i++) {
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
                if (toDelete[i]) {
                    continue;
                }
                if (components[token.index][i] != null) {
                    token.dao.save(this, components[token.index][i], ids[i]);
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
            int id = ids[sessionIndex];
            if (token.dao.isPresent(id)) {
                T component = token.dao.createAndLoad(this, id);
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
        Entity entity  = idToEntity.get(id);
        return entity != null ? entity : createEmptyEntity(id);
    }

    protected void deleteEntityBySessionIndex(int sessionIndex) {
        toDelete[sessionIndex] = true;
    }

    private Entity createEmptyEntity(int id) {
        int newIndex = counter++;
        ids[newIndex] = id;
        Entity entity = new Entity(this, newIndex);
        idToEntity.put(id, entity);
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

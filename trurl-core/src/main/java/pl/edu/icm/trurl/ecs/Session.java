package pl.edu.icm.trurl.ecs;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

final public class Session {
    private final HashMap<Integer, Entity> idToEntity;
    private final DaoManager daoManager;
    private final Engine engine;
    private final Object[][] components;
    private final Entity[] entities;
    private final int[] ids;
    private int ownerId;
    private int counter;
    private ComponentToken<?>[] tokens;

    Session(Engine engine, int capacity) {
        idToEntity = new HashMap<>(capacity);
        this.daoManager = engine.getDaoManager();
        int componentCount = daoManager.componentCount();
        components = new Object[componentCount][capacity];
        tokens = Arrays.copyOf(daoManager.allTokens(), daoManager.allTokens().length);
        ids = new int[capacity];
        entities = new Entity[capacity];
        this.engine = engine;
        this.counter = 0;
    }

    public void clear() {
        idToEntity.clear();
        // .stream() is unavailable for BitSet in GWT
        for (int i = 0; i < components.length; i++) {
            Arrays.fill(components[i], 0, counter, null);
        }
        Arrays.fill(entities, 0, counter, null);
        counter = 0;
    }

    public <T> T getIfAvailable(int id, ComponentToken<T> token) {
        Entity entity = idToEntity.get(id);
        if (entity == null) {
            return null;
        } else {
            return (T) components[token.index][entity.getSessionIndex()];
        }
    }

    public void setDefaultFlushTokensToAll() {
        tokens = Arrays.copyOf(daoManager.allTokens(), daoManager.allTokens().length);
    }

    public void setDefaultFlushTokens(ComponentToken<?>... tokens) {
        this.tokens = Arrays.copyOf(tokens, tokens.length);
    }

    public void flush() {
        flush(tokens);
    }

    public void close() {
        flush();
        clear();
    }

    public Collection<Entity> findEntitiesInSession() {
        return idToEntity.values();
    }

    public Entity findEntityInSession(int id) {
        return idToEntity.get(id);
    }

    public void evictEntity(Entity entity) {
        internalEvictEntity(entity.getSessionIndex());
    }

    public void flushEntity(Entity entity) {
        internalFlushEntity(entity.getSessionIndex());
    }

    void internalFlushEntity(int sessionIndex) {
        throw new UnsupportedOperationException("not yet");
    }

    void internalEvictEntity(int sessionIndex) {
        throw new UnsupportedOperationException("not yet");
    }

    public void flush(ComponentToken<?>... tokens) {
        for (int i = 0; i < counter; i++) {
            if (ids[i] < 0) {
                engine.getRootStore().getCounter().free(~ids[i]);
            }
        }
        for (ComponentToken token : tokens) {
            for (int i = 0; i < counter; i++) {
                int id = ids[i];
                if (id < 0) {
                    continue;
                }
                if (components[token.index][i] != null) {
                    token.dao.save(this, components[token.index][i], id);
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
        if (ids[sessionIndex] < 0) {
            return;
        }
        ids[sessionIndex] = ~ids[sessionIndex];
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

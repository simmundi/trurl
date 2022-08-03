package pl.edu.icm.trurl.ecs;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import pl.edu.icm.trurl.ecs.mapper.ComponentOwner;

public final class Session implements ComponentOwner {
    private final Engine engine;
    private final Int2ObjectMap<Entity> entities;

    private final boolean detachedEntities;
    private final boolean createStubEntities;
    private final boolean persist;

    private final int ownerId;

    Session(Engine engine, int expectedEntityCount, Mode mode, int ownerId) {
        this.engine = engine;
        this.createStubEntities = mode == Mode.STUB_ENTITIES;
        this.detachedEntities = mode == Mode.DETACHED_ENTITIES;
        this.persist = (mode == Mode.NORMAL || mode == Mode.SHARED);
        this.entities = (detachedEntities || expectedEntityCount == 0) ? null : new Int2ObjectOpenHashMap<>(expectedEntityCount);
        this.ownerId = ownerId;
    }

    public void close() {
        if (persist) {
            entities.values().stream().forEach(Entity::persist);
        }
    }

    public Entity getEntity(int id) {
        if (createStubEntities) {
            return new Entity(id);
        } else if (detachedEntities) {
            return new Entity(engine.getMapperSet(), this, id);
        } else {
            return entities
                    .computeIfAbsent(id, newId ->
                            new Entity(engine.getMapperSet(), this, newId));
        }
    }

    public Entity createEntity(Object... components) {
        Entity entity = getEntity(engine.nextId());
        for (Object component : components) {
            entity.add(component);
        }
        return entity;
    }

    public Engine getEngine() {
        return engine;
    }

    public int getCount() {
        return entities == null ? 0 : entities.size();
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    public enum Mode {
        NORMAL,
        STUB_ENTITIES,
        DETACHED_ENTITIES,
        NO_PERSIST,
        SHARED
    }
}

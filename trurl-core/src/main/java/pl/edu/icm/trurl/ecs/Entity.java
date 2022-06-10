package pl.edu.icm.trurl.ecs;

import pl.edu.icm.trurl.ecs.mapper.Mapper;

import java.util.Objects;
import java.util.Optional;

public final class Entity {
    public static final int NULL_ID = Integer.MIN_VALUE;

    private final MapperSet mapperSet;
    private final Session session;
    private final int id;
    private final Object[] components;

    /**
     * Creates a stub entity (basically, a type-safe id wrapper)
     */
    public Entity(int id) {
        this.mapperSet = null;
        this.session = null;
        this.components = null;
        this.id = id;
    }

    public Entity(MapperSet mapperSet, Session session, int id) {
        this.mapperSet = mapperSet;
        this.session = session;
        this.components = new Object[mapperSet.componentCount()];
        this.id = id;
    }

    public <T> T get(Class<T> componentClass) {
        return get(componentClass, false);
    }

    public <T> T getOrCreate(Class<T> componentClass) {
        return get(componentClass, true);
    }

    public <T> T add(T component) {
        int idx = mapperSet.classToIndex(component.getClass());
        components[idx] = component;
        return component;
    }

    public void persist() {
        for (int idx = 0; idx < components.length; idx++) {
            if (components[idx] != null) {
                Mapper<Object> mapper = mapperSet.indexToMapper(idx);
                mapper.save(components[idx], id);
            }
        }
    }

    public int getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public <T> Optional<T> optional(Class<T> classToken) {
        return Optional.ofNullable(get(classToken));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private <T> T get(Class<T> componentClass, boolean createIfDoesntExist) {
        int idx = mapperSet.classToIndex(componentClass);
        if (components[idx] == null) {
            Mapper<T> mapper = mapperSet.classToMapper(componentClass);
            if (mapper.isPresent(id)) {
                components[idx] = mapper.createAndLoad(session, id);
            } else if (createIfDoesntExist) {
                components[idx] = mapper.create();
            }
        }
        return (T) components[idx];
    }
}

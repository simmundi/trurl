package pl.edu.icm.trurl.ecs;

import java.util.Objects;

public final class Entity {
    public final static int NULL_ID = Integer.MIN_VALUE;

    private final Session session;
    private final int sessionIndex;

    Entity(Session session, int sessionIndex) {
        this.session = session;
        this.sessionIndex = sessionIndex;
    }

    public int getId() {
        return session == null ? sessionIndex : session.getId(sessionIndex);
    }

    int getSessionIndex() {
        return sessionIndex;
    }

    public <T> T get(Class<T> componentClass) {
        return session.get(componentClass, sessionIndex);
    }

    public <T> T get(ComponentToken<T> token) {
        return session.get(token, sessionIndex, false);
    }

    public <T> T getOrCreate(Class<T> componentClass) {
        return session.getOrCreate(componentClass, sessionIndex);
    }

    public <T> T getOrCreate(ComponentToken<T> componentClass) {
        return null;
    }

    public <T> T add(T component) {
        return session.add(component, sessionIndex);
    }

    public <T> T add(ComponentToken<T> token, T component) {
        return null;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return sessionIndex == entity.sessionIndex && Objects.equals(session, entity.session);
    }

    public void delete() {
        session.deleteEntityBySessionIndex(sessionIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionIndex);
    }

    public static Entity stub(int id) {
        return new Entity(null, id);
    }
}

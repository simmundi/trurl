package pl.edu.icm.trurl.ecs;

public final class Entity extends AnyEntity {

    private final Session session;
    private int sessionIndex;

    Entity(Session session, int sessionIndex) {
        this.session = session;
        this.sessionIndex = sessionIndex;
    }

    @Override
    public <T> T get(Class<T> componentClass) {
        return session.get(componentClass, sessionIndex);
    }

    @Override
    public <T> T get(ComponentToken<T> token) {
        return session.get(token, sessionIndex, false);
    }

    @Override
    public <T> T getOrCreate(Class<T> componentClass) {
        return session.getOrCreate(componentClass, sessionIndex);
    }

    @Override
    public <T> T getOrCreate(ComponentToken<T> componentClass) {
        return null;
    }

    @Override
    public <T> T add(T component) {
        return session.add(component, sessionIndex);
    }

    @Override
    public <T> T add(ComponentToken<T> token, T component) {
        return null;
    }

    @Override
    public long getId() {
        return session.getId(sessionIndex);
    }

    @Override
    public Session getSession() {
        return session;
    }

}

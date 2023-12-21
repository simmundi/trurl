package pl.edu.icm.trurl.ecs;

public final class ReadWriteEntity extends AbstractEntity {

    private final ReadWriteSession session;
    private int sessionIndex;

    private ReadWriteEntity(ReadWriteSession session, int sessionIndex) {
        this.session = session;
        this.sessionIndex = sessionIndex;
    }

    @Override
    public <T> T get(Class<T> componentClass) {
        return session.get(componentClass, sessionIndex);
    }

    @Override
    public <T> T getOrCreate(Class<T> componentClass) {
        return session.getOrCreate(componentClass, sessionIndex);
    }

    @Override
    public <T> T add(T component) {
        return session.add(component, sessionIndex);
    }

    @Override
    public int getId() {
        return session.getId(sessionIndex);
    }

    @Override
    public ReadWriteSession getSession() {
        return session;
    }

    @Override
    public <T> T get(ComponentToken<T> token) {
        return session.get(token, sessionIndex);
    }
}

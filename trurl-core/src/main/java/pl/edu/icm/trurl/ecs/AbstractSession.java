package pl.edu.icm.trurl.ecs;

public abstract class AbstractSession {
    public abstract void close();

    public abstract Entity getEntity(int id);

    public abstract Entity createEntity(Object... components);

    public abstract void deleteEntity(Entity entity);

    public abstract Engine getEngine();

    public abstract int getCount();

    public abstract int getOwnerId();
}

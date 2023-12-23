package pl.edu.icm.trurl.ecs;

public abstract class AnyEntity {
    public static final int NULL_ID = Integer.MIN_VALUE;

    public abstract <T> T get(Class<T> componentClass);

    public abstract <T> T get(ComponentToken<T> token);

    public abstract <T> T getOrCreate(Class<T> componentClass);

    public abstract <T> T getOrCreate(ComponentToken<T> componentClass);

    public abstract <T> T add(T component);

    public abstract <T> T add(ComponentToken<T> token, T component);

    public abstract long getId();

    public abstract AbstractSession getSession();
}
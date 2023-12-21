package pl.edu.icm.trurl.ecs;

public abstract class AbstractEntity {
    public static final int NULL_ID = Integer.MIN_VALUE;

    public abstract <T> T get(Class<T> componentClass);

    public abstract <T> T getOrCreate(Class<T> componentClass);

    public abstract <T> T add(T component);

    public abstract int getId();

    public abstract AbstractSession getSession();

    public abstract <T> T get(ComponentToken<T> token);
}

package pl.edu.icm.trurl.ecs.mapper.feature;

/**
 * Component gets an extra copy of itself
 * when it's loaded from mapper.
 *
 * This might be a bit slower (costs at least an extra reference)
 * than maintaining a copy of the data by hand.
 *
 * @param <T> Should be the same class as the component
 *
 */
public interface RequiresOriginalCopy<T> {
    void setOriginalCopy(T other);
}

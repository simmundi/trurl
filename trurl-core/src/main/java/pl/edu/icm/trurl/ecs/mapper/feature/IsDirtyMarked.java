package pl.edu.icm.trurl.ecs.mapper.feature;

/**
 * IsDirtyMarked implements custom logic for checking whether it is "dirty"
 * (should be written back to the store) or not.
 *
 * This custom logic seems to be required in components implementing Resolving,
 * since even a component containing exactly the same data as in the store
 * might require writing back (component can change due to a call to #resolve).
 */
public interface IsDirtyMarked {
    void markAsClean();
    boolean isDirty();
}

package pl.edu.icm.trurl.ecs.mapper.feature;

/**
 * This component stores the id of its owner
 * Upon a conflict with another copy of the row, it can resolve the
 * conflict by inspecting the other copy and applying necessary changes
 * to itself.
 *
 * @param <T> Should be the same class as the component
 */
public interface CanResolveConflicts<T> {
    T resolve(T other);

    int getOwnerId();

    void setOwnerId(int ownerId);
}

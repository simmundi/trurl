package pl.edu.icm.trurl.store;

/**
 * Used instead of a full store in situations where
 * it's important to show that the store might be assumed to be empty.
 *
 */
public interface StoreFactory {
    Store create(int initialCapacity);
    default int defaultInitialCapacity() {
        return 1024;
    }
}

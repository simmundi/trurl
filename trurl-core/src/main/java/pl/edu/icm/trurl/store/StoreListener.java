package pl.edu.icm.trurl.store;

import pl.edu.icm.trurl.ecs.SessionFactory;

import java.util.Optional;

/**
 * listener of a coarse-grained event informing that the data in the store
 * was changed by one of the store's clients.
 *
 * This might happen within a session or without one, depending on the circumstances.
 */
public interface StoreListener {
    void onUnderlyingDataChanged(int fromInclusive, int toExclusive);
}

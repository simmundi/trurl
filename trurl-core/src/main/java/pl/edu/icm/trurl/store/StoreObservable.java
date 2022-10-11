package pl.edu.icm.trurl.store;


public interface StoreObservable {

    /**
     * Adds a listener to the store. Neither the store nor its attributes
     * fire events, so the events are fired on a cooperative basis - any
     * user of the store performing changes that might be of interest to someone
     * is expected to call fireUnderlyingDataChanged.
     *
     * @param storeListener
     */
    void addStoreListener(StoreListener storeListener);
}

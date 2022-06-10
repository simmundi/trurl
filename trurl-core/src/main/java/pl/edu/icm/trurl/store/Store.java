package pl.edu.icm.trurl.store;

import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a columnar store.
 * <p>
 * The interface gives access to specific attributes (instances of Attribute, which can be
 * downcast to interfaces like ShortAttribute, StringAttribute etc.) and acts as a hub
 * for adding listeners and firing general events.
 * <p>
 * The events are not fired automatically by any part of the store; they are meant
 * for situations where a couple of consumers of the same store wish to let
 * one another know about changes to the data - like when a loader loads data
 * into the store.
 * <p>
 * Within Trurl, the stores are meant to store entities / components.
 * <p>
 * The flow is:
 *
 * <ul>
 *     <li>
 *         code creates a store instance,
 *     </li>
 *     <li>
 *         the instance is passed to Mappers, which use the StoreMetadata
 *     interface to configure types and names of the store's columns
 *     </li>
 *     <li>
 *         the mappers are then attached to the store (i.e. get all the
 *     necessary attributes) and become its listeners
 *     </li>
 *     <li>
 *         the store configuration is used to load raw data from a file
 *     </li>
 *     <li>
 *         the loader fires underlying data changed event; the mappers
 *     handle it and use it to find out about the length of the data and
 *     to fire any required events to their relevant listeners.
 *     </li>
 * </ul>
 */
public interface Store extends StoreMetadata {
    /**
     * Returns an attribute of the given name, if exists
     *
     * @param name
     * @param <T>
     * @return attribute or null, if none found
     */
    <T extends Attribute> T get(String name);

    /**
     * Adds a listener to the store. Neither the store nor its attributes
     * fire events, so the events are fired on a cooperative basis - any
     * user of the store performing changes that might be of interest to someone
     * is expected to call fireUnderlyingDataChanged.
     *
     * @param storeListener
     */
    void addStoreListener(StoreListener storeListener);

    /**
     * Part of the cooperative event system - calling this method notifies
     * all the storeListeners.
     *
     * @param fromInclusive
     * @param toExclusive
     * @param excludedListeners list of listers to ignore while sending the event
     */
    void fireUnderlyingDataChanged(int fromInclusive, int toExclusive, StoreListener... excludedListeners);

    /**
     * returns stream of all the attributes.
     *
     * @return attributes
     */
    Stream<Attribute> attributes();

    /**
     * Returns the max value passed to fireUnderlyingDataChanged event
     * as toExclusive.
     */
    int getCount();

}
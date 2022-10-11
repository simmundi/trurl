package pl.edu.icm.trurl.store;

import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.stream.Stream;

public interface StoreInspector {
    /**
     * Returns an attribute of the given name, if exists
     *
     * @param name
     * @param <T>
     * @return attribute or null, if none found
     */
    <T extends Attribute> T get(String name);

    /**
     * Part of the cooperative event system - calling this method notifies
     * all the storeListeners.
     *
     * @param fromInclusive
     * @param toExclusive
     * @param excludedListeners list of listeners to ignore while sending the event
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

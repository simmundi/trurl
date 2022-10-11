package pl.edu.icm.trurl.store;

import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StoreSubset implements StoreInspector {
    private final Map<String, Attribute> attributes = new LinkedHashMap<>();
    private int count;
    private final StoreInspector originalStore;

    StoreSubset(StoreInspector store, Predicate<Attribute> predicate, int count) {
        this.originalStore = store;
        store
                .attributes()
                .filter(predicate)
                .forEach(attribute -> attributes.put(attribute.name(), attribute));
        this.count = count;
    }

    @Override
    public <T extends Attribute> T get(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public void fireUnderlyingDataChanged(int fromInclusive, int toExclusive, StoreListener... excludedListeners) {
        this.originalStore.fireUnderlyingDataChanged(fromInclusive, toExclusive, excludedListeners);
    }

    @Override
    public Stream<Attribute> attributes() {
        return attributes.values().stream();
    }

    @Override
    public int getCount() {
        return count;
    }
}

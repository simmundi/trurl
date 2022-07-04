package pl.edu.icm.trurl.store.tablesaw;

import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreListener;
import pl.edu.icm.trurl.store.StoreMetadata;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.generic.GenericEntityListOverStringAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawBooleanAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawByteAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawDoubleAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawEntityAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawEnumAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawFloatAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawIntAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawShortAttribute;
import pl.edu.icm.trurl.store.tablesaw.attribute.TablesawStringAttribute;
import tech.tablesaw.api.Table;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class TablesawStore implements StoreMetadata, Store {
    private final CopyOnWriteArrayList<StoreListener> listeners = new CopyOnWriteArrayList();
    private AtomicInteger count = new AtomicInteger();

    Map<String, Attribute> attributes = new LinkedHashMap<>(40);

    public <T extends Attribute> T get(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public void addStoreListener(StoreListener storeListener) {
        listeners.add(storeListener);
    }

    @Override
    public void fireUnderlyingDataChanged(int fromInclusive, int toExclusive, StoreListener... excludes) {
        List<StoreListener> excludesList = Arrays.asList(excludes);
        count.set(toExclusive);
        for (StoreListener storeListener : listeners) {
            if (!excludesList.contains(storeListener)) {
                storeListener.onUnderlyingDataChanged(fromInclusive, toExclusive);
            }
        }
    }

    @Override
    public void addBoolean(String name) {
        attributes.putIfAbsent(name, new TablesawBooleanAttribute(name));
    }

    @Override
    public void addByte(String name) {
        attributes.putIfAbsent(name, new TablesawByteAttribute(name));
    }

    @Override
    public void addDouble(String name) {
        attributes.putIfAbsent(name, new TablesawDoubleAttribute(name));
    }

    @Override
    public void addEntity(String name) {
        attributes.putIfAbsent(name, new TablesawEntityAttribute(name));
    }

    @Override
    public void addEntityList(String name) {
        attributes.putIfAbsent(name, new GenericEntityListOverStringAttribute(new TablesawStringAttribute(name)));
    }

    @Override
    public void addValueObjectList(String name) {

    }

    @Override
    public <E extends Enum<E>> void addEnum(String name, Class<E> enumType) {
        attributes.putIfAbsent(name, new TablesawEnumAttribute<E>(enumType, name));
    }

    @Override
    public void addFloat(String name) {
        attributes.putIfAbsent(name, new TablesawFloatAttribute(name));
    }

    @Override
    public void addInt(String name) {
        attributes.putIfAbsent(name, new TablesawIntAttribute(name));
    }

    @Override
    public void addShort(String name) {
        attributes.putIfAbsent(name, new TablesawShortAttribute(name));
    }

    @Override
    public void addString(String name) {
        attributes.putIfAbsent(name, new TablesawStringAttribute(name));
    }

    public Table asTable(String tableName) {
        Table table = Table.create(tableName);
        int maxRowCount = 0;

        for (Attribute value : attributes.values()) {
            TablesawAttribute attribute = asTablesawAttribute(value);
            if (attribute.capacity() > maxRowCount) {
                maxRowCount = attribute.capacity();
            }
        }

        for (Attribute value : attributes.values()) {
            TablesawAttribute attribute = asTablesawAttribute(value);

            while (maxRowCount > attribute.capacity()) attribute.addRows();
        }

        for (Attribute value : attributes.values()) {
            TablesawAttribute attribute = asTablesawAttribute(value);
            table.addColumns(attribute.column());
        }

        return table;
    }

    private TablesawAttribute asTablesawAttribute(Attribute value) {
        if (value instanceof GenericEntityListOverStringAttribute) {
            value = ((GenericEntityListOverStringAttribute) value).getWrappedAttribute();
        }
        return (TablesawAttribute) value;
    }

    @Override
    public Stream<Attribute> attributes() {
        return attributes.values().stream().map(x -> (Attribute) x);
    }

    @Override
    public int getCount() {
        return count.get();
    }
}

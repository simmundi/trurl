package pl.edu.icm.trurl.ecs.mapper;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.exampledata.pizza.Olive;
import pl.edu.icm.trurl.exampledata.pizza.Pizza;
import pl.edu.icm.trurl.exampledata.pizza.Topping;
import pl.edu.icm.trurl.store.PrefixedStore;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreMetadata;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.ByteAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.ValueObjectListAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class HandwrittenPizzaMapper implements Mapper<Pizza> {
    private Mapper olives;

    private IntAttribute olives_start;

    private ByteAttribute olives_length;

    private Mapper toppings;

    private ValueObjectListAttribute toppings_ids;

    private AtomicInteger count = new AtomicInteger();

    private Store store;

    private final MapperListeners mapperListeners = new MapperListeners();

    @Override
    public int getCount() {
        return this.count.get();
    }

    @Override
    public void setCount(int count) {
        this.count.set(count);
        ensureCapacity(count);
    }

    @Override
    public void setEmpty(int row) {
        olives.setEmpty(row);
        toppings.setEmpty(row);
    }

    @Override
    public boolean isModified(Pizza component, int row) {
        {
            int size = component.getOlives().size();
            if (olives_start.isEmpty(row) && size > 0) return true;
            int start = olives_start.getInt(row);
            for (int i = 0; i < size; i++) {
                if (olives.isModified(component.getOlives().get(i), start + i)) return true;
            }
            byte length = olives_length.getByte(row);
            if (length > size && olives.isPresent(start + size)) return true;
        }
        {
            int size = component.getToppings().size();
            if (toppings_ids.isEmpty(row) && size > 0) return true;
            //todo O(N). maybe its enough to get rough size with O(1)
            int mapped_size = toppings_ids.getSize(row);
            if (mapped_size != size) return true;
            int[] ids = new int[size];
            toppings_ids.loadIds(row, ((index, value) -> ids[index] = value));
            for (int i = 0; i < size; i++) {
                if (toppings.isModified(component.getToppings().get(i), ids[i])) return true;
            }
        }
        return false;
    }

    @Override
    public void attachStore(Store store) {
        this.store = store;
        olives.attachStore(PrefixedStore.wrap(store, "olives"));
        olives_start = (IntAttribute) store.get("olives_start");
        olives_length = (ByteAttribute) store.get("olives_length");
        toppings.attachStore(PrefixedStore.wrap(store, "toppings"));
        toppings_ids = (ValueObjectListAttribute) store.get("toppings_id");
        this.setCount(store.getCount());
    }

    @Override
    public void configureStore(StoreMetadata meta) {
        olives = Mappers.create(Olive.class);
        olives.configureStore(PrefixedStore.wrap(meta, "olives"));
        meta.addInt("olives_start");
        meta.addByte("olives_length");
        toppings = Mappers.create(Topping.class);
        toppings.configureStore(PrefixedStore.wrap(meta, "toppings"));
        meta.addValueObjectList("toppings_id");
    }

    @Override
    public Pizza create() {
        return new Pizza();
    }

    @Override
    public boolean isPresent(int row) {
        return olives.isPresent(row) || !olives_start.isEmpty(row) || !olives_length.isEmpty(row) || toppings.isPresent(row) || !toppings_ids.isEmpty(row);
    }

    @Override
    public void load(Session session, Pizza component, int row) {
        fetchValues(session, component, row);
    }

    private void fetchValues(Session session, Pizza component, int row) {
        if (!olives_start.isEmpty(row)) {
            int length = olives_length.getByte(row);
            int start = olives_start.getInt(row);
            for (int i = start; i < start + length; i++) {
                if (!olives.isPresent(i)) break;
                Olive element = (Olive) olives.create();
                olives.load(session, element, i);
                component.getOlives().add(element);
            }
        }
        if (!toppings_ids.isEmpty(row)) {
            int ids_size = toppings_ids.getSize(row);
            int[] ids = new int[ids_size];
            toppings_ids.loadIds(row, ((index, value) -> ids[index] = value));
            for (int i = 0; i < ids_size; i++) {
                if (!toppings.isPresent(ids[i])) break;
                Topping element = (Topping) toppings.create();
                toppings.load(session, element, ids[i]);
                component.getToppings().add(element);
            }
        }
    }

    @Override
    public void save(Session session, Pizza component, int row) {
        storeValues(component, row);
    }

    private void storeValues(Pizza component, int row) {
        int current = count.get();
        if (row < current && !isModified(component, row)) return;
        while (row >= current) {
            boolean ok = count.compareAndSet(current, row + 1);
            current = count.get();
            if (!ok) continue;
            ensureCapacity(current);
        }
        mapperListeners.fireSavingComponent(component, row);
        {
            int size = component.getOlives().size();
            if (size > 127) {
                throw new IllegalStateException("Embedded lists over 127 elements are not supported");
            }
            if (size > 0 && olives_start.isEmpty(row)) {
                byte sizeMin = 1;
                byte sizeMargin = 2;
                byte length = (byte) (Math.max(size, sizeMin - sizeMargin) + sizeMargin);
                olives_start.setInt(row, olives.getCount());
                olives_length.setByte(row, length);
                olives.setCount(olives.getCount() + length);
            }
            if (size > 0) {
                int length = olives_length.getByte(row);
                int start = olives_start.getInt(row);
                int end = start + size;
                if (size > length) {
                    throw new IllegalStateException("resizing this list over " + length + " is not supported");
                }
                for (int i = 0; i < size; i++) {
                    olives.save(component.getOlives().get(i), i + start);
                }
                if (size < length) {
                    olives.setEmpty(start + size);
                }
            }
        }
        {
            int size = component.getToppings().size();
//todo take care of default size min and size margin
            if(size > 0)
        }
    }

    @Override
    public void ensureCapacity(int capacity) {
        this.olives.ensureCapacity(capacity);
        this.olives_start.ensureCapacity(capacity);
        this.olives_length.ensureCapacity(capacity);
        this.toppings.ensureCapacity(capacity);
        this.toppings_ids.ensureCapacity(capacity);
    }

    @Override
    public List<Attribute> attributes() {
        List<Attribute> result = new ArrayList<Attribute>();
        result.addAll(Arrays.asList(olives_start, olives_length, toppings_ids));
        Arrays.<Mapper>asList(olives, toppings).stream().forEach(mapper -> result.addAll(mapper.attributes()));
        return result;
    }

    @Override
    public MapperListeners getMapperListeners() {
        return mapperListeners;
    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
    }
}

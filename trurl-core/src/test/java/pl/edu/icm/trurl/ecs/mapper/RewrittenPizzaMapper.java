package pl.edu.icm.trurl.ecs.mapper;

import pl.edu.icm.trurl.ecs.Counter;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.exampledata.pizza.Olive;
import pl.edu.icm.trurl.exampledata.pizza.Pizza;
import pl.edu.icm.trurl.exampledata.pizza.Topping;
import pl.edu.icm.trurl.store.join.Join;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreConfigurer;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RewrittenPizzaMapper implements Mapper<Pizza> {
    private Mapper olives;

    private Mapper toppings;

    private Counter counter;

    final Mappers mappers;

    private final MapperListeners mapperListeners = new MapperListeners();
    private Join olivesReference;
    private Join toppingsReference;

    public RewrittenPizzaMapper(Mappers mappers) {
        this.mappers = mappers;
    }


    @Override
    public boolean isModified(Pizza component, int row) {
        {
            int size = component.getOlives().size();

            for (int idx = 0; true; idx++) {
                int olivesRow = olivesReference.getRow(row, idx);
                if (idx >= size) {
                    if (olivesRow != Integer.MIN_VALUE) {
                        return true;
                    } else {
                        break;
                    }
                }
                if (olivesRow == Integer.MIN_VALUE || olives.isModified(component.getOlives().get(idx), olivesRow)) return true;
            }
        }
        {
            int size = component.getToppings().size();

            for (int idx = 0; true; idx++) {
                int toppingsRow = toppingsReference.getRow(row, idx);
                if (idx >= size) {
                    if (toppingsRow != Integer.MIN_VALUE) {
                        return true;
                    } else {
                        break;
                    }
                }
                if (toppingsRow == Integer.MIN_VALUE || toppings.isModified(component.getToppings().get(idx), toppingsRow)) return true;
            }
        }
        return false;
    }

    @Override
    public void attachStore(Store store) {
        olivesReference = store.getReference("olives");
        olives.attachStore(store.getSubstore("olives"));

        toppingsReference = store.getReference("toppings");

        toppings.attachStore(store.getSubstore("toppings"));
        this.counter = store.getCounter();
    }

    @Override
    public void configureStore(StoreConfigurer meta) {
        olives = mappers.create(Olive.class);
        StoreConfigurer olivesStore = meta.addJoin("olives").rangeTyped(1, 2).toSubstore();
        olives.configureStore(olivesStore);

        toppings = mappers.create(Topping.class);
        StoreConfigurer toppingsStore = meta.addJoin("toppings").arrayTyped(5, 2).toSubstore();
        toppings.configureStore(toppingsStore);
    }

    @Override
    public Pizza create() {
        return new Pizza();
    }

    @Override
    public boolean isPresent(int row) {
        return olivesReference.getRow(row, 0) != Integer.MIN_VALUE || toppingsReference.getRow(row, 0) != Integer.MIN_VALUE;
    }

    @Override
    public void load(Session session, Pizza component, int row) {
        fetchValues(session, component, row);
    }

    private void fetchValues(Session session, Pizza component, int row) {
        for (int i = 0; true; i++) {
            int olivesRow = olivesReference.getRow(row, i);
            if (olivesRow < 0) break;
            if (!olives.isPresent(olivesRow)) break;
            Olive element = (Olive) olives.create();
            olives.load(session, element, olivesRow);
            component.getOlives().add(element);
        }

        for (int i = 0; true; i++) {
            int toppingsRow = toppingsReference.getRow(row, i);
            if (toppingsRow < 0) break;
            if (!toppings.isPresent(toppingsRow)) break;
            Topping element = (Topping) toppings.create();
            toppings.load(session, element, toppingsRow);
            component.getToppings().add(element);
        }
    }

    @Override
    public void save(Session session, Pizza component, int row) {
        storeValues(component, row);
    }

    private void storeValues(Pizza component, int row) {
        int current = counter.getCount();
        if (row < current && !isModified(component, row)) return;

        mapperListeners.fireSavingComponent(component, row);
        {
            int size = component.getOlives().size();
            olivesReference.setSize(row, size);
            for (int idx = 0; idx < size; idx++) {
                int componentRow = olivesReference.getRow(row, idx);
                Olive element = component.getOlives().get(idx);
                olives.save(element, componentRow);
            }
        }

        {
            int size = component.getToppings().size();
            toppingsReference.setSize(row, size);
            for (int idx = 0; idx < size; idx++) {
                int componentRow = toppingsReference.getRow(row, idx);
                Topping element = component.getToppings().get(idx);
                toppings.save(element, componentRow);
            }
        }
    }

    @Override
    public List<Attribute> attributes() {
        List<Attribute> result = new ArrayList<Attribute>();
        Arrays.asList(olivesReference, toppingsReference).stream().forEach(reference -> result.addAll(reference.joiningAttributes()));
        Arrays.<Mapper>asList(olives, toppings).stream().forEach(mapper -> result.addAll(mapper.attributes()));
        return result;
    }

    @Override
    public MapperListeners getMapperListeners() {
        return mapperListeners;
    }

    public Mapper<Olive> getOlivesMapper() {
        return olives;
    }

    public Mapper<Topping> getToppingsMapper() {
        return toppings;
    }

    @Override
    public List<Mapper> getChildMappers() {
        return Arrays.asList(olives, toppings);
    }

    @Override
    public void lifecycleEvent(LifecycleEvent event) {
    }
}

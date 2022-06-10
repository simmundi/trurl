package pl.edu.icm.trurl.ecs;

import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.MapperListeners;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.Selectors;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreFactory;
import pl.edu.icm.trurl.store.StoreListener;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class Engine implements StoreListener {
    private final AtomicInteger count = new AtomicInteger();
    private final Store store;
    private final MapperSet mapperSet;
    private final Mapper<?>[] mappers;
    private final SessionFactory defaultSessionFactory;

    public Engine(StoreFactory storeFactory, MapperSet mapperSet) {
        this.store = storeFactory.create();
        this.store.addStoreListener(this);
        this.mapperSet = mapperSet;
        this.defaultSessionFactory = new SessionFactory(this, Session.Mode.NORMAL);
        mappers = this.mapperSet.streamMappers().peek(mapper -> {
            mapper.configureStore(store);
            mapper.attachStore(store);
        }).collect(toList()).toArray(new Mapper<?>[0]);
    }

    public Selector allIds() {
        return new Selectors(this).allEntities();
    }

    @Deprecated
    public Stream<Entity> streamDetached() {
        Session session = defaultSessionFactory.withModeAndCount(Session.Mode.DETACHED_ENTITIES, 0)
                .create(getCount());
        return allIds().chunks().flatMapToInt(chunk -> chunk.ids()).mapToObj(session::getEntity);
    }

    public Engine(Store store, MapperSet mapperSet) {
        this.store = store;
        this.store.addStoreListener(this);
        this.mapperSet = mapperSet;
        this.defaultSessionFactory = new SessionFactory(this, Session.Mode.NORMAL);
        this.count.set(mapperSet.streamMappers().mapToInt(m -> m.getCount()).max().getAsInt());
        mappers = this.mapperSet
                .streamMappers()
                .collect(toList())
                .toArray(new Mapper<?>[0]);
    }

    public void execute(EntitySystem system) {
        system.execute(defaultSessionFactory);
    }

    public Store getComponentStore() {
        return store;
    }

    public MapperSet getMapperSet() {
        return mapperSet;
    }

    public int getCount() {
        return count.get();
    }

    int nextId() {
        return count.getAndIncrement();
    }

    public void onUnderlyingDataChanged(int fromInclusive, int toExclusive) {
        Session session = defaultSessionFactory
                .withModeAndCount(Session.Mode.STUB_ENTITIES, 0)
                .create();

        if (toExclusive > this.count.get()) {
            this.count.set(toExclusive);
            for (Mapper mapper : mappers) {
                mapper.setCount(toExclusive);
            }
        }

        for (Mapper mapper : mappers) {
            MapperListeners mapperListeners = mapper.getMapperListeners();

            if (mapperListeners.isEmpty()) {
                continue;
            }

            Object component = mapper.create();
            for (int row = fromInclusive; row < toExclusive; row++) {
                if (mapper.isPresent(row)) {
                    mapper.load(session, component, row);
                    mapperListeners.fireSavingComponent(component, row);
                } else {
                    mapperListeners.fireSavingComponent(null, row);
                }
            }
        }
    }
}

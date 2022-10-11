package pl.edu.icm.trurl.ecs.mapper;

import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreConfigurer;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public interface Mapper<T> {

    void attachStore(Store store);
    void configureStore(StoreConfigurer metadata);
    default void configureAndAttach(Store store) {
        configureStore(store);
        attachStore(store);
    }
    T create();
    boolean isPresent(int index);
    void load(Session session, T component, int index);
    default void save(T component, int index) {
        save(null, component, index);
    }
    void save(Session owner, T component, int index);
    boolean isModified(T component, int index);
    void ensureCapacity(int row);
    List<Attribute> attributes();
    MapperListeners<T> getMapperListeners();
    int getCount();
    void setCount(int count);
    int getAndUpdateCount(int delta);
    void setEmpty(int row);
    default String name() {
        return this.create().getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }
    default T createAndLoad(int row) {
        return createAndLoad(null, row);
    }
    default T createAndLoad(Session session, int row) {
        T created = create();
        load(session, created, row);
        return created;
    }
    void lifecycleEvent(LifecycleEvent lifecycleEvent);
    default void ensureCapacityHeadroom(int headroom) {
        this.ensureCapacity(getCount() + headroom);
    }
    default List<Mapper> getChildMappers() {
        return Collections.emptyList();
    }
}

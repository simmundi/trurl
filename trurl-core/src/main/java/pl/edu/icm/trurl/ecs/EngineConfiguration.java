package pl.edu.icm.trurl.ecs;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreFactory;
import pl.edu.icm.trurl.store.array.ArrayStoreFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EngineConfiguration {
    private Engine engine;
    private Store store;
    private StoreFactory storeFactory;
    private ComponentAccessor componentAccessor;
    private List<EngineCreationListener> engineCreationListeners = new CopyOnWriteArrayList<>();
    private List<Class<?>> componentClasses = new CopyOnWriteArrayList<>();

    @WithFactory
    public EngineConfiguration() {
        this(new ArrayStoreFactory(53_000_000));
    }

    public EngineConfiguration(StoreFactory storeFactory) {
        this.storeFactory = storeFactory;
    }

    public void setStore(Store store) {
        Preconditions.checkState(this.store == null, "Store already set");
        this.store = store;
    }

    public void setStoreFactory(StoreFactory storeFactory) {
        Preconditions.checkState(this.store == null, "Store already set");
        this.storeFactory = storeFactory;
    }

    public void setComponentIndexer(ComponentAccessor componentAccessor) {
        Preconditions.checkState(this.componentAccessor == null, "ComponentIndexer already set");
        this.componentAccessor = componentAccessor;
    }

    public void addEngineCreationListeners(EngineCreationListener engineCreationListeners) {
        this.engineCreationListeners.add(engineCreationListeners);
    }

    public Engine getEngine() {
        if(engine == null) {
            engine = new Engine(storeFactory, createMapperSet());
            for (EngineCreationListener engineCreationListener : engineCreationListeners) {
                engineCreationListener.onEngineCreated(engine);
            }
        }
        return engine;
    }

    private MapperSet createMapperSet() {
        ComponentAccessor componentAccessor = getComponentIndexer();
        return new MapperSet(componentAccessor);
    }

    public void addComponentClasses(Class<?>... componentClass) {
        for (Class<?> aClass : componentClass) {
            componentClasses.add(aClass);
        }
    }

    private ComponentAccessor getComponentIndexer() {
        if (componentAccessor == null) {
            componentAccessor = new DynamicComponentAccessor(componentClasses.toArray(new Class[0]));
        }
        // loop for fast failing if the indexer doesn't support any of the components
        for (Class<?> componentClass : componentClasses) {
            componentAccessor.classToIndex(componentClass);
        }
        return componentAccessor;
    }

}

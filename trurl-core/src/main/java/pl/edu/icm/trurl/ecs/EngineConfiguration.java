package pl.edu.icm.trurl.ecs;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;
import pl.edu.icm.trurl.store.StoreFactory;
import pl.edu.icm.trurl.store.array.ArrayStoreFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EngineConfiguration {
    private volatile Engine engine;
    private StoreFactory storeFactory;
    private ComponentAccessor componentAccessor;
    private int initialCapacity = Integer.MIN_VALUE;
    private int capacityHeadroom = Integer.MIN_VALUE;
    private boolean sharedSession;
    private List<EngineCreationListener> engineCreationListeners = new CopyOnWriteArrayList<>();
    private List<Class<?>> componentClasses = new CopyOnWriteArrayList<>();

    @WithFactory
    public EngineConfiguration() {

    }

    public void setStoreFactory(StoreFactory storeFactory) {
        preconditionEngineNotCreated();
        this.storeFactory = storeFactory;
    }

    public void setComponentIndexer(ComponentAccessor componentAccessor) {
        preconditionEngineNotCreated();

        Preconditions.checkState(this.componentAccessor == null, "ComponentIndexer already set");
        this.componentAccessor = componentAccessor;
    }

    public void setSharedSession(boolean shared) {
        preconditionEngineNotCreated();
        this.sharedSession = shared;
    }

    public void addEngineCreationListeners(EngineCreationListener engineCreationListeners) {
        preconditionEngineNotCreated();
        this.engineCreationListeners.add(engineCreationListeners);
    }

    public Engine getEngine() {
        if (engine == null) {
            engine = new Engine(getStoreFactory(), getInitialCapacity(), getCapacityHeadroom(), getMapperSet(), sharedSession);
            for (EngineCreationListener engineCreationListener : engineCreationListeners) {
                engineCreationListener.onEngineCreated(engine);
            }
        }
        return engine;
    }

    public void addComponentClasses(Class<?>... componentClass) {
        preconditionEngineNotCreated();
        for (Class<?> aClass : componentClass) {
            componentClasses.add(aClass);
        }
    }

    public void setInitialCapacity(int initialCapacity) {
        preconditionEngineNotCreated();
        this.initialCapacity = initialCapacity;
    }

    public void setCapacityHeadroom(int capacityHeadroom) {
        preconditionEngineNotCreated();
        this.capacityHeadroom = capacityHeadroom;
    }

    private int getCapacityHeadroom() {
        if (capacityHeadroom == Integer.MIN_VALUE) {
            capacityHeadroom = (int) Math.ceil(initialCapacity / 10f);
        }
        return capacityHeadroom;
    }

    private int getInitialCapacity() {
        if (initialCapacity == Integer.MIN_VALUE) {
            initialCapacity = getStoreFactory().defaultInitialCapacity();
        }
        return initialCapacity;
    }

    private StoreFactory getStoreFactory() {
        if (storeFactory == null) {
            storeFactory = new ArrayStoreFactory();
        }
        return storeFactory;
    }

    private MapperSet getMapperSet() {
        preconditionEngineNotCreated();
        ComponentAccessor componentAccessor = getComponentIndexer();
        return new MapperSet(componentAccessor);
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

    private void preconditionEngineNotCreated() {
        Preconditions.checkState(this.engine == null, "Engine already created set");
    }

}

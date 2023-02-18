/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.ecs;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.ecs.mapper.MappersFactory;
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
    private final Bento bento;

    @WithFactory
    public EngineConfiguration(Bento bento) {
        this.bento = bento;
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
        return new MapperSet(componentAccessor, bento.get(MappersFactory.IT));
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

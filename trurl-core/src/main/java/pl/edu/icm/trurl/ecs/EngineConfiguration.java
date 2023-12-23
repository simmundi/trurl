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
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.dao.DaosFactory;
import pl.edu.icm.trurl.store.attribute.AttributeFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EngineConfiguration {
    private final int sessionCacheSize;
    private volatile Engine engine;
    private ComponentAccessorCreator componentAccessorCreator;
    private List<EngineCreationListener> engineCreationListeners = new CopyOnWriteArrayList<>();
    private Set<Class<?>> componentClasses = new LinkedHashSet<>();
    private final AttributeFactory attributeFactory;
    private final Bento bento;
    private final int initialCapacity;
    private final int capacityHeadroom;

    @WithFactory
    public EngineConfiguration(ComponentAccessorCreator componentAccessorCreator,
                               @ByName(value = "trurl.engine.initial-capacity", fallbackValue = "200000") int initialCapacity,
                               @ByName(value = "trurl.engine.capacity-headroom", fallbackValue = "128") int capacityHeadroom,
                               @ByName(value = "trurl.engine.session-cache-size", fallbackValue = "20000") int sessionCacheSize,
                               AttributeFactory attributeFactory,
                               Bento bento) {
        this.componentAccessorCreator = componentAccessorCreator;
        this.initialCapacity = initialCapacity;
        this.capacityHeadroom = capacityHeadroom;
        this.attributeFactory = attributeFactory;
        this.sessionCacheSize = sessionCacheSize;
        this.bento = bento;
    }

    public void addEngineCreationListener(EngineCreationListener engineCreationListeners) {
        preconditionEngineNotCreated();
        this.engineCreationListeners.add(engineCreationListeners);
    }

    public Engine getEngine() {
        if (engine == null) {
            engine = new Engine(initialCapacity, capacityHeadroom, getDaoManager(), attributeFactory, sessionCacheSize);
            for (EngineCreationListener engineCreationListener : engineCreationListeners) {
                engineCreationListener.onEngineCreated(engine);
            }
        }
        return engine;
    }

    public void addComponentClasses(Class<?>... componentClass) {
        preconditionEngineNotCreated();
        componentClasses.addAll(Arrays.asList(componentClass));
    }


    private DaoManager getDaoManager() {
        preconditionEngineNotCreated();
        ComponentAccessor componentAccessor = getComponentIndexer();
        return new DaoManager(componentAccessor, bento.get(DaosFactory.IT));
    }

    private ComponentAccessor getComponentIndexer() {
        ComponentAccessor componentAccessor = componentAccessorCreator.create(componentClasses);
        verifyComponentAccessor(componentAccessor);
        return componentAccessor;
    }

    private void preconditionEngineNotCreated() {
        Preconditions.checkState(this.engine == null, "Engine already created set");
    }

    private void verifyComponentAccessor(ComponentAccessor componentAccessor) {
        // loop for fast failing if the indexer doesn't support any of the components
        Map<Integer, Class<?>> map = new HashMap<>(componentClasses.size());
        for (Class<?> componentClass : componentClasses) {
            map.put(componentAccessor.classToIndex(componentClass), componentClass);
        }
        if (!new HashSet<>(map.values()).equals(new HashSet<>(componentClasses))) {
            throw new IllegalStateException("Some components were not mapped by " + componentAccessor);
        }
        for (Integer index : map.keySet()) {
            if (map.get(index) != componentAccessor.indexToClass(index)) {
                throw new IllegalStateException("Component " + map.get(index) + " was not mapped correctly by " + componentAccessor);
            }
        }
    }
}

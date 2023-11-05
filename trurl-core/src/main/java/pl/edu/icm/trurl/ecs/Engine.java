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

import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.AttributeFactory;

import static java.util.stream.Collectors.toList;

public final class Engine {
    private final Store rootStore;
    private final MapperSet mapperSet;
    private final Mapper<?>[] mappers;
    private final SessionFactory defaultSessionFactory;
    private int capacityHeadroom;

    public Engine(int initialCapacity, int capacityHeadroom, MapperSet mapperSet, boolean shared, AttributeFactory attributeFactory) {
        this.rootStore = new Store(attributeFactory, initialCapacity + capacityHeadroom);
        this.capacityHeadroom = capacityHeadroom;
        this.mapperSet = mapperSet;
        if (shared) {
            this.defaultSessionFactory = new SessionFactory(this, Session.Mode.SHARED, initialCapacity + capacityHeadroom);
        } else {
            this.defaultSessionFactory = new SessionFactory(this, Session.Mode.NORMAL);
        }
        mappers = this.mapperSet.streamMappers().peek(mapper -> {
            mapper.configureStore(rootStore);
            mapper.attachStore(rootStore);
        }).collect(toList()).toArray(new Mapper<?>[0]);
    }

    public Engine(Store store, int capacityHeadroom, MapperSet mapperSet, boolean shared) {
        this.rootStore = store;
        this.capacityHeadroom = capacityHeadroom;
        this.mapperSet = mapperSet;
        this.defaultSessionFactory = new SessionFactory(this, shared ? Session.Mode.SHARED : Session.Mode.NORMAL);
        mappers = this.mapperSet
                .streamMappers()
                .collect(toList())
                .toArray(new Mapper<?>[0]);
    }

    public void execute(EntitySystem system) {
        ensureHeadroom();
        system.execute(defaultSessionFactory);
    }

    public Store getRootStore() {
        return rootStore;
    }

    public MapperSet getMapperSet() {
        return mapperSet;
    }

    public int getCount() {
        return rootStore.getCounter().getCount();
    }

    int nextId() {
        return rootStore.getCounter().next();
    }

    private void ensureHeadroom() {
        int targetCapacity = rootStore.getCounter().getCount() + capacityHeadroom;
        rootStore.ensureCapacity(targetCapacity);
    }
}

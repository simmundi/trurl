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

import pl.edu.icm.trurl.ecs.entity.IdentityMapSession;
import pl.edu.icm.trurl.ecs.entity.Session;
import pl.edu.icm.trurl.ecs.entity.SessionFactory;
import pl.edu.icm.trurl.ecs.mapper.LifecycleEvent;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.AttributeFactory;

public final class Engine {
    private final Store rootStore;
    private final MapperSet mapperSet;
    private int capacityHeadroom;

    public Engine(int initialCapacity, int capacityHeadroom, MapperSet mapperSet, AttributeFactory attributeFactory, int sessionCapacity, boolean clearByDefault) {
        this.rootStore = new Store(attributeFactory, initialCapacity + capacityHeadroom);
        this.capacityHeadroom = capacityHeadroom;
        this.mapperSet = mapperSet;
        this.mapperSet.streamMappers().forEach(mapper -> {
            mapper.configureStore(rootStore);
            mapper.attachStore(rootStore);
        });
        this.sessionFactory = new SessionFactory() {
            private final ThreadLocal<Session> sessionThreadLocal = ThreadLocal.withInitial(() -> new IdentityMapSession(Engine.this, sessionCapacity));

            @Override
            public Session getSession() {
                return sessionThreadLocal.get();
            }

            @Override
            public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
                mapperSet.streamMappers().forEach(mapper -> mapper.lifecycleEvent(lifecycleEvent));
            }

            @Override
            public boolean shouldClearByDefault() {
                return clearByDefault;
            }
        };
    }

    private final SessionFactory sessionFactory;

    public void execute(EntitySystem system) {
        ensureHeadroom();
        system.execute(sessionFactory);
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

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

import pl.edu.icm.trurl.ecs.dao.Dao;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.AttributeFactory;

public final class Engine {
    private final int capacityHeadroom;
    private final Store rootStore;
    private final DaoManager daoManager;
    private final SessionFactory sessionFactory;

    public Engine(int initialCapacity, int capacityHeadroom, DaoManager daoManager, AttributeFactory attributeFactory, int sessionCacheCapacity) {
        this.rootStore = new Store(attributeFactory, initialCapacity + capacityHeadroom);
        this.capacityHeadroom = capacityHeadroom;
        this.daoManager = daoManager;
        this.sessionFactory = new SessionFactory(this, sessionCacheCapacity);
        for (Dao<?> dao : daoManager.allDaos()) {
            dao.configureStore(rootStore);
            dao.attachStore(rootStore);
        }
    }

    public Engine(Store store, int capacityHeadroom, DaoManager daoManager, int sessionCacheCapacity) {
        this.rootStore = store;
        this.capacityHeadroom = capacityHeadroom;
        this.daoManager = daoManager;
        this.sessionFactory = new SessionFactory(this, sessionCacheCapacity);
    }

    public void execute(EntitySystem system) {
        ensureHeadroom();
        system.execute(sessionFactory);
    }

    public Store getRootStore() {
        return rootStore;
    }

    public DaoManager getDaoManager() {
        return daoManager;
    }

    public int getCount() {
        return rootStore.getCounter().getCount();
    }

    int allocateNextId() {
        return rootStore.getCounter().next();
    }

    private void ensureHeadroom() {
        int targetCapacity = rootStore.getCounter().getCount() + capacityHeadroom;
        rootStore.ensureCapacity(targetCapacity);
    }
}

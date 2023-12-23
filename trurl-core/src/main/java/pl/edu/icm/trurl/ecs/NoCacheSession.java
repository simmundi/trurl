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

import pl.edu.icm.trurl.ecs.dao.ComponentOwner;

public final class NoCacheSession extends AbstractSession implements ComponentOwner {
    private final Engine engine;
    private final int ownerId;

    NoCacheSession(Engine engine, int ownerId) {
        this.engine = engine;
        this.ownerId = ownerId;
    }

    @Override
    public DetachedEntity getEntity(long id) {
        return new DetachedEntity(engine.getDaoManager(), this, id);
    }

    @Override
    public DetachedEntity createEntity(Object... components) {
        DetachedEntity entity = getEntity(engine.allocateNextId());
        for (Object component : components) {
            entity.add(component);
        }
        return entity;
    }

    public void deleteEntity(DetachedEntity entity) {
        engine.getRootStore().free((int) entity.getId());
    }

    public Engine getEngine() {
        return engine;
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }
}

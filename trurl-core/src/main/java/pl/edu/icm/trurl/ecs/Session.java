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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import pl.edu.icm.trurl.ecs.mapper.ComponentOwner;

public final class Session implements ComponentOwner {
    private final Engine engine;
    private final Int2ObjectMap<Entity> entities;

    private final boolean detachedEntities;
    private final boolean createStubEntities;
    private final boolean persist;

    private final int ownerId;

    Session(Engine engine, int expectedEntityCount, Mode mode, int ownerId) {
        this.engine = engine;
        this.createStubEntities = mode == Mode.STUB_ENTITIES;
        this.detachedEntities = mode == Mode.DETACHED_ENTITIES;
        this.persist = (mode == Mode.NORMAL || mode == Mode.SHARED);
        this.entities = (detachedEntities || expectedEntityCount == 0) ? null : new Int2ObjectOpenHashMap<>(expectedEntityCount);
        this.ownerId = ownerId;
    }

    public void close() {
        if (persist) {
            entities.values().stream().forEach(Entity::persist);
        }
    }

    public Entity getEntity(int id) {
        if (createStubEntities) {
            return new Entity(id);
        } else if (detachedEntities) {
            return new Entity(engine.getMapperSet(), this, id);
        } else {
            return entities
                    .computeIfAbsent(id, newId ->
                            new Entity(engine.getMapperSet(), this, newId));
        }
    }

    public Entity createEntity(Object... components) {
        Entity entity = getEntity(engine.nextId());
        for (Object component : components) {
            entity.add(component);
        }
        return entity;
    }

    public void removeEntity(Entity entity) {
        if (detachedEntities) {
            throw new IllegalStateException("Cannot remove entity from detached session");
        }
        entities.remove(entity.getId());
        engine.getRootStore().free(entity.getId());
    }

    public Engine getEngine() {
        return engine;
    }

    public int getCount() {
        return entities == null ? 0 : entities.size();
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    public enum Mode {
        NORMAL,
        STUB_ENTITIES,
        DETACHED_ENTITIES,
        NO_PERSIST,
        SHARED
    }
}

/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.ecs.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.mapper.Mapper;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class IdentityMapSession extends Session {
    private final Engine engine;
    private final Int2ObjectOpenHashMap<Entity> entities;
    private final IntList entitiesIds = new IntArrayList();
    private final Object[][] components;
    private int ownerId;
    private Mapper[] persistingMappers;

    IdentityMapSession(Engine engine, int expectedSize) {
        this.engine = engine;
        this.entities = new Int2ObjectOpenHashMap<>(expectedSize);
        components = new Object[engine.getMapperSet().componentCount()][];
        for (int i = 0; i < components.length; i++) {
            components[i] = new Object[1024];
        }
        persistingAll();
    }

    <T> T readComponent(int cix, int eix) {
        return eix < components[cix].length ? (T) components[cix][eix] : null;
    }

    void writeComponent(int cix, int eix, Object component) {
        if (eix >= components[cix].length) {
            components[cix] = Arrays.copyOf(components[cix], newLength(eix, components[cix].length));
        }
        components[cix][eix] = component;
    }

    private int newLength(int eix, int length) {
        while (eix >= length) {
            length *= 2;
        }
        return length;
    }

    @Override
    public void persistingAll() {
        persistingMappers = engine.getMapperSet().streamMappers().collect(Collectors.toList()).toArray(new Mapper[]{});
    }

    @Override
    public void persistingExactly(Class<?>... components) {
        persistingMappers = Arrays.stream(components).map(c -> engine.getMapperSet().classToMapper(c)).collect(Collectors.toList()).toArray(new Mapper[]{});
    }

    @Override
    public void persist() {
        for (int mapperIdx = 0; mapperIdx < persistingMappers.length; mapperIdx++) {
            Mapper persistingMapper = persistingMappers[mapperIdx];
            for (int idx = 0; idx < components[mapperIdx].length; idx++) {
                int id = entitiesIds.getInt(idx);
                if (id != Integer.MIN_VALUE && components[mapperIdx][idx] == null) {
                    continue;
                }
                persistingMapper.save(this, components[mapperIdx][idx], id);
            }
        }
    }

    @Override
    public void clear() {
        entitiesIds.clear();
        entities.clear();
        for (int i = 0; i < components.length; i++) {
            Arrays.fill(components[i], null);
        }
    }

    @Override
    public void close() {
        persist();
        clear();
    }

    @Override
    public Entity getEntity(int id) {
            return entities
                    .computeIfAbsent(id, newId -> {
                        int eix = entitiesIds.size();
                        entitiesIds.add(newId);
                        return new IdentityMapEntity(this, newId, eix);
                    });
        }

    @Override
    public Entity createEntity(Object... components) {
        Entity entity = getEntity(engine.nextId());
        for (Object component : components) {
            entity.add(component);
        }
        return entity;
    }

    @Override
    public void removeEntity(IdentityMapEntity entity) {
        entities.remove(entity.getId());
        entitiesIds.set(entity.eix, Integer.MIN_VALUE);
        engine.getRootStore().free(entity.getId());
    }

    @Override
    public Engine getEngine() {
        return engine;
    }

    @Override
    public int getCount() {
        return entities == null ? 0 : entities.size();
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}

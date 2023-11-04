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

import java.util.Objects;
import java.util.Optional;

public final class Entity {
    public static final int NULL_ID = Integer.MIN_VALUE;

    private final MapperSet mapperSet;
    private final Session session;
    private final int id;
    private final Object[] components;

    /**
     * Creates a stub entity (basically, a type-safe id wrapper)
     */
    public Entity(int id) {
        this.mapperSet = null;
        this.session = null;
        this.components = null;
        this.id = id;
    }

    public Entity(MapperSet mapperSet, Session session, int id) {
        this.mapperSet = mapperSet;
        this.session = session;
        this.components = new Object[mapperSet.componentCount()];
        this.id = id;
    }

    public <T> T get(Class<T> componentClass) {
        return get(componentClass, false);
    }

    public <T> T getOrCreate(Class<T> componentClass) {
        return get(componentClass, true);
    }

    public <T> T add(T component) {
        int idx = mapperSet.classToIndex(component.getClass());
        components[idx] = component;
        return component;
    }

    public void persist() {
        for (int idx = 0; idx < components.length; idx++) {
            if (components[idx] != null) {
                Mapper<Object> mapper = mapperSet.indexToMapper(idx);
                mapper.save(session, components[idx], id);
            }
        }
    }

    public int getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    public <T> Optional<T> optional(Class<T> classToken) {
        return Optional.ofNullable(get(classToken));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private <T> T get(Class<T> componentClass, boolean createIfDoesntExist) {
        int idx = mapperSet.classToIndex(componentClass);
        if (components[idx] == null) {
            Mapper<T> mapper = mapperSet.classToMapper(componentClass);
            if (mapper.isPresent(id)) {
                components[idx] = mapper.createAndLoad(session, id);
            } else if (createIfDoesntExist) {
                components[idx] = mapper.create();
            }
        }
        return (T) components[idx];
    }

    public <T> T get(ComponentToken<T> token) {
        if (components[token.index] == null && token.mapper.isPresent(id)) {
            components[token.index] = token.mapper.createAndLoad(session, id);
        }
        return (T) components[token.index];
    }
}

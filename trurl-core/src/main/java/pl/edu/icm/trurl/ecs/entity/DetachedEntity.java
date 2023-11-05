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

import pl.edu.icm.trurl.ecs.mapper.Mapper;

import java.util.Objects;

final class DetachedEntity extends Entity {
    private final DetachedSession session;
    private final int id;
    private final Object[] components;

    public DetachedEntity(DetachedSession session, int id) {
        this.session = session;
        this.components = new Object[session.getEngine().getMapperSet().componentCount()];
        this.id = id;
    }

    public <T> T get(Class<T> componentClass) {
        return get(componentClass, false);
    }

    public <T> T getOrCreate(Class<T> componentClass) {
        return get(componentClass, true);
    }

    public <T> T add(T component) {
        int idx = session.getEngine().getMapperSet().classToIndex(component.getClass());
        components[idx] = component;
        return component;
    }

    public int getId() {
        return id;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetachedEntity entity = (DetachedEntity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private <T> T get(Class<T> componentClass, boolean createIfDoesntExist) {
        int idx = session.getEngine().getMapperSet().classToIndex(componentClass);
        if (components[idx] == null) {
            Mapper<T> mapper = session.getEngine().getMapperSet().classToMapper(componentClass);
            if (mapper.isPresent(id)) {
                components[idx] = mapper.createAndLoad(session, id);
            } else if (createIfDoesntExist) {
                components[idx] = mapper.create();
            }
        }
        return (T) components[idx];
    }

    public <T> T get(ComponentToken<T> token) {
        if (components[token.cix] == null && token.mapper.isPresent(id)) {
            components[token.cix] = token.mapper.createAndLoad(session, id);
        }
        return (T) components[token.cix];
    }
}

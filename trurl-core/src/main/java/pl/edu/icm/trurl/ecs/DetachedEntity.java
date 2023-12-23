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

import java.util.Objects;
import java.util.Optional;

public final class DetachedEntity extends AnyEntity {
    private final DaoManager daoManager;
    private final NoCacheSession noCacheSession;
    private final int id;
    private final Object[] components;

    DetachedEntity(DaoManager daoManager, NoCacheSession noCacheSession, long id) {
        this.daoManager = daoManager;
        this.noCacheSession = noCacheSession;
        this.id = (int) id;
        this.components = new Object[daoManager.componentCount()];
    }

    @Override
    public <T> T get(Class<T> componentClass) {
        return get(componentClass, false);
    }

    @Override
    public <T> T get(ComponentToken<T> token) {
        return get(token, false);
    }

    @Override
    public <T> T getOrCreate(Class<T> componentClass) {
        return get(componentClass, true);
    }

    @Override
    public <T> T getOrCreate(ComponentToken<T> token) {
        return get(token, true);
    }

    @Override
    public <T> T add(T component) {
        ComponentToken<T> componentToken = (ComponentToken<T>) daoManager.classToToken(component.getClass());
        return add(componentToken, component);
    }

    @Override
    public <T> T add(ComponentToken<T> token, T component) {
        components[token.index] = component;
        return component;
    }

    public void persist() {
        for (int idx = 0; idx < components.length; idx++) {
            if (components[idx] != null) {
                Dao<Object> dao = daoManager.indexToMapper(idx);
                dao.save(noCacheSession, components[idx], id);
            }
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public NoCacheSession getSession() {
        return noCacheSession;
    }

    public <T> Optional<T> optional(Class<T> classToken) {
        return Optional.ofNullable(get(classToken));
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
        int idx = daoManager.classToIndex(componentClass);
        if (components[idx] == null) {
            Dao<T> dao = daoManager.classToMapper(componentClass);
            if (dao.isPresent(id)) {
                components[idx] = dao.createAndLoad(noCacheSession, id);
            } else if (createIfDoesntExist) {
                components[idx] = dao.create();
            }
        }
        return (T) components[idx];
    }


    private <T> T get(ComponentToken<T> token, boolean createIfDoesntExist) {
        if (components[token.index] == null) {
            if (token.dao.isPresent(id)) {
                components[token.index] = token.dao.createAndLoad(id);
            } else if (createIfDoesntExist) {
                components[token.index] = token.dao.create();
            }
        }
        return (T) components[token.index];
    }
}

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

final class IdentityMapEntity extends Entity {
    private final IdentityMapSession session;
    private final int id;
    final int eix;

    public IdentityMapEntity(IdentityMapSession session, int id, int eix) {
        this.session = session;
        this.id = id;
        this.eix = eix;
    }

    @Override
    public <T> T get(Class<T> componentClass) {
        return get(componentClass, false);
    }

    @Override
    public <T> T getOrCreate(Class<T> componentClass) {
        return get(componentClass, true);
    }

    @Override
    public <T> T add(T component) {
        int cix = session.getEngine().getMapperSet().classToIndex(component.getClass());
        session.writeComponent(cix, eix, component);
        return component;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentityMapEntity entity = (IdentityMapEntity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private <T> T get(Class<T> componentClass, boolean createIfDoesntExist) {
        int cix = this.session.getEngine().getMapperSet().classToIndex(componentClass);
        T preFetched = this.session.readComponent(cix, eix);
        if (preFetched != null) {
            return preFetched;
        } else {
            Mapper<T> mapper = this.session.getEngine().getMapperSet().indexToMapper(cix);
            if (mapper.isPresent(id)) {
                T justFetched = mapper.createAndLoad(session, id);
                this.session.writeComponent(cix, eix, justFetched);
                return justFetched;
            } else if (createIfDoesntExist) {
                T justCreated = mapper.create();
                this.session.writeComponent(cix, eix, justCreated);
                return justCreated;
            } else {
                return null;
            }
        }
    }

    @Override
    public <T> T get(ComponentToken<T> token) {
        if (this.session.readComponent(token.cix, eix) == null) {
            if (token.mapper.isPresent(id)) {
                T justFetched = token.mapper.createAndLoad(session, id);
                this.session.writeComponent(token.cix, eix, justFetched);
                return justFetched;
            } else {
                return null;
            }
        }
        return this.session.readComponent(token.cix, eix);
    }
}

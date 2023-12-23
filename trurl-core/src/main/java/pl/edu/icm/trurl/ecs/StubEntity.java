/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import java.util.Objects;

public class StubEntity extends AnyEntity {
    private final long id;

    public StubEntity(long id) {
        this.id = id;
    }

    @Override
    public <T> T get(Class<T> componentClass) {
        throw new UnsupportedOperationException("Stub entity");
    }

    @Override
    public <T> T get(ComponentToken<T> token) {
        throw new UnsupportedOperationException("Stub entity");
    }

    @Override
    public <T> T getOrCreate(Class<T> componentClass) {
        throw new UnsupportedOperationException("Stub entity");
    }

    @Override
    public <T> T getOrCreate(ComponentToken<T> componentClass) {
        throw new UnsupportedOperationException("Stub entity");
    }

    @Override
    public <T> T add(T component) {
        throw new UnsupportedOperationException("Stub entity");
    }

    @Override
    public <T> T add(ComponentToken<T> token, T component) {
        throw new UnsupportedOperationException("Stub entity");
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public AbstractSession getSession() {
        throw new UnsupportedOperationException("Stub entity");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubEntity that = (StubEntity) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

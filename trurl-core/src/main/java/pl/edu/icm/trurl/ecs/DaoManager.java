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

import net.snowyhollows.bento.BentoFactory;
import pl.edu.icm.trurl.ecs.dao.Dao;
import pl.edu.icm.trurl.ecs.dao.DaoProducer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class DaoManager {
    private final ComponentAccessor componentAccessor;
    private final Dao[] daos;
    private final ComponentToken[] tokens;

    public DaoManager(ComponentAccessor componentAccessor, Map<Class<?>, BentoFactory<?>> factories, DaoProducer daoProducer) {
        this.componentAccessor = componentAccessor;
        this.daos = new Dao[componentAccessor.componentCount()];
        tokens = new ComponentToken[componentAccessor.componentCount()];
        for (int idx = 0; idx < componentCount(); idx++) {
            BentoFactory factory = factories.get(componentAccessor.indexToClass(idx));
            this.daos[idx] = daoProducer.createDao(factory);
            tokens[idx] = new ComponentToken<>(this.daos[idx], idx);
        }
    }

    public <T> Dao<T> classToDao(Class<T> componentClass) {
        return daos[componentAccessor.classToIndex(componentClass)];
    }

    public <T> Dao<T> indexToDao(int componentIndex) {
        return daos[componentIndex];
    }

    public int classToIndex(Class<?> componentClass) {
        return componentAccessor.classToIndex(componentClass);
    }

    public int componentCount() {
        return componentAccessor.componentCount();
    }

    public List<Dao<?>> getAllDaos() {
        return Arrays.asList(daos);
    }

    public <T> ComponentToken<T> classToToken(Class<T> componentClass) {
        return tokens[componentAccessor.classToIndex(componentClass)];
    }

    public <T> Dao<T> tokenToDao(ComponentToken<T> token) {
        return token.dao;
    }

    ComponentToken[] allTokens() {
        return tokens;
    }
}

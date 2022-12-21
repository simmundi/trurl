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

package pl.edu.icm.trurl.ecs.index;

import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineCreationListener;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.MapperListener;

public abstract class ComponentIndex<T> implements MapperListener<T>, EngineCreationListener {

    private final Class<T> classToken;
    private Mapper<T> mapper;

    public ComponentIndex(EngineConfiguration engineConfiguration, Class<T> clazz) {
        this.classToken = clazz;
        engineConfiguration.addEngineCreationListeners(this);
    }

    @Override
    public final void onEngineCreated(Engine engine) {
        mapper = engine.getMapperSet().classToMapper(classToken);
        mapper.getMapperListeners().addSavingListener(this);
        afterOnEngineCreated(engine, mapper);
    }

    public void afterOnEngineCreated(Engine engine, Mapper<T> mapper) {

    }
}

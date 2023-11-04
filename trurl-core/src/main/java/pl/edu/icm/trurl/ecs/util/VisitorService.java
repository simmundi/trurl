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

package pl.edu.icm.trurl.ecs.util;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.ComponentToken;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

public class VisitorService {
    private final EngineConfiguration engineConfiguration;

    @WithFactory
    public VisitorService(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
    }

    public <Context, Q> Visit<Context> withComponent(Class<Q> q, Systems.OneComponentSystem<Q> system) {
        return new Visit<Context>() {
            ComponentToken<Q> qToken;
            @Override
            public void init() {
                 qToken = engineConfiguration.getEngine().getMapperSet().classToToken(q);
            }

            @Override
            public void perform(Context context, Session session, int idx) {
                if (!qToken.mapper.isPresent(idx)) return;
                Entity e = session.getEntity(idx);
                Q qComponent = e.get(qToken);
                system.execute(e, qComponent);
            }
        };
    }

    public <Context, Q, W> Visit<Context> withComponents(Class<Q> q, Class<W> w, Systems.TwoComponentSystem<Q, W> system) {
        return new Visit<Context>() {
            ComponentToken<Q> qToken;
            ComponentToken<W> wToken;
            @Override
            public void init() {
                qToken = engineConfiguration.getEngine().getMapperSet().classToToken(q);
                wToken = engineConfiguration.getEngine().getMapperSet().classToToken(w);
            }

            @Override
            public void perform(Context context, Session session, int idx) {
                if (!qToken.mapper.isPresent(idx) || !wToken.mapper.isPresent(idx)) return;
                Entity e = session.getEntity(idx);
                Q qComponent = e.get(qToken);
                W wComponent = e.get(wToken);
                system.execute(e, qComponent, wComponent);
            }
        };
    }

    public <Context, Q, W, E> Visit<Context> withComponents(Class<Q> q, Class<W> w, Class<E> e, Systems.ThreeComponentSystem<Q, W, E> system) {
        return new Visit<Context>() {
            ComponentToken<Q> qToken;
            ComponentToken<W> wToken;
            ComponentToken<E> eToken;
            @Override
            public void init() {
                qToken = engineConfiguration.getEngine().getMapperSet().classToToken(q);
                wToken = engineConfiguration.getEngine().getMapperSet().classToToken(w);
                eToken = engineConfiguration.getEngine().getMapperSet().classToToken(e);
            }

            @Override
            public void perform(Context context, Session session, int idx) {
                if (!qToken.mapper.isPresent(idx) || !wToken.mapper.isPresent(idx) || !eToken.mapper.isPresent(idx)) return;
                Entity entity = session.getEntity(idx);
                Q qComponent = entity.get(qToken);
                W wComponent = entity.get(wToken);
                E eComponent = entity.get(eToken);
                system.execute(entity, qComponent, wComponent, eComponent);
            }
        };
    }

    public <Context, Q, W, E, R> Visit<Context> withComponents(Class<Q> q, Class<W> w, Class<E> e, Class<R> r, Systems.FourComponentSystem<Q, W, E, R> system) {
        return new Visit<Context>() {
            ComponentToken<Q> qToken;
            ComponentToken<W> wToken;
            ComponentToken<E> eToken;
            ComponentToken<R> rToken;
            @Override
            public void init() {
                qToken = engineConfiguration.getEngine().getMapperSet().classToToken(q);
                wToken = engineConfiguration.getEngine().getMapperSet().classToToken(w);
                eToken = engineConfiguration.getEngine().getMapperSet().classToToken(e);
                rToken = engineConfiguration.getEngine().getMapperSet().classToToken(r);
            }

            @Override
            public void perform(Context context, Session session, int idx) {
                if (!qToken.mapper.isPresent(idx) || !wToken.mapper.isPresent(idx) || !eToken.mapper.isPresent(idx) || !rToken.mapper.isPresent(idx)) return;
                Entity entity = session.getEntity(idx);
                Q qComponent = entity.get(qToken);
                W wComponent = entity.get(wToken);
                E eComponent = entity.get(eToken);
                R rComponent = entity.get(rToken);
                system.execute(entity, qComponent, wComponent, eComponent, rComponent);
            }
        };
    }
}

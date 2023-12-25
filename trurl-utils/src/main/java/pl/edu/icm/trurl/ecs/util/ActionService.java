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
import pl.edu.icm.trurl.ecs.*;
import pl.edu.icm.trurl.util.Systems;

public class ActionService {
    private final EngineConfiguration engineConfiguration;

    @WithFactory
    public ActionService(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
    }

    public <Context, Q> Action<Context> withComponent(Class<Q> q, Systems.OneComponentSystem<Q> system) {
        return new Action<Context>() {
            ComponentToken<Q> qToken;
            @Override
            public void init() {
                 qToken = engineConfiguration.getEngine().getDaoManager().classToToken(q);
            }

            @Override
            public void perform(Context context, Session session, int idx) {
                if (!qToken.dao.isPresent(idx)) return;
                Entity e = session.getEntity(idx);
                Q qComponent = e.get(qToken);
                system.execute(e, qComponent);
            }
        };
    }

    public <Context, Q, W> Action<Context> withComponents(Class<Q> q, Class<W> w, Systems.TwoComponentSystem<Q, W> system) {
        return new Action<Context>() {
            ComponentToken<Q> qToken;
            ComponentToken<W> wToken;
            @Override
            public void init() {
                qToken = engineConfiguration.getEngine().getDaoManager().classToToken(q);
                wToken = engineConfiguration.getEngine().getDaoManager().classToToken(w);
            }

            @Override
            public void perform(Context context, Session session, int idx) {
                if (!qToken.dao.isPresent(idx) || !wToken.dao.isPresent(idx)) return;
                Entity e = session.getEntity(idx);
                Q qComponent = e.get(qToken);
                W wComponent = e.get(wToken);
                system.execute(e, qComponent, wComponent);
            }
        };
    }

    public <Context, Q, W, E> Action<Context> withComponents(Class<Q> q, Class<W> w, Class<E> e, Systems.ThreeComponentSystem<Q, W, E> system) {
        return new Action<Context>() {
            ComponentToken<Q> qToken;
            ComponentToken<W> wToken;
            ComponentToken<E> eToken;
            @Override
            public void init() {
                qToken = engineConfiguration.getEngine().getDaoManager().classToToken(q);
                wToken = engineConfiguration.getEngine().getDaoManager().classToToken(w);
                eToken = engineConfiguration.getEngine().getDaoManager().classToToken(e);
            }

            @Override
            public void perform(Context context, Session session, int idx) {
                if (!qToken.dao.isPresent(idx) || !wToken.dao.isPresent(idx) || !eToken.dao.isPresent(idx)) return;
                Entity e = session.getEntity(idx);
                Q qComponent = e.get(qToken);
                W wComponent = e.get(wToken);
                E eComponent = e.get(eToken);
                system.execute(e, qComponent, wComponent, eComponent);
            }
        };
    }

    public <Context, Q, W, E, R> Action<Context> withComponents(Class<Q> q, Class<W> w, Class<E> e, Class<R> r, Systems.FourComponentSystem<Q, W, E, R> system) {
        return new Action<Context>() {
            ComponentToken<Q> qToken;
            ComponentToken<W> wToken;
            ComponentToken<E> eToken;
            ComponentToken<R> rToken;
            @Override
            public void init() {
                qToken = engineConfiguration.getEngine().getDaoManager().classToToken(q);
                wToken = engineConfiguration.getEngine().getDaoManager().classToToken(w);
                eToken = engineConfiguration.getEngine().getDaoManager().classToToken(e);
                rToken = engineConfiguration.getEngine().getDaoManager().classToToken(r);
            }

            @Override
            public void perform(Context context, Session session, int idx) {
                if (!qToken.dao.isPresent(idx) || !wToken.dao.isPresent(idx) || !eToken.dao.isPresent(idx) || !rToken.dao.isPresent(idx)) return;
                Entity e = session.getEntity(idx);
                Q qComponent = e.get(qToken);
                W wComponent = e.get(wToken);
                E eComponent = e.get(eToken);
                R rComponent = e.get(rToken);
                system.execute(e, qComponent, wComponent, eComponent, rComponent);
            }
        };
    }
}

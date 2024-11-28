///*
// * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// *
// */
//
//package pl.edu.icm.trurl.ecs.util;
//
//import net.snowyhollows.bento.annotation.WithFactory;
//import pl.edu.icm.trurl.ecs.*;
//
///**
// * Service with helper methods for creating addActions with typical patterns, like addActions with filters
// * based on presence of components.
// */
//public class ActionService {
//    private final EngineBuilder engineBuilder;
//
//    @WithFactory
//    public ActionService(EngineBuilder engineBuilder) {
//        this.engineBuilder = engineBuilder;
//    }
//
//    public <Context, Q> Action<Context> withComponent(Class<Q> q, Steps.OneComponentStep<Q> step) {
//        return new Action<Context>() {
//            ComponentToken<Q> qToken;
//            @Override
//            public void init() {
//                 qToken = engineBuilder.getEngine().getDaoManager().classToToken(q);
//            }
//
//            @Override
//            public void perform(Context context, Session session, int idx) {
//                if (!qToken.dao.isPresent(idx)) return;
//                Entity e = session.getEntity(idx);
//                Q qComponent = e.get(qToken);
//                step.execute(e, qComponent);
//            }
//        };
//    }
//
//    public <Context, Q, W> Action<Context> withComponents(Class<Q> q, Class<W> w, Steps.TwoComponentStep<Q, W> step) {
//        return new Action<Context>() {
//            ComponentToken<Q> qToken;
//            ComponentToken<W> wToken;
//            @Override
//            public void init() {
//                qToken = engineBuilder.getEngine().getDaoManager().classToToken(q);
//                wToken = engineBuilder.getEngine().getDaoManager().classToToken(w);
//            }
//
//            @Override
//            public void perform(Context context, Session session, int idx) {
//                if (!qToken.dao.isPresent(idx) || !wToken.dao.isPresent(idx)) return;
//                Entity e = session.getEntity(idx);
//                Q qComponent = e.get(qToken);
//                W wComponent = e.get(wToken);
//                step.execute(e, qComponent, wComponent);
//            }
//        };
//    }
//
//    public <Context, Q, W, E> Action<Context> withComponents(Class<Q> q, Class<W> w, Class<E> e, Steps.ThreeComponentStep<Q, W, E> step) {
//        return new Action<Context>() {
//            ComponentToken<Q> qToken;
//            ComponentToken<W> wToken;
//            ComponentToken<E> eToken;
//            @Override
//            public void init() {
//                qToken = engineBuilder.getEngine().getDaoManager().classToToken(q);
//                wToken = engineBuilder.getEngine().getDaoManager().classToToken(w);
//                eToken = engineBuilder.getEngine().getDaoManager().classToToken(e);
//            }
//
//            @Override
//            public void perform(Context context, Session session, int idx) {
//                if (!qToken.dao.isPresent(idx) || !wToken.dao.isPresent(idx) || !eToken.dao.isPresent(idx)) return;
//                Entity e = session.getEntity(idx);
//                Q qComponent = e.get(qToken);
//                W wComponent = e.get(wToken);
//                E eComponent = e.get(eToken);
//                step.execute(e, qComponent, wComponent, eComponent);
//            }
//        };
//    }
//
//    public <Context, Q, W, E, R> Action<Context> withComponents(Class<Q> q, Class<W> w, Class<E> e, Class<R> r, Steps.FourComponentStep<Q, W, E, R> step) {
//        return new Action<Context>() {
//            ComponentToken<Q> qToken;
//            ComponentToken<W> wToken;
//            ComponentToken<E> eToken;
//            ComponentToken<R> rToken;
//            @Override
//            public void init() {
//                qToken = engineBuilder.getEngine().getDaoManager().classToToken(q);
//                wToken = engineBuilder.getEngine().getDaoManager().classToToken(w);
//                eToken = engineBuilder.getEngine().getDaoManager().classToToken(e);
//                rToken = engineBuilder.getEngine().getDaoManager().classToToken(r);
//            }
//
//            @Override
//            public void perform(Context context, Session session, int idx) {
//                if (!qToken.dao.isPresent(idx) || !wToken.dao.isPresent(idx) || !eToken.dao.isPresent(idx) || !rToken.dao.isPresent(idx)) return;
//                Entity e = session.getEntity(idx);
//                Q qComponent = e.get(qToken);
//                W wComponent = e.get(wToken);
//                E eComponent = e.get(eToken);
//                R rComponent = e.get(rToken);
//                step.execute(e, qComponent, wComponent, eComponent, rComponent);
//            }
//        };
//    }
//}

/*
 * Copyright (c) 2024 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import pl.edu.icm.trurl.ecs.dao.LifecycleEvent;
import pl.edu.icm.trurl.ecs.index.Index;
import pl.edu.icm.trurl.ecs.util.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

abstract public class IteratingSystem implements EntitySystem {
    private boolean persistAll = true;
    private boolean flush = true;
    private Class[] componentsToPersist = new Class[0];
    private boolean parallel = false;
    private Index index;
    private Action[] actions;
    private boolean clear = true;
    private ComponentToken[] tokens;

    @Override
    public void onAddedToEngine(Engine engine) {
        List<Action<?>> tmpActions = new ArrayList<>();
        
        configure(new Config() {
            @Override
            public Config noPersist(boolean persistAll) {
                IteratingSystem.this.persistAll = persistAll;
                return this;
            }

            @Override
            public Config persistOnly(Class<?>... components) {
                IteratingSystem.this.persistAll = false;
                IteratingSystem.this.componentsToPersist = components;
                return this;
            }

            @Override
            public Config noFlush() {
                IteratingSystem.this.flush = false;
                return this;
            }

            @Override
            public Config noClear() {
                IteratingSystem.this.clear = false;
                return this;
            }

            @Override
            public Config addActions(Action<?>... operations) {
                tmpActions.addAll(Arrays.asList(operations));
                return this;
            }

            @Override
            public Config setIndex(Index index) {
                IteratingSystem.this.index = index;
                return this;
            }
        });
        boolean flushSelected = !persistAll && flush;
        if (flushSelected && componentsToPersist == null) {
            throw new IllegalStateException("Persisting selected components requires specifying which components to persist");
        }
        DaoManager daoManager = engine.getDaoManager();
        Stream<ComponentToken<?>> componentTokenStream = Arrays.stream(componentsToPersist).map(c -> daoManager.classToToken(c));
        tokens = flushSelected ? componentTokenStream.toArray(ComponentToken<?>[]::new) : null;
        if (index == null) {
            index = engine.getIndexes().allEntities();
        }
        actions = tmpActions.toArray(new Action[0]);
    }
    
    public abstract void configure(Config config);

    @Override
    public void execute(Engine engine) {
        DaoManager daoManager = engine.getDaoManager();

        for (int i = 0; i < actions.length; i++) {
            actions[i].startIteration();
        }

        (parallel ? index.chunks().parallel() : index.chunks()).forEach(chunk -> {
            final Session session = engine.getSession();
            int ownerId = chunk.getChunkInfo().getChunkId();
            session.setOwnerId(ownerId);

            Object[] privateContexts = new Object[actions.length];
            for (int i = 0; i < actions.length; i++) {
                privateContexts[i] = actions[i].startChunk(session, chunk.getChunkInfo());
            }

            chunk.ids().forEach(id -> {
                        for (int i = 0; i < actions.length; i++) {
                            actions[i].perform(privateContexts[i], session, id);
                        }
                    }
            );
            for (int i = 0; i < actions.length; i++) {
                actions[i].endChunk(privateContexts[i]);
            }
            if (flush) {
                if (persistAll) {
                    session.flush();
                } else {
                    session.flush(tokens);
                }
            }
            if (clear) {
                session.clear();
            }
        });
        for (int i = 0; i < actions.length; i++) {
            actions[i].endIteration();
        }
        if (parallel) {
            for (ComponentToken<?> token : tokens) {
                daoManager.tokenToDao(token).fireEvent(LifecycleEvent.POST_PARALLEL_ITERATION);
            }
        }
    }

    public interface Config {
        Config noPersist(boolean persistAll);
        Config persistOnly(Class<?>... components);
        Config noFlush();
        Config noClear();
        Config addActions(Action<?>... operations);
        Config setIndex(Index index);
    }
}

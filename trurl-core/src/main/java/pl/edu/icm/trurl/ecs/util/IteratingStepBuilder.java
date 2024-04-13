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

 package pl.edu.icm.trurl.ecs.util;

 import pl.edu.icm.trurl.ecs.*;
 import pl.edu.icm.trurl.ecs.dao.LifecycleEvent;
 import pl.edu.icm.trurl.ecs.index.Chunk;
 import pl.edu.icm.trurl.ecs.index.Index;

 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.function.Function;
 import java.util.function.Supplier;

 public class IteratingStepBuilder {
     private final Supplier<Index> indexSupplier;

     public IteratingStepBuilder(Supplier<Index> indexSupplier) {
         this.indexSupplier = indexSupplier;
     }

     public IteratingStepBuilder(Index index) {
         this.indexSupplier = () -> index;
     }

     public Config iteratingOver() {
         return new IteratingStep(indexSupplier, false);
     }

     public Config iteratingOverInParallel() {
         return new IteratingStep(indexSupplier, true);
     }

     private static class IteratingStep<Context> implements Config, ActionConfig<Context>, Step {
         private final Supplier<Index> index;
         private boolean parallel = false;
         private final List<ContextualAction<Context>> actions = new ArrayList<>();
         private boolean persistAll = true;
         private Class<?>[] componentsToPersist;
         private boolean flush = true;
         private final boolean clear = true;
         private Function contextFactory;
         private ContextualAction[] operationsArray;

         IteratingStep(Supplier<Index> index, boolean parallel) {
             this.index = index;
             this.parallel = parallel;
         }

         @Override
         public void execute(SessionFactory sessionFactory) {
             DaoManager daoManager = sessionFactory.getEngine().getDaoManager();
             boolean flushSelected = !persistAll && flush;
             if (flushSelected && componentsToPersist == null) {
                 throw new IllegalStateException("Persisting selected components requires specifying which components to persist");
             }

             ComponentToken<?>[] tokens = flushSelected ? Arrays.stream(componentsToPersist).map(c -> daoManager.classToToken(c)).toArray(ComponentToken[]::new) : null;
             for (ContextualAction action : operationsArray) {
                 action.init();
             }
             if (parallel) {
                 sessionFactory.lifecycleEvent(LifecycleEvent.PRE_PARALLEL_ITERATION);
             }
             (parallel ? index.get().chunks().parallel() : index.get().chunks()).forEach(chunk -> {
                 final Session session = sessionFactory.createOrGet();
                 int ownerId = chunk.getChunkInfo().getChunkId() + 1;
                 session.setOwnerId(ownerId);
                 Object[] privateContexts = new Object[actions.size()];
                 for (int i = 0; i < actions.size(); i++) {
                     privateContexts[i] = actions.get(i).initPrivateContext(session, chunk.getChunkInfo());
                 }

                 Context context = (Context) contextFactory.apply(chunk);
                 chunk.ids().forEach(id -> {
                             for (int i = 0; i < operationsArray.length; i++) {
                                 operationsArray[i].perform(context, session, id, privateContexts[i]);
                             }
                         }
                 );
                 for (int i = 0; i < actions.size(); i++) {
                     actions.get(i).closePrivateContext(privateContexts[i]);
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
             if (parallel) {
                 sessionFactory.lifecycleEvent(LifecycleEvent.POST_PARALLEL_ITERATION);
             }
         }

         @Override
         public Config persistingAll() {
             persistAll = true;
             return this;
         }

         @Override
         public Config persisting(Class<?>... components) {
             componentsToPersist = components;
             persistAll = false;
             return this;
         }

         public Config withoutPersisting() {
             flush = false;
             persistAll = false;
             componentsToPersist = null;
             return this;
         }

         @Override
         public ActionConfig<Void> withoutContext() {
             this.contextFactory = (unused) -> null;
             return (ActionConfig<Void>) this;
         }

         @Override
         public <Context> ActionConfig<Context> withContext(Function<Chunk, Context> contextFactory) {
             this.contextFactory = contextFactory;
             return (ActionConfig<Context>) this;
         }


         @Override
         public ActionConfig<Context> perform(ContextualAction<Context> action) {
             actions.add(action);
             return this;
         }

         @Override
         public Step build() {
             operationsArray = actions.toArray(new ContextualAction[0]);
             return this;
         }
     }

     public interface Config {

         Config persistingAll();

         Config persisting(Class<?>... components);

         Config withoutPersisting();

         ActionConfig<Void> withoutContext();

         <Context> ActionConfig<Context> withContext(Function<Chunk, Context> contextFactory);
     }

     public interface ActionConfig<Context> {
         ActionConfig<Context> perform(ContextualAction<Context> action);

         Step build();
     }

     public static Config iteratingOver(final Index index) {
         return new IteratingStep(() -> index, false);
     }

     public static Config iteratingOverInParallel(final Index index) {
         return new IteratingStep(() -> index, true);
     }

     public static Config iteratingOver(final Supplier<Index> index) {
         return new IteratingStep(index, false);
     }

     public static Config iteratingOverInParallel(final Supplier<Index> index) {
         return new IteratingStep(index, true);
     }
 }

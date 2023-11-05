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

 package pl.edu.icm.trurl.ecs.system;

 import pl.edu.icm.trurl.ecs.EntitySystem;
 import pl.edu.icm.trurl.ecs.entity.Session;
 import pl.edu.icm.trurl.ecs.entity.SessionFactory;
 import pl.edu.icm.trurl.ecs.mapper.LifecycleEvent;
 import pl.edu.icm.trurl.ecs.selector.Chunk;
 import pl.edu.icm.trurl.ecs.selector.Selector;

 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.function.Function;

 public class IteratingSystemBuilder {

     public enum Mode {
            SHARED, NO_PERSIST, DEFAULT
     }

     private static class IteratingSystem<Context> implements PersistenceConfig, ContextConfig, FirstVisitConfig<Context>, VisitConfig<Context>, EntitySystem {

         private final Selector selector;
         private Mode mode = Mode.DEFAULT;
         private boolean parallel;
         private List<Visit<Context>> visits = new ArrayList<>();
         private Object[] componentsToPersist;
         private Function contextFactory;
         private Visit[] operationsArray;

         IteratingSystem(Selector selector, boolean parallel) {
             this.selector = selector;
             this.parallel = parallel;
         }

         @Override
         public void execute(SessionFactory sessionFactory) {
             for (Visit visit : operationsArray) {
                 visit.init();
             }
             if (parallel) {
                 sessionFactory.lifecycleEvent(LifecycleEvent.PRE_PARALLEL_ITERATION);
             }
             (parallel ? selector.chunks().parallel() : selector.chunks()).forEach(chunk -> {
                 final Session session = sessionFactory.getSession();
                 session.setOwnerId(chunk.getChunkInfo().getChunkId() + 1);
                 if (componentsToPersist == null) {
                     session.persistingAll();
                 } else {
                     session.persistingExactly(Arrays.asList(componentsToPersist).toArray(new Class[0]));
                 }
                 Context context = (Context) contextFactory.apply(chunk);
                 chunk.ids()
                         .forEach(id -> {
                             for (Visit visit : operationsArray) {
                                 visit.perform(context, session, id);
                             }
                         });
                 switch (mode) {
                     case DEFAULT:
                         if (sessionFactory.shouldClearByDefault()) {
                             session.close();
                         } else {
                             session.persist();
                         }
                         break;
                     case SHARED:
                         session.persist();
                         break;
                     case NO_PERSIST:
                         session.clear();
                 }
                 session.close();
             });
             if (parallel) {
                 sessionFactory.lifecycleEvent(LifecycleEvent.POST_PARALLEL_ITERATION);
             }
         }

         @Override
         public ContextConfig sharedEntities() {
             this.mode = Mode.SHARED;
             return this;
         }

         @Override
         public ContextConfig readOnlyEntities() {
             this.mode = Mode.NO_PERSIST;
             return this;
         }

         @Override
         public ContextConfig persistingAll() {
             componentsToPersist = null;
             return this;
         }

         @Override
         public ContextConfig persistingOnly(Object... components) {
             componentsToPersist = components;
             return this;
         }

         @Override
         public FirstVisitConfig<Void> withoutContext() {
             this.contextFactory = (unused) -> null;
             return (FirstVisitConfig<Void>) this;
         }

         @Override
         public <Context> FirstVisitConfig<Context> withContext(Function<Chunk, Context> contextFactory) {
             this.contextFactory = contextFactory;
             return (FirstVisitConfig<Context>) this;
         }


         @Override
         public VisitConfig<Context> perform(Visit<Context> visit) {
             visits.add(visit);
             return this;
         }

         @Override
         public VisitConfig<Context> andPerform(Visit<Context> visit) {
             return perform(visit);
         }

         @Override
         public EntitySystem build() {
             operationsArray = visits.toArray(new Visit[0]);
             return this;
         }
     }

     public interface PersistenceConfig {
         ContextConfig sharedEntities();

         ContextConfig readOnlyEntities();

         ContextConfig persistingAll();

         ContextConfig persistingOnly(Object... components);
     }

     public interface ContextConfig {
         FirstVisitConfig<Void> withoutContext();

         <Context> FirstVisitConfig<Context> withContext(Function<Chunk, Context> contextFactory);
     }

     public interface FirstVisitConfig<Context> {
         VisitConfig<Context> perform(Visit<Context> visit);

     }


     public interface VisitConfig<Context> {
         VisitConfig<Context> andPerform(Visit<Context> visit);

         EntitySystem build();
     }

     public static PersistenceConfig iteratingOver(final Selector selector) {
         return new IteratingSystem(selector, false);
     }

     public static PersistenceConfig iteratingOverInParallel(final Selector selector) {
         return new IteratingSystem(selector, true);
     }

 }

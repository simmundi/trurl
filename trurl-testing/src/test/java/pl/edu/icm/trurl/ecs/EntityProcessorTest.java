/*
 * Copyright (c) 2026 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntityProcessorTest {

    @Mock
    private Session session;

    @Mock
    private Entity entity;

    @Test
    public void testEntityPiggybacking() {
        // given
        AtomicInteger lookupCounter = new AtomicInteger();
        when(session.getEntity(10)).thenAnswer(inv -> {
            lookupCounter.incrementAndGet();
            return entity;
        });

        // Worker that needs the entity
        EntityProcessor worker = EntityProcessor.from((Entity e) -> {
            // just use it
        });

        // Case 1: Start with entity
        lookupCounter.set(0);
        worker.run(entity);
        // Should NOT have triggered session.getEntity(10) because we passed the entity
        assertThat(lookupCounter.get()).isEqualTo(0);

        // Case 2: Start with session and id
        lookupCounter.set(0);
        worker.run(session, 10);
        // SHOULD have triggered session.getEntity(10) once
        assertThat(lookupCounter.get()).isEqualTo(1);

        // Case 3: Chain with redundant workers
        lookupCounter.set(0);
        EntityProcessor chain = EntityProcessor.chain(worker, worker, worker);
        chain.run(session, 10);
        // SHOULD have triggered session.getEntity(10) exactly ONCE, then piggybacked
        assertThat(lookupCounter.get()).isEqualTo(1);
    }

    @Test
    public void testStatefulReification() {
        // given
        AtomicInteger productionCounter = new AtomicInteger();
        java.util.function.Supplier<String> supplier = () -> {
            productionCounter.incrementAndGet();
            return "context";
        };

        List<String> results = new ArrayList<>();
        EntityProcessor stateful = EntityProcessor.from((s, id, e, ctx, next) -> {
            results.add(ctx);
            next.runInternal(s, id, e);
        }, supplier);

        EntityProcessor chain = EntityProcessor.chain(stateful, stateful);

        // Case 1: Before reification
        assertThat(productionCounter.get()).isEqualTo(0);

        // Case 2: Reify
        EntityProcessor reified = chain.reify();
        // Each stateful link in the chain should have been reified
        assertThat(productionCounter.get()).isEqualTo(2);

        // Case 3: Run
        reified.run(session, 10);
        assertThat(results).containsExactly("context", "context");

        // Case 4: Reify again should produce NEW contexts
        results.clear();
        EntityProcessor reified2 = chain.reify();
        assertThat(productionCounter.get()).isEqualTo(4);
        reified2.run(session, 10);
        assertThat(results).containsExactly("context", "context");
    }

    @Test
    public void testLifecycle() {
        // given
        List<String> results = new ArrayList<>();
        EntityProcessor worker = EntityProcessor.from(new EntityProcessor.StatefulProcessor<List<String>>() {
            @Override
            public void run(Session session, int entityId, Entity entity, List<String> context, EntityProcessor.InternalLink next) {
                context.add("entity-" + entityId);
                next.runInternal(session, entityId, entity);
            }

            @Override
            public void onEnd(Session session, List<String> context) {
                results.addAll(context);
                results.add("ended");
            }
        }, ArrayList::new);

        EntityProcessor reified = worker.reify();

        // execute
        reified.onBegin(session);
        reified.run(session, 1);
        reified.run(session, 2);
        assertThat(results).isEmpty(); // Not ended yet
        reified.onEnd(session);

        // assert
        assertThat(results).containsExactly("entity-1", "entity-2", "ended");
    }

    @Test
    public void testChainLifecyclePropagation() {
        // given
        List<String> results = new ArrayList<>();
        EntityProcessor processor = new EntityProcessor() {
            @Override
            public void run(Session session, int entityId) {}

            @Override
            public void rawRun(Session session, int entityId, Entity entity) {}

            @Override
            public void onBegin(Session session) {
                results.add("begin");
            }

            @Override
            public void onEnd(Session session) {
                results.add("end");
            }
        };

        // Case 1: Direct execution
        results.clear();
        processor.onBegin(session);
        processor.onEnd(session);
        assertThat(results).as("Direct execution should trigger lifecycle").containsExactly("begin", "end");

        // Case 2: Chained execution
        results.clear();
        EntityProcessor chained = EntityProcessor.chain(processor);
        chained.onBegin(session);
        chained.onEnd(session);
        assertThat(results).as("Chained execution should trigger lifecycle").containsExactly("begin", "end");
    }

    @Test
    public void testBasicFromConsumer() {
        // given
        AtomicInteger counter = new AtomicInteger();
        EntityProcessor processor = EntityProcessor.from((Entity e) -> counter.incrementAndGet());
        when(session.getEntity(10)).thenReturn(entity);

        // execute
        processor.run(session, 10);

        // assert
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    public void testChain() {
        // given
        List<String> results = new ArrayList<>();
        EntityProcessor p1 = EntityProcessor.from((Entity e) -> results.add("p1"));
        EntityProcessor p2 = EntityProcessor.from((Entity e) -> results.add("p2"));
        EntityProcessor p3 = EntityProcessor.from((Entity e) -> results.add("p3"));
        EntityProcessor chain = EntityProcessor.chain(p1, p2, p3);
        when(session.getEntity(10)).thenReturn(entity);

        // execute
        chain.run(session, 10);

        // assert
        assertThat(results).containsExactly("p1", "p2", "p3");
    }

    @Test
    public void testChainWithShortCircuit() {
        // given
        List<String> results = new ArrayList<>();
        EntityProcessor p1 = EntityProcessor.from((Entity e) -> results.add("p1"));
        EntityProcessor shortCircuit = EntityProcessor.from((Session s, int id, Entity entity, EntityProcessor.InternalLink next) -> {
            results.add("short");
            // does NOT call next.runInternal()
        });
        EntityProcessor p3 = EntityProcessor.from((Entity e) -> results.add("p3"));
        EntityProcessor chain = EntityProcessor.chain(p1, shortCircuit, p3);
        when(session.getEntity(10)).thenReturn(entity);

        // execute
        chain.run(session, 10);

        // assert
        assertThat(results).containsExactly("p1", "short");
    }

    @Test
    public void testChainWithFanOut() {
        // given
        List<Integer> processedIds = new ArrayList<>();
        EntityProcessor worker = EntityProcessor.from((Session s, int id) -> processedIds.add(id));

        EntityProcessor fanOut = EntityProcessor.from((Session s, int id, Entity entity, EntityProcessor.InternalLink next) -> {
            next.runInternal(s, 101, null);
            next.runInternal(s, 102, null);
            next.runInternal(s, 103, null);
        });

        EntityProcessor chain = EntityProcessor.chain(fanOut, worker);

        // execute
        chain.run(session, 100);

        // assert
        assertThat(processedIds).containsExactly(101, 102, 103);
    }

    @Test
    public void testChainWithEntityAndNext() {
        // given
        List<String> results = new ArrayList<>();
        when(session.getEntity(10)).thenReturn(entity);

        EntityProcessor p = EntityProcessor.from((Entity e, EntityProcessor.RawEntityProcessor next) -> {
            results.add("before");
            next.run(e.getSession(), e.getId(), e);
            results.add("after");
        });

        EntityProcessor worker = EntityProcessor.from((Entity e) -> results.add("work"));

        EntityProcessor chain = EntityProcessor.chain(p, worker);

        // execute
        chain.run(session, 10);

        // assert
        assertThat(results).containsExactly("before", "work", "after");
    }

    @Test
    public void testFlattening() {
        // given
        List<String> results = new ArrayList<>();
        EntityProcessor p1 = EntityProcessor.from((Entity e) -> results.add("p1"));
        EntityProcessor p2 = EntityProcessor.from((Entity e) -> results.add("p2"));
        EntityProcessor p3 = EntityProcessor.from((Entity e) -> results.add("p3"));
        EntityProcessor p4 = EntityProcessor.from((Entity e) -> results.add("p4"));

        EntityProcessor subChain = EntityProcessor.chain(p2, p3);
        EntityProcessor mainChain = EntityProcessor.chain(p1, subChain, p4);

        when(session.getEntity(10)).thenReturn(entity);

        // execute
        mainChain.run(session, 10);

        // assert
        assertThat(results).containsExactly("p1", "p2", "p3", "p4");

        // check flattening: subChain should be flattened into mainChain
        EntityProcessor.TopLevelChain topLevel = (EntityProcessor.TopLevelChain) mainChain;
        EntityProcessor.InternalLink link1 = topLevel.first;
        EntityProcessor.InternalLink link2 = link1.nextLink;
        EntityProcessor.InternalLink link3 = link2.nextLink;
        EntityProcessor.InternalLink link4 = link3.nextLink;
        EntityProcessor.InternalLink link5 = link4.nextLink;

        assertThat(link5).isSameAs(EntityProcessor.FinalLink.INSTANCE);
    }

    @Test
    public void testBranching() {
        // given
        List<String> results = new ArrayList<>();
        EntityProcessor trueBranch = EntityProcessor.from((Entity e) -> results.add("true"));
        EntityProcessor falseBranch = EntityProcessor.from((Entity e) -> results.add("false"));
        EntityProcessor after = EntityProcessor.from((Entity e) -> results.add("after"));

        EntityProcessor branching = EntityProcessor.branch(
                (s, id) -> id == 1,
                trueBranch,
                falseBranch
        );

        EntityProcessor chain = EntityProcessor.chain(branching, after);
        when(session.getEntity(1)).thenReturn(entity);
        when(session.getEntity(2)).thenReturn(entity);

        // execute 1
        results.clear();
        chain.run(session, 1);
        // assert 1
        assertThat(results).containsExactly("true", "after");

        // execute 2
        results.clear();
        chain.run(session, 2);
        // assert 2
        assertThat(results).containsExactly("false", "after");
    }
}

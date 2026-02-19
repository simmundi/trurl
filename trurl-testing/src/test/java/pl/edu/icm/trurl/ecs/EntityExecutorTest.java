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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntityExecutorTest {

    @Mock
    private EngineBuilder engineBuilder;

    @Mock
    private Engine engine;

    @Mock
    private Session session;

    @Test
    public void testExecute() {
        // given
        when(engineBuilder.getEngine()).thenReturn(engine);
        when(engine.getSession()).thenReturn(session);
        EntityExecutor executor = new EntityExecutor(engineBuilder);
        List<String> events = new ArrayList<>();
        EntityProcessor processor = EntityProcessor.from(new EntityProcessor.StatefulProcessor<Void>() {
            @Override
            public void run(Session session, int entityId, Entity entity, Void context, EntityProcessor.InternalLink next) {
                events.add("run-" + entityId);
                next.runInternal(session, entityId, entity);
            }
            @Override
            public void onBegin(Session session) { events.add("begin"); }
            @Override
            public void onEnd(Session session) { events.add("end"); }
        }, () -> null);

        Source source = consumer -> {
            consumer.accept(1);
            consumer.accept(2);
        };

        // execute
        executor.execute(source, processor);

        // assert
        assertThat(events).containsExactly("begin", "run-1", "run-2", "end");
        verify(session).flush();
    }

    @Test
    public void testChunkAsEntityPattern() {
        // given
        when(engineBuilder.getEngine()).thenReturn(engine);
        when(engine.getSession()).thenReturn(session);
        EntityExecutor executor = new EntityExecutor(engineBuilder);

        // Components for our reified chunks
        class ChunkComponent {
            int[] entityIds;
            ChunkComponent(int... ids) { this.entityIds = ids; }
        }

        // The "inner" worker that does the actual work on entities
        List<Integer> processedEntities = new ArrayList<>();
        EntityProcessor innerWorker = EntityProcessor.from((Session s, int id) -> processedEntities.add(id));

        // The "chunk processor" that fanning out to entities within the chunk
        EntityProcessor chunkProcessor = EntityProcessor.from((Entity chunkEntity, EntityProcessor.RawEntityProcessor next) -> {
            ChunkComponent chunk = chunkEntity.get(ChunkComponent.class);
            for (int id : chunk.entityIds) {
                // Fan out to the rest of the chain for each ID in the chunk
                next.run(chunkEntity.getSession(), id, null);
            }
        });

        // The complete chain: Chunk Processor -> Inner Worker
        EntityProcessor fullChain = EntityProcessor.chain(chunkProcessor, innerWorker);

        // Setup mock entities for chunks
        Entity chunk1 = mock(Entity.class);
        Entity chunk2 = mock(Entity.class);
        when(chunk1.get(ChunkComponent.class)).thenReturn(new ChunkComponent(10, 11));
        when(chunk2.get(ChunkComponent.class)).thenReturn(new ChunkComponent(20, 21, 22));
        when(chunk1.getSession()).thenReturn(session);
        when(chunk2.getSession()).thenReturn(session);

        when(session.getEntity(100)).thenReturn(chunk1);
        when(session.getEntity(101)).thenReturn(chunk2);

        // Source that provides Chunk IDs
        Source chunkSource = consumer -> {
            consumer.accept(100);
            consumer.accept(101);
        };

        // execute
        executor.execute(chunkSource, fullChain);

        // assert
        assertThat(processedEntities).containsExactly(10, 11, 20, 21, 22);
        verify(session).flush();
    }
}

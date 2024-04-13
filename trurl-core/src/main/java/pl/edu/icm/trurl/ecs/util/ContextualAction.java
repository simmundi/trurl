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

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.index.ChunkInfo;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Action is a single piece of business logic which can be applied to a single Entity.
 *
 * Actions might require a public context, which is an arbitrary object created on a per-chunk basis,
 * shared by multiple actions.
 *
 * Actions can also create private contexts, which are created on a per-chunk basis and are private to the action.
 *
 * Since all entities within a chunk are guaranteed to be processed in a single thread, the
 * context objects don't need to be thread-safe and their usage is deterministic across execution
 * with different numbers of threads (including the single-threaded execution).
 *
 * Any user of an action should first call its init() method at least once.
 *
 * Then, for each chunk of entities (even if it's only one), the user should call initPrivateContext(),
 * passing in the chunkInfo (can be ChunkInfo.NO_CHUNK if entity was not accessed in a chunk) and a session.
 * The returned object should then be passed to the perform() method with each entity to process (it can be null,
 * at the Action's discretion).
 *
 * Finally, after the last call to perform(), the user should call closePrivateContext().
 *
 * Implementations are free to ignore the context objects if they don't need them or allow calling
 * without the context objects.
 *
 * @param <Context>
 */
public interface ContextualAction<Context> {
    default void perform(Context context, Session session, int idx, Object t) {
        perform(context, session, idx);
    }

    void perform(Context context, Session session, int idx);

    default void init() {}

    default <T> T initPrivateContext(Session session, ChunkInfo chunkInfo) { return null; }

    default void closePrivateContext(Object context) {  }

    static<Context> ContextualAction<Context> of(BiConsumer<Context, Entity> consumer) {
        return (context, session, idx) -> consumer.accept(context, session.getEntity(idx));
    }

    static<Context> ContextualAction<Context> of(Consumer<Entity> consumer) {
        return (context, session, idx) -> consumer.accept(session.getEntity(idx));
    }
}

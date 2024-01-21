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
 * Actions might require a context, which is an arbitrary object created on a per-chunk basis.
 * Since all entities within a chunk are guaranteed to be processed in a single thread, the
 * context object doesn't need to be thread-safe and its usage is deterministic across execution
 * with different numbers of threads (including the single-threaded execution).
 *
 * @param <Context>
 */
public interface Action<Context> {
    void perform(Context context, Session session, int idx);

    default void init() {}

    static<Context> Action<Context> of(BiConsumer<Context, Entity> consumer) {
        return (context, session, idx) -> consumer.accept(context, session.getEntity(idx));
    }

    static<Context> Action<Context> of(Consumer<Entity> consumer) {
        return (context, session, idx) -> consumer.accept(session.getEntity(idx));
    }
}

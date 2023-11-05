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

package pl.edu.icm.trurl.ecs.system;

import pl.edu.icm.trurl.ecs.entity.Entity;
import pl.edu.icm.trurl.ecs.entity.Session;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Visit<Context> {
    void perform(Context context, Session session, int idx);

    default void init() {}

    static<Context> Visit<Context> of(BiConsumer<Context, Entity> consumer) {
        return (context, session, idx) -> consumer.accept(context, session.getEntity(idx));
    }

    static<Context> Visit<Context> of(Consumer<Entity> consumer) {
        return (context, session, idx) -> consumer.accept(session.getEntity(idx));
    }
}



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

package pl.edu.icm.trurl.ecs;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.ecs.Session;

/**
 * This class contains pocs and examples of different approaches of using systems.
 * It's a kind of a playground.
 * <p>
 * The point of keeping it separately from the Engine is that the Engine should be stable & mature,
 * while the addons can be more elastic and welcome experimentation.
 */
public final class Systems {

    private Systems() {

    }

    // combinators:

    public static EntitySystem sequence(EntitySystem... systems) {
        return (sessionFactory -> {
            for (EntitySystem system : systems) {
                system.execute(sessionFactory);
            }
        });
    }

    public interface IdxProcessor<Context> {
        void process(Context context, Session session, int idx);
    }

    public interface OneComponentSystem<Q> {
        void execute(Entity e, Q q);
    }

    public interface TwoComponentSystem<Q, W> {
        void execute(Entity e, Q q, W w);
    }

    public interface ThreeComponentSystem<Q, W, E> {
        void execute(Entity entity, Q q, W w, E e);
    }

    public interface FourComponentSystem<Q, W, E, R> {
        void execute(Entity entity, Q q, W w, E e, R r);
    }

}

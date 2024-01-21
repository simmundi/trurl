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

package pl.edu.icm.trurl.ecs;

import pl.edu.icm.trurl.ecs.dao.LifecycleEvent;

public final class SessionFactory {
    private final Engine engine;
    private final int expectedEntityCount;
    private final ThreadLocal<Session> sessions;

    public SessionFactory(Engine engine, int expectedEntityCount) {
        this.engine = engine;
        this.expectedEntityCount = expectedEntityCount;
        sessions = ThreadLocal.withInitial(this::createNew);
    }

    public Session createOrGet() {
        return sessions.get();
    }

    public void lifecycleEvent(LifecycleEvent event) {
        engine.getDaoManager().getAllDaos().forEach(dao -> dao.fireEvent(event));
    }

    private Session createNew() {
        Session session = new Session(engine, expectedEntityCount);
        return session;
    }

    public Engine getEngine() {
        return engine;
    }
}

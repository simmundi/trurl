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

import com.google.common.base.Preconditions;
import pl.edu.icm.trurl.ecs.mapper.LifecycleEvent;

import java.util.ArrayList;
import java.util.List;

public final class SessionFactory {
    public final static int EXPECTED_ENTITY_COUNT = 25_000;
    private final static Session.Mode DEFAULT_MODE = Session.Mode.NORMAL;

    private final Engine engine;
    private final Session.Mode mode;
    private final int expectedEntityCount;
    private final Session shared;
    private Class[] persistables = new Class[]{};

    public void setPersistables(Class[] persistables) {
        this.persistables = persistables;
    }

    public SessionFactory(Engine engine) {
        this(engine, DEFAULT_MODE, EXPECTED_ENTITY_COUNT);
    }

    public SessionFactory(Engine engine, Session.Mode mode) {
        this(engine, mode, EXPECTED_ENTITY_COUNT);
    }

    public SessionFactory(Engine engine, Session.Mode mode, int expectedEntityCount) {
        this.engine = engine;
        this.expectedEntityCount = expectedEntityCount;
        this.mode = mode;
        if (mode == Session.Mode.SHARED) {
            this.shared = create();
        } else {
            this.shared = null;
        }
    }

    public Session create() {
        return shared == null ? this.create(1) : shared;
    }

    public Session create(int ownerId) {
        return create(ownerId, expectedEntityCount);
    }

    public Session create(int ownerId, int expectedEntityCount) {
        Preconditions.checkState(ownerId > 0, "OwnerId must be positive");
        Preconditions.checkState(shared == null || shared.getOwnerId() == ownerId, "Shared session factory cannot change ownerId");
        Preconditions.checkState(shared == null || this.expectedEntityCount >= expectedEntityCount, "Shared session factory cannot enlarge expected entity count (was %s is %s)", this.expectedEntityCount, expectedEntityCount);
        return shared == null ? new Session(engine, expectedEntityCount, mode, ownerId, persistables) : shared;
    }

    public SessionFactory withModeAndCount(Session.Mode mode, int expectedEntityCount) {
        if (shared != null) {
            Preconditions.checkState(this.expectedEntityCount >= expectedEntityCount, "Shared session factory cannot enlarge expected entity count (was %s is %s)", this.expectedEntityCount, expectedEntityCount);
            return this;
        }
        return new SessionFactory(engine, mode, expectedEntityCount);
    }

    public void lifecycleEvent(LifecycleEvent event) {
        engine.getMapperSet().streamMappers().forEach(mapper -> mapper.lifecycleEvent(event));
    }
}

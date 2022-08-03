package pl.edu.icm.trurl.ecs;

import com.google.common.base.Preconditions;
import pl.edu.icm.trurl.ecs.mapper.LifecycleEvent;

public final class SessionFactory {
    public final static int EXPECTED_ENTITY_COUNT = 25_000;
    private final static Session.Mode DEFAULT_MODE = Session.Mode.NORMAL;

    private final Engine engine;
    private final Session.Mode mode;
    private final int expectedEntityCount;
    private final Session shared;

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
        return shared == null ? new Session(engine, expectedEntityCount, mode, ownerId) : shared;
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

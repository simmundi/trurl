package pl.edu.icm.trurl.ecs;

public final class SessionFactory {
    private final static int EXPECTED_ENTITY_COUNT = 25_000;
    private final static Session.Mode DEFAULT_MODE = Session.Mode.NORMAL;

    private final Engine engine;
    private final Session.Mode mode;
    private final int expectedEntityCount;

    public SessionFactory(Engine engine) {
        this(engine, DEFAULT_MODE, EXPECTED_ENTITY_COUNT);
    }

    public SessionFactory(Engine engine, Session.Mode mode) {
        this(engine, mode, EXPECTED_ENTITY_COUNT);
    }

    public SessionFactory(Engine engine, Session.Mode mode, int expectedEntityCount) {
        this.engine = engine;
        this.mode = mode;
        this.expectedEntityCount = expectedEntityCount;
    }

    public Session create() {
        return create(EXPECTED_ENTITY_COUNT);
    }

    public Session create(int expectedEntityCount) {
        return new Session(engine, expectedEntityCount, mode);
    }

    public SessionFactory withModeAndCount(Session.Mode mode, int expectedEntityCount) {
        return new SessionFactory(engine, mode, expectedEntityCount);
    }
}
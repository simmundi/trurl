package pl.edu.icm.trurl.ecs.util;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.EntitySystem;
import pl.edu.icm.trurl.util.Status;

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

    public static EntitySystem withStatusMessage(EntitySystem system, String message) {
        return sessionFactory -> {
            Status status = Status.of(message);
            system.execute(sessionFactory);
            status.done();
        };
    }

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

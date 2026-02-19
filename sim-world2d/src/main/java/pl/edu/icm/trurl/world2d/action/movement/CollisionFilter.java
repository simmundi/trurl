package pl.edu.icm.trurl.world2d.action.movement;

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import pl.edu.icm.trurl.ecs.Session;

@ImplementationSwitch(configKey = "trurl.world2d.movement.collisionFilter", cases = {
        @ImplementationSwitch.When(name = "everything", implementation = DefaultFilter.class, useByDefault = true)
})
public interface CollisionFilter {
    boolean test(Session session, int movingId);
    CollisionResponse testPerTarget(Session session, int movingId, int targetId);

    enum CollisionResponse {
        HARD(true, true),
        SOFT(false, true),
        SIMPLE(true, false),
        NONE(false, false);

        public final boolean stops;
        public final boolean remembers;

        CollisionResponse(boolean stops, boolean remembers) {
            this.stops = stops;
            this.remembers = remembers;
        }
    }
}

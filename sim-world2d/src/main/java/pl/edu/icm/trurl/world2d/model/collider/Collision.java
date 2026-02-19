package pl.edu.icm.trurl.world2d.model.collider;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
public class Collision {
    private Entity target;
    private CollisionType type;

    public Collision() {
    }

    public Collision(Entity target, CollisionType type) {
        this.target = target;
        this.type = type;
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public CollisionType getType() {
        return type;
    }

    public void setType(CollisionType type) {
        this.type = type;
    }
}

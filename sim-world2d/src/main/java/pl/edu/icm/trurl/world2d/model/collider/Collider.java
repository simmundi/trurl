package pl.edu.icm.trurl.world2d.model.collider;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.ArrayList;
import java.util.List;

@WithDao(namespace = "collider")
public class Collider {
    private List<Collision> collisions = new ArrayList<>();
    private float dx, dy;

    public List<Collision> getCollisions() {
        return collisions;
    }

    public void horizontalCollisionWith(Entity entity) {
        collisions.add(new Collision(entity, CollisionType.HORIZONTAL));
    }

    public void verticalCollisionWith(Entity entity) {
        collisions.add(new Collision(entity, CollisionType.VERTICAL));
    }

    public void reset() {
        collisions.clear();
    }

    public float getDx() {
        return dx;
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

    public float getDy() {
        return dy;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }
}

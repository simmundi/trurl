package pl.edu.icm.trurl.world2d.model.space;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao(namespace = "velocity")
public class Velocity {
    private float dx;
    private float dy;

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

    public void clamp(float max) {
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > max) {
            dx = dx / length * max;
            dy = dy / length * max;
        }
    }

    public void clampX(float max) {
        if (Math.abs(dx) > max) {
            dx = dx > 0 ? max : -max;
        }
    }

    public void clampY(float max) {
        if (Math.abs(dy) > max) {
            dy = dy > 0 ? max : -max;
        }
    }
}

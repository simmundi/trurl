package pl.edu.icm.trurl.world2d.model.display;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao(namespace = "animation")
public class AnimationFrame {
    private Entity frame;
    private float duration;

    public Entity getFrame() {
        return frame;
    }

    public void setFrame(Entity frame) {
        this.frame = frame;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public static AnimationFrame of(Entity frame, float duration) {
        AnimationFrame animationFrame = new AnimationFrame();
        animationFrame.setFrame(frame);
        animationFrame.setDuration(duration);
        return animationFrame;
    }
}

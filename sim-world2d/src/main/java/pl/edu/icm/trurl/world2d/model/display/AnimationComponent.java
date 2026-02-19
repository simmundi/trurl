package pl.edu.icm.trurl.world2d.model.display;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.ArrayList;
import java.util.List;

@WithDao
public class AnimationComponent {
    private List<AnimationFrame> frames = new ArrayList<>();
    public List<AnimationFrame> getFrames() {
        return frames;
    }
}

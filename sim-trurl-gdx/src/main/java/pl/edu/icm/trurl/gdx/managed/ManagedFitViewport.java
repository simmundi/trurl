package pl.edu.icm.trurl.gdx.managed;

import com.badlogic.gdx.utils.viewport.FitViewport;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;

public class ManagedFitViewport extends FitViewport {
    public ManagedFitViewport(float worldWidth, float worldHeight) {
        super(worldWidth, worldHeight);
    }

    @WithFactory
    public ManagedFitViewport(@ByName(value = "gdx.worldWidth", fallbackValue = "640") float worldWidth,
                              @ByName(value = "gdx.worldHeight", fallbackValue = "480") float worldHeight,
                              ManagedOrthographicCamera camera) {
        super(worldWidth, worldHeight, camera);
    }
}

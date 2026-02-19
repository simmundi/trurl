package pl.edu.icm.trurl.gdx.managed;

import com.badlogic.gdx.graphics.OrthographicCamera;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;

public class ManagedHudOrthographicCamera extends OrthographicCamera {

    public ManagedHudOrthographicCamera() {
    }

    @WithFactory
    public ManagedHudOrthographicCamera(@ByName(value="gdx.hud.viewportWidth", fallbackValue="640") float viewportWidth,
                                        @ByName(value="gdx.hud.viewportHeight", fallbackValue="480") float viewportHeight) {
        super(viewportWidth, viewportHeight);
    }
}

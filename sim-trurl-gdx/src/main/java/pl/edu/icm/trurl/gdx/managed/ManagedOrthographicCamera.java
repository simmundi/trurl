package pl.edu.icm.trurl.gdx.managed;

import com.badlogic.gdx.graphics.OrthographicCamera;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;

public class ManagedOrthographicCamera extends OrthographicCamera {

    public ManagedOrthographicCamera() {
    }

    @WithFactory
    public ManagedOrthographicCamera(@ByName(value="gdx.ortho.viewportWidth", fallbackValue="640") float viewportWidth,
                                     @ByName(value="gdx.ortho.viewportHeight", fallbackValue="480") float viewportHeight) {
        super(viewportWidth, viewportHeight);
    }
}

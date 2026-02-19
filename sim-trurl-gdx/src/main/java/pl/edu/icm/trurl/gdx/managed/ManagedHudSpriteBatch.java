package pl.edu.icm.trurl.gdx.managed;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;

public class ManagedHudSpriteBatch extends SpriteBatch {
    public ManagedHudSpriteBatch() {
    }

    @WithFactory

    public ManagedHudSpriteBatch(@ByName(value = "gdx.hud.spriteBatch.size", fallbackValue = "1000") int size) {
        super(size);
    }
}

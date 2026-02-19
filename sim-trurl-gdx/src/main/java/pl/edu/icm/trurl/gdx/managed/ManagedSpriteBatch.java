package pl.edu.icm.trurl.gdx.managed;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;

public class ManagedSpriteBatch extends SpriteBatch {
    public ManagedSpriteBatch() {
    }

    @WithFactory
    public ManagedSpriteBatch(@ByName(value = "gdx.spriteBatch.size", fallbackValue = "1000") int size) {
        super(size);
    }

}

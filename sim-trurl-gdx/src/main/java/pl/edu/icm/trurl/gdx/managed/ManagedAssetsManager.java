package pl.edu.icm.trurl.gdx.managed;

import com.badlogic.gdx.assets.AssetManager;
import net.snowyhollows.bento.annotation.WithFactory;

public class ManagedAssetsManager extends AssetManager {
    @WithFactory
    public ManagedAssetsManager() {
    }
}

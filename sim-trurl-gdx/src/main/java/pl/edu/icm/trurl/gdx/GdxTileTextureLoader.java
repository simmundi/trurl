package pl.edu.icm.trurl.gdx;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.gdx.managed.ManagedAssetsManager;
import pl.edu.icm.trurl.world2d.model.display.*;

import java.util.HashMap;
import java.util.Map;

public class GdxTileTextureLoader  {
    private final IntMap<TextureRegion> textureRegions = new IntMap<>();
    private final Map<String, Texture> textures = new HashMap<>();
    private final AssetManager assetManager;

    @WithFactory
    public GdxTileTextureLoader(EngineBuilder engineBuilder, ManagedAssetsManager assetManager) {
        this.assetManager = assetManager;
        engineBuilder.addComponentWithDao(TextureComponent.class, DaoOfTextureComponentFactory.IT);
        engineBuilder.addComponentWithDao(TextureRegionComponent.class, DaoOfTextureRegionComponentFactory.IT);
        engineBuilder.addComponentWithDao(AnimationComponent.class, DaoOfAnimationComponentFactory.IT);
    }

    public TextureRegion getRegion(Entity representation) {
        int id = representation.getId();
        TextureRegion textureRegion = textureRegions.get(id);
        if (textureRegion != null) {
            return textureRegion;
        }

        TextureRegionComponent textureFragmentComponent = representation.get(TextureRegionComponent.class);
        String texturePath = textureFragmentComponent.getTexture().get(TextureComponent.class).getTexturePath();

        Texture texture = textures.computeIfAbsent(texturePath, this::loadTexture);
        textureRegion = new TextureRegion(
                texture,
                textureFragmentComponent.getX(),
                textureFragmentComponent.getY(),
                textureFragmentComponent.getWidth(),
                textureFragmentComponent.getHeight()
        );
        textureRegions.put(id, textureRegion);
        return textureRegion;
    }

    private Texture loadTexture(String s) {
        assetManager.load(s, Texture.class);
        assetManager.finishLoading();
        return assetManager.get(s, Texture.class);
    }
}

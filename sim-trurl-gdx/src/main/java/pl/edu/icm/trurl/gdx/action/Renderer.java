package pl.edu.icm.trurl.gdx.action;


import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.snowyhollows.bento.annotation.ImplementationSwitch;

@ImplementationSwitch(
        cases = @ImplementationSwitch.When(
                name = "default", useByDefault = true, implementation = DefaultRenderer.class
        )
)
public interface Renderer {
    void clear();

    void render();

    boolean isInFrustum(float centerX, float centerY, float width, float height);

    void addTextureRegion(TextureRegion textureRegion,
                          float x,
                          float y,
                          float width,
                          float height,
                          float scale,
                          float rotation,
                          float alpha,
                          boolean horizontalFlip);
}

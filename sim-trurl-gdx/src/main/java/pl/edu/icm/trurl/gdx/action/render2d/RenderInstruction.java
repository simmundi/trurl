package pl.edu.icm.trurl.gdx.action.render2d;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pl.edu.icm.trurl.ecs.dao.annotation.Blob;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
public class RenderInstruction {
    @Blob
    private TextureRegion textureRegion;
    private float x;
    private float y;
    private float width;
    private float height;
    private float scale;
    private float rotation;
    private float alpha;
    private boolean horizontalFlip;

    public RenderInstruction() {
    }

    public RenderInstruction(TextureRegion textureRegion, float x, float y, float width, float height, float scale, int rotation, float alpha, boolean horizontalFlip) {
        this.textureRegion = textureRegion;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.rotation = rotation;
        this.alpha = alpha;
        this.horizontalFlip = horizontalFlip;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public boolean isHorizontalFlip() {
        return horizontalFlip;
    }

    public void setHorizontalFlip(boolean horizontalFlip) {
        this.horizontalFlip = horizontalFlip;
    }
}

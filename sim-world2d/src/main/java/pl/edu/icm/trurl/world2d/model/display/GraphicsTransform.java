package pl.edu.icm.trurl.world2d.model.display;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
public class GraphicsTransform {
    public final static byte FLIP_HORIZONTAL =  0b00000001;
    public final static byte FLIP_VERTICAL =  0b00000010;

    private byte transformMask;
    private float alpha = 1;
    private float rotation = 0;
    private float scale = 1;
    private float animationOffset = 0;

    byte getTransformMask() {
        return transformMask;
    }

    void setTransformMask(byte transformMask) {
        this.transformMask = transformMask;
    }

    public boolean isHorizontalFlip() {
        return (transformMask & FLIP_HORIZONTAL) != 0;
    }

    public void setHorizontalFlip(boolean horizontalFlip) {
        if (horizontalFlip) {
            transformMask |= FLIP_HORIZONTAL;
        } else {
            transformMask &= ~FLIP_HORIZONTAL;
        }
    }

    public boolean isVerticalFlip() {
        return (transformMask & FLIP_VERTICAL) != 0;
    }

    public void setVerticalFlip(boolean verticalFlip) {
        if (verticalFlip) {
            transformMask |= FLIP_VERTICAL;
        } else {
            transformMask &= ~FLIP_VERTICAL;
        }
    }

    public static boolean isHorizontalFlip(byte transformMask) {
        return (transformMask & FLIP_HORIZONTAL) != 0;
    }

    public static boolean isVerticalFlip(byte transformMask) {
        return (transformMask & FLIP_VERTICAL) != 0;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getAnimationOffset() {
        return animationOffset;
    }

    public void setAnimationOffset(float animationOffset) {
        this.animationOffset = animationOffset;
    }
}

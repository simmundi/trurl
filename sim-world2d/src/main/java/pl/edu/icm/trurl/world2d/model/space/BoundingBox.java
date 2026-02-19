package pl.edu.icm.trurl.world2d.model.space;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.Objects;

import static java.lang.Math.signum;

@WithDao
public class BoundingBox {
    private float centerX;
    private float centerY;
    private float width;
    private float height;

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public float getMinX() {
        return centerX - width / 2;
    }

    public float getMaxX() {
        return centerX + width / 2;
    }

    public float getMinY() {
        return centerY - height / 2;
    }

    public float getMaxY() {
        return centerY + height / 2;
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

    public void moveX(float dx) {
        centerX += dx;
    }

    public void moveY(float dy) {
        centerY += dy;
    }

    public void move(float dx, float dy) {
        centerX += dx;
        centerY += dy;
    }

    public void move(Velocity velocity) {
        centerX += velocity.getDx();
        centerY += velocity.getDy();
    }

    public float overlapX(BoundingBox other) {
        return Math.min(getMaxX(), other.getMaxX()) - Math.max(getMinX(), other.getMinX());
    }

    public float overlapY(BoundingBox other) {
        return Math.min(getMaxY(), other.getMaxY()) - Math.max(getMinY(), other.getMinY());
    }

    public void moveBy(Velocity velocity) {
        centerX += velocity.getDx();
        centerY += velocity.getDy();
    }

    public void moveBy(Velocity velocity, float fraction) {
        centerX += velocity.getDx() * fraction;
        centerY += velocity.getDy() * fraction;
    }
    public boolean overlapsBox(BoundingBox other) {
        return (other.getMaxX() > getMinX() && other.getMinX() < getMaxX() && other.getMaxY() > getMinY() && other.getMinY() < getMaxY());
    }

    public boolean overlapsBox(float centerX, float width, float centerY, float height) {
        return (centerX + width / 2 > getMinX() && centerX - width / 2 < getMaxX() && centerY + height / 2 > getMinY() && centerY - height / 2 < getMaxY());
    }

    public boolean overlapsMinMax(float minX, float maxX, float minY, float maxY) {
        return overlapsMinMax(minX, maxX, minY, maxY, getMinX(), getMaxX(), getMinY(), getMaxY());
    }

    public static boolean overlapsMinMax(float minX, float maxX, float minY, float maxY, float minX2, float maxX2, float minY2, float maxY2) {
        return (maxX2 > minX && minX2 < maxX && maxY2 > minY && minY2 < maxY);
    }

    public static boolean overlapsBox(float centerX, float width, float centerY, float height, float minX, float maxX, float minY, float maxY) {
        return (centerX + width / 2 > minX && centerX - width / 2 < maxX && centerY + height / 2 > minY && centerY - height / 2 < maxY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundingBox that = (BoundingBox) o;
        return Float.compare(centerX, that.centerX) == 0 && Float.compare(centerY, that.centerY) == 0 && Float.compare(width, that.width) == 0 && Float.compare(height, that.height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(centerX, centerY, width, height);
    }
}

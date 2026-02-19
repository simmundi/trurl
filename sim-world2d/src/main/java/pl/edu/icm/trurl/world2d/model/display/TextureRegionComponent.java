package pl.edu.icm.trurl.world2d.model.display;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.Objects;

@WithDao(namespace = "region")
public class TextureRegionComponent {
    private Entity texture;
    private short x;
    private short y;
    private short width;
    private short height;

    public TextureRegionComponent() {
    }

    public TextureRegionComponent(Entity texture, short x, short y, short width, short height) {
        this.texture = texture;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Entity getTexture() {
        return texture;
    }

    public void setTexture(Entity texture) {
        this.texture = texture;
    }

    public short getX() {
        return x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return y;
    }

    public void setY(short y) {
        this.y = y;
    }

    public short getWidth() {
        return width;
    }

    public void setWidth(short width) {
        this.width = width;
    }

    public short getHeight() {
        return height;
    }

    public void setHeight(short height) {
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextureRegionComponent that = (TextureRegionComponent) o;
        return x == that.x && y == that.y && width == that.width && height == that.height && Objects.equals(texture, that.texture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texture, x, y, width, height);
    }
}

package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Objects;

@WithMapper
public class Looks {
    private Color color;
    private Texture texture;

    public Looks() {
    }

    public Looks(Color color, Texture texture) {
        this.color = color;
        this.texture = texture;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Looks looks = (Looks) o;
        return color == looks.color && texture == looks.texture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, texture);
    }
}

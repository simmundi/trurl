package pl.edu.icm.trurl.world2d.model.display;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.Objects;

@WithDao(namespace = "texture")
public class TextureComponent {
    private String texturePath;

    public String getTexturePath() {
        return texturePath;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextureComponent that = (TextureComponent) o;
        return Objects.equals(texturePath, that.texturePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texturePath);
    }
}

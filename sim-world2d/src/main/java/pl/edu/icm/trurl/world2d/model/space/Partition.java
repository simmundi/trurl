package pl.edu.icm.trurl.world2d.model.space;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.ArrayList;
import java.util.List;

@WithDao(namespace = "partition")
public class Partition {
    private Type type;
    private List<Entity> entities = new ArrayList<>();

    public List<Entity> getEntities() {
        return entities;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        SPATIAL, OTHER
    }
}

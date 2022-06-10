package pl.edu.icm.trurl.store.attribute;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

public interface EntityAttribute extends Attribute {
    Entity getEntity(int row, Session session);
    void setEntity(int row, Entity value);
    void setId(int row, int id);
    int getId(int row);
    boolean isEqual(int row, Entity other);
}

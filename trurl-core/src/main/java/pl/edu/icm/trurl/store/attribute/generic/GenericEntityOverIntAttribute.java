package pl.edu.icm.trurl.store.attribute.generic;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;

public final class GenericEntityOverIntAttribute implements EntityAttribute {

    private final IntAttribute wrappedAttribute;

    public GenericEntityOverIntAttribute(IntAttribute wrappedAttribute) {
        this.wrappedAttribute = wrappedAttribute;
    }

    @Override
    public void ensureCapacity(int capacity) {
        wrappedAttribute.ensureCapacity(capacity);
    }

    @Override
    public boolean isEmpty(int row) {
        return wrappedAttribute.isEmpty(row);
    }

    @Override
    public void setEmpty(int row) {
        wrappedAttribute.setEmpty(row);
    }

    @Override
    public String name() {
        return wrappedAttribute.name();
    }

    @Override
    public String getString(int row) {
        return EntityEncoder.encodeId(getId(row));
    }

    @Override
    public void setString(int row, String value) {
        wrappedAttribute.setInt(row, EntityEncoder.decodeId(value));
    }

    @Override
    public Entity getEntity(int row, Session session) {
        return isEmpty(row) ? null : session.getEntity(getId(row));
    }

    @Override
    public void setEntity(int row, Entity value) {
        if (value != null) {
            setId(row, value.getId());
        } else {
            setEmpty(row);
        }
    }

    @Override
    public void setId(int row, int id) {
        wrappedAttribute.setInt(row, id);
    }

    @Override
    public int getId(int row) {
        return wrappedAttribute.getInt(row);
    }

    @Override
    public boolean isEqual(int row, Entity other) {
        boolean missing = isEmpty(row);
        return missing && other == null
                || other != null && other.getId() == getId(row);
    }
}

package pl.edu.icm.trurl.store.attribute.generic;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

public final class GenericEntityOverStringAttribute implements EntityAttribute {

    private final StringAttribute wrappedAttribute;

    public GenericEntityOverStringAttribute(StringAttribute wrappedAttribute) {
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
        return wrappedAttribute.getString(row);
    }

    @Override
    public void setString(int row, String value) {
        wrappedAttribute.setString(row, value);
    }

    @Override
    public Entity getEntity(int row, Session session) {
        return EntityEncoder.decode(getString(row), session);
    }

    @Override
    public void setEntity(int row, Entity value) {
        setString(row, EntityEncoder.encode(value));
    }

    @Override
    public void setId(int row, int id) {
        setString(row, EntityEncoder.encodeId(id));
    }

    @Override
    public int getId(int row) {
        return EntityEncoder.decodeId(getString(row));
    }

    @Override
    public boolean isEqual(int row, Entity other) {
        boolean missing = isEmpty(row);
        return missing && other == null
                || other != null && EntityEncoder.encodeId(other.getId()).equals(getString(row));
    }
}

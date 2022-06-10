package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;
import tech.tablesaw.api.TextColumn;

public class TablesawEntityAttribute extends TablesawAttribute<TextColumn> implements EntityAttribute {
    public TablesawEntityAttribute(String name) {
        super(TextColumn.create(name));
    }

    public TablesawEntityAttribute(String name, int size) {
        super(TextColumn.create(name, size));
    }

    public Entity getEntity(int row, Session session) {
        return EntityEncoder.decode(column().getString(row), session);
    }
    public void setEntity(int row, Entity value) {
        column().set(row, EntityEncoder.encode(value));
    }

    @Override
    public void setId(int row, int id) {
        column().set(row, EntityEncoder.encodeId(id));
    }

    @Override
    public int getId(int row) {
        return EntityEncoder.decodeId(column().get(row));
    }

    @Override
    public boolean isEqual(int row, Entity other) {
        boolean missing = column().isMissing(row);
        return missing && other == null
                || other != null && EntityEncoder.encodeId(other.getId()).equals(column().get(row));
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, value);
    }

    @Override
    String getNotBlankString(int row) {
        return column().getString(row);
    }
}

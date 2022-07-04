package pl.edu.icm.trurl.store.tablesaw.attribute;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.IntSource;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;
import pl.edu.icm.trurl.store.attribute.ValueObjectListAttribute;
import tech.tablesaw.api.TextColumn;
//todo implement this
public class TablesawValueObjectListAttribute extends TablesawAttribute<TextColumn> implements ValueObjectListAttribute {
    public TablesawValueObjectListAttribute(String name) {
        super(TextColumn.create(name));
    }

    public TablesawValueObjectListAttribute(String name, int size) {
        super(TextColumn.create(name, size));
    }

    public Entity getEntity(int row, Session session) {
        return EntityEncoder.decode(column().getString(row), session);
    }
    public void setEntity(int row, Entity value) {
        column().set(row, EntityEncoder.encode(value));
    }


    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, value);
    }

    @Override
    String getNotBlankString(int row) {
        return column().getString(row);
    }

    @Override
    public int getSize(int row) {
        return 0;
    }

    @Override
    public void loadIds(int row, IntSink ids) {

    }

    @Override
    public void saveIds(int row, int size, IntSource ids) {

    }

    @Override
    public boolean isEqual(int row, int size, IntSource ids) {
        return false;
    }
}

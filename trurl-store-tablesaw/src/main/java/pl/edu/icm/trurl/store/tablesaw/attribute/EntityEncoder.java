package pl.edu.icm.trurl.store.tablesaw.attribute;

import com.google.common.base.Strings;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

public final class EntityEncoder {
    private EntityEncoder() {
    }

    static String encode(Entity entity) {
        return entity != null ? Integer.toString(entity.getId(), 36) : null;
    }

    static Entity decode(String id, Session session) {
        return Strings.isNullOrEmpty(id) ? null : session.getEntity(Integer.parseInt(id, 36));
    }

    static String encodeId(int id) {
        return id == Entity.NULL_ID ? null : Integer.toString(id, 36);
    }

    static int decodeId(String id) {
        return Strings.isNullOrEmpty(id) ? Entity.NULL_ID : Integer.parseInt(id, 36);
    }
}

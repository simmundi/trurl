package pl.edu.icm.trurl.store.attribute.generic;

import com.google.common.base.Strings;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class EntityEncoder {
    private EntityEncoder() {
    }

    static String listOfEntitiesToString(List<Entity> entities) {
        return entities.stream()
                .map(EntityEncoder::encode)
                .collect(Collectors.joining(","));
    }

    static void appendEntitiesFromString(Session session, Collection<Entity> entities, String encodedEntities) {
        Arrays.stream(encodedEntities.split(","))
                .filter(s -> !Strings.isNullOrEmpty(s))
                .map(s -> EntityEncoder.decode(s, session))
                .forEach(entities::add);
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

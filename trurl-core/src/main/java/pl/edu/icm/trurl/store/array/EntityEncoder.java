package pl.edu.icm.trurl.store.array;

import com.google.common.base.Strings;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.IntSource;

import java.io.IOException;

final class EntityEncoder {
    private EntityEncoder() {
    }

    static void encode(IntSource intSource, int size, Appendable appendable) {
        try {
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    appendable.append(',');
                }
                appendable.append(encode(intSource.getInt(i)));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static int countIds(CharSequence encoded) {
        int length = encoded.length();
        int counter = 0;
        for (int i = 0; i < length; i++) {
            if (encoded.charAt(i) == ',') {
                counter++;
            }
        }
        return counter;
    }

    static void decode(IntSink intSink, CharSequence encoded) {
        int lastEnd = 0;
        int length = encoded.length();
        int index = 0;
        for (int i = 0; i < length; i++) {
            if (encoded.charAt(i) == ',') {
                intSink.setInt(index++, parseInt(encoded, lastEnd, i));
                lastEnd = i;
            }
        }
    }

    static String encode(Entity entity) {
        return entity != null ? Integer.toString(entity.getId(), 36) : null;
    }

    static Entity decode(String id, Session session) {
        return Strings.isNullOrEmpty(id) ? null : session.getEntity(Integer.parseInt(id, 36));
    }

    static String encode(int id) {
        return id == Entity.NULL_ID ? null : Integer.toString(id, 36);
    }

    static int decode(String id) {
        return Strings.isNullOrEmpty(id) ? Entity.NULL_ID : Integer.parseInt(id, 36);
    }


    public static int parseInt(CharSequence s, int beginIndex, int endIndex) {
        int result = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            result *= 36;
            result += Character.digit(s.charAt(i), 36);
        }
        return result;
    }
}
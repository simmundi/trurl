/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.store.array;

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
        return isNullOrEmpty(id) ? null : session.getEntity(Integer.parseInt(id, 36));
    }

    static String encode(int id) {
        return id == Entity.NULL_ID ? null : Integer.toString(id, 36);
    }

    static int decode(String id) {
        return isNullOrEmpty(id) ? Entity.NULL_ID : Integer.parseInt(id, 36);
    }

    static int decode(String id, int from, int to) {
        return isNullOrEmpty(id) ? Entity.NULL_ID : Integer.parseInt(id, 36);
    }

    public static int parseInt(CharSequence s, int beginIndex, int endIndex) {
        int result = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            result *= 36;
            result += Character.digit(s.charAt(i), 36);
        }
        return result;
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}

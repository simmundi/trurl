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

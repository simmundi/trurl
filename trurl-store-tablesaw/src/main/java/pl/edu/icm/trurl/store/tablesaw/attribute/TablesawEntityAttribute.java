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

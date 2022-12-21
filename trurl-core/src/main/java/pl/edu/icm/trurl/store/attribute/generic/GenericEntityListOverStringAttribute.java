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

package pl.edu.icm.trurl.store.attribute.generic;

import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.IntSource;
import pl.edu.icm.trurl.store.attribute.EntityListAttribute;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

import java.util.StringJoiner;
import java.util.regex.Pattern;

public final class GenericEntityListOverStringAttribute implements EntityListAttribute {

    private static final Pattern splitter = Pattern.compile(",");
    private final StringAttribute wrappedAttribute;

    public GenericEntityListOverStringAttribute(StringAttribute wrappedAttribute) {
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
    public int getSize(int row) {
        String result = wrappedAttribute.getString(row);
        return result == null ? -1 : result.split(",").length;
    }

    @Override
    public void loadIds(int row, IntSink ids) {
        String idString = wrappedAttribute.getString(row);
        if ("".equals(idString)) {
            return;
        }
        String[] split = splitter.split(idString);
        for (int i = 0; i < split.length; i++) {
            ids.setInt(i, EntityEncoder.decodeId(split[i]));
        }
    }

    @Override
    public void saveIds(int row, int count, IntSource ids) {
        wrappedAttribute.setString(row, stringFromIds(count, ids));
    }


    public StringAttribute getWrappedAttribute() {
        return this.wrappedAttribute;
    }

    @Override
    public boolean isEqual(int row, int size, IntSource ids) {
        return stringFromIds(size, ids).equals(wrappedAttribute.getString(row));
    }

    private String stringFromIds(int count, IntSource ids) {
        if (count == 0) {
            return "";
        } else if (count == 1) {
            return EntityEncoder.encodeId(ids.getInt(0));
        } else {
            StringJoiner stringJoiner = new StringJoiner(",");
            for (int i = 0; i < count; i++) {
                stringJoiner.add(EntityEncoder.encodeId(ids.getInt(i)));
            }
            return stringJoiner.toString();
        }
    }

}

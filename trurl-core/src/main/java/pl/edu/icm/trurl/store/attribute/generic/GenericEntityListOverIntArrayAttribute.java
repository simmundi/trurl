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
import pl.edu.icm.trurl.store.attribute.IntListAttribute;

public final class GenericEntityListOverIntArrayAttribute implements EntityListAttribute {

    private final IntListAttribute wrappedAttribute;

    public GenericEntityListOverIntArrayAttribute(IntListAttribute wrappedAttribute) {
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
        return wrappedAttribute.getSize(row);
    }

    @Override
    public void loadIds(int row, IntSink ids) {
        wrappedAttribute.loadInts(row, ids);
    }

    @Override
    public void saveIds(int row, int size, IntSource ids) {
        wrappedAttribute.saveInts(row, size, ids);
    }

    @Override
    public boolean isEqual(int row, int size, IntSource ids) {
        return wrappedAttribute.isEqual(row, size, ids);
    }
}

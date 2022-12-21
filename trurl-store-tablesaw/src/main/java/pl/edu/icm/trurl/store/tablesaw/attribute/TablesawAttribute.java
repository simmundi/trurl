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

import pl.edu.icm.trurl.store.attribute.Attribute;
import tech.tablesaw.columns.Column;

public abstract class TablesawAttribute<E extends Column<?>> implements Attribute {
    private final E column;
    public TablesawAttribute(E column) {
        this.column = column;
    }
    public int capacity() {
        return column.size();
    }
    public void addRows() {
        column.appendMissing();
    }
    public E column() {
        return column;
    }
    public String name() {
        return column.name();
    }

    @Override
    public boolean isEmpty(int row) {
        return row >= column.size() || column.isMissing(row);
    }

    @Override
    public final void setString(int row, String value) {
        if (value == null || value.equals("")) {
            column.setMissing(row);
        } else {
            setNotBlankString(row, value);
        }
    }

    @Override
    public final String getString(int row) {
        return isEmpty(row) ? "" : getNotBlankString(row);
    }

    abstract void setNotBlankString(int row, String value);

    abstract String getNotBlankString(int row);

    @Override
    public void ensureCapacity(int capacity) {
        while (column().size() < capacity) {
            column().appendMissing();
        }
    }

    @Override
    public void setEmpty(int row) {
        column.setMissing(row);
    }
}

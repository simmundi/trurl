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

import pl.edu.icm.trurl.store.attribute.BooleanAttribute;
import tech.tablesaw.api.BooleanColumn;

public class TablesawBooleanAttribute extends TablesawAttribute<BooleanColumn> implements BooleanAttribute {
    public TablesawBooleanAttribute(String name) {
        super(BooleanColumn.create(name));
    }

    public TablesawBooleanAttribute(String name, int initialSize) {
        super(BooleanColumn.create(name, initialSize));
    }

    public boolean getBoolean(int row) {
        return column().get(row);
    }

    public void setBoolean(int row, boolean value) {
        column().set(row, value);
    }

    @Override
    String getNotBlankString(int row) {
        return Boolean.toString(getBoolean(row));
    }

    @Override
    void setNotBlankString(int row, String value) {
        setBoolean(row, Boolean.parseBoolean(value));
    }

    @Override
    public void setEmpty(int row) {
        column().setMissing(row);
    }

}

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

import pl.edu.icm.trurl.store.attribute.IntAttribute;
import tech.tablesaw.api.IntColumn;

public class TablesawIntAttribute extends TablesawAttribute<IntColumn> implements IntAttribute {
    public TablesawIntAttribute(String name) {
        super(IntColumn.create(name));
    }

    public TablesawIntAttribute(String name, int initialSize) {
        super(IntColumn.create(name, initialSize));
    }

    public int getInt(int row) {
        return column().getInt(row);
    }
    public void setInt(int row, int value) {
        column().set(row, value);
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, Integer.parseInt(value));
    }

    @Override
    String getNotBlankString(int row) {
        return Integer.toString(column().get(row));
    }
}

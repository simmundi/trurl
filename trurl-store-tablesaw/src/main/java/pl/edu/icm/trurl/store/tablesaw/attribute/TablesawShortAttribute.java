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

import pl.edu.icm.trurl.store.attribute.ShortAttribute;
import tech.tablesaw.api.ShortColumn;

public class TablesawShortAttribute extends TablesawAttribute<ShortColumn> implements ShortAttribute {
    public TablesawShortAttribute(String name) {
        super(ShortColumn.create(name));
    }

    public TablesawShortAttribute(String name, int initialSize) {
        super(ShortColumn.create(name, initialSize));
    }

    public short getShort(int row) {
        return column().getShort(row);
    }
    public void setShort(int row, short value) {
        column().set(row, value);
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, Short.parseShort(value));
    }

    @Override
    String getNotBlankString(int row) {
        return Short.toString(column().getShort(row));
    }
}

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

import pl.edu.icm.trurl.store.attribute.DoubleAttribute;
import tech.tablesaw.api.DoubleColumn;

public class TablesawDoubleAttribute extends TablesawAttribute<DoubleColumn> implements DoubleAttribute {
    public TablesawDoubleAttribute(String name) {
        super(DoubleColumn.create(name));
    }

    public TablesawDoubleAttribute(String name, int initialSize) {
        super(DoubleColumn.create(name, initialSize));
    }

    public double getDouble(int row) {
        return column().getDouble(row);
    }
    public void setDouble(int row, double value) {
        column().set(row, value);
    }

    @Override
    void setNotBlankString(int row, String value) {
        column().set(row, Double.parseDouble(value));
    }

    @Override
    String getNotBlankString(int row) {
        return Double.toString(row);
    }
}

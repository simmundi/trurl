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
import net.snowyhollows.bento.soft.SoftEnum;
import net.snowyhollows.bento.soft.SoftEnumManager;
import pl.edu.icm.trurl.store.attribute.SoftEnumAttribute;
import tech.tablesaw.api.StringColumn;

public class TablesawSoftEnumAttribute<E extends SoftEnum> extends TablesawAttribute<StringColumn> implements SoftEnumAttribute<E> {

    private final SoftEnumManager<E> converter;
    private E[] values;

    public TablesawSoftEnumAttribute(SoftEnumManager<E> enumType, String name) {
        this(enumType, name, 0);
    }

    public TablesawSoftEnumAttribute(SoftEnumManager<E> enumType, String name, int size) {
        super(StringColumn.create(name, size));
        converter = enumType;
        values = enumType.values().toArray(enumType.emptyArray());
    }

    public E getEnum(int row) {
        String literal = column().get(row);
        return Strings.isNullOrEmpty(literal) ? null : converter.getByName(column().get(row));
    }
    public void setEnum(int row, E value) {
        column().set(row, value == null ? null : value.name());
    }

    @Override
    public byte getOrdinal(int row) {
        return (byte) getEnum(row).ordinal();
    }

    @Override
    public void setOrdinal(int row, byte value) {
        setEnum(row, values[value]);
    }

    @Override
    public E[] values() {
        return values;
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

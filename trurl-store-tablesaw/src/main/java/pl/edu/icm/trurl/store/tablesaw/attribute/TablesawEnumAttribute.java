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

import com.google.common.base.Converter;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;
import tech.tablesaw.api.StringColumn;

public class TablesawEnumAttribute<E extends Enum<E>> extends TablesawAttribute<StringColumn> implements EnumAttribute<E> {

    private final Converter<String, E> converter;
    private E[] values;

    public TablesawEnumAttribute(Class<E> enumType, String name) {
        super(StringColumn.create(name));
        converter = Enums.stringConverter(enumType);
        values = enumType.getEnumConstants();
    }

    public TablesawEnumAttribute(Class<E> enumType, String name, int size) {
        super(StringColumn.create(name, size));
        converter = Enums.stringConverter(enumType);
        values = enumType.getEnumConstants();
    }

    public E getEnum(int row) {
        String literal = column().get(row);
        return Strings.isNullOrEmpty(literal) ? null : converter.convert(column().get(row));
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

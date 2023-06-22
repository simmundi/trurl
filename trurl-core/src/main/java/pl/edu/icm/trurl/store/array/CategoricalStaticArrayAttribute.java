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

package pl.edu.icm.trurl.store.array;

import com.google.common.base.Converter;
import com.google.common.base.Enums;
import com.google.common.base.Strings;
import pl.edu.icm.trurl.store.attribute.CategoricalStaticAttribute;

import java.util.Arrays;

final public class CategoricalStaticArrayAttribute<T extends Enum<T>> implements CategoricalStaticAttribute<T> {
    private final String name;
    private final Converter<String, T> converter;
    private byte[] values;
    private static byte NULL = Byte.MIN_VALUE;
    private T[] instances;
    private int capacity;

    public CategoricalStaticArrayAttribute(Class<T> enumType, String name, int capacity) {
        this.name = name;
        values = new byte[0];
        converter = Enums.stringConverter(enumType);
        instances = enumType.getEnumConstants();
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            byte[] bigger = Arrays.copyOf(values, target);
            Arrays.fill(bigger, values.length, target, NULL);
            this.values = bigger;
        }
    }

    @Override
    public boolean isEmpty(int row) {
        return row >= values.length || values[row] == NULL;
    }

    @Override
    public void setEmpty(int row) {
        values[row] = NULL;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String getString(int row) {
        byte ordinal = values[row];
        return ordinal >= 0 ? instances[ordinal].name() : "";
    }

    @Override
    public void setString(int row, String value) {
        values[row] = Strings.isNullOrEmpty(value) ? Byte.MIN_VALUE : (byte) converter.convert(value).ordinal();
    }

    @Override
    public T getEnum(int row) {
        byte ordinal = values[row];
        return ordinal == Byte.MIN_VALUE ? null : instances[ordinal];
    }
    @Override
    public void setEnum(int row, T value) {
        setOrdinal(row, value != null ? (byte) value.ordinal() : Byte.MIN_VALUE);
    }

    @Override
    public byte getOrdinal(int row) {
        return values[row];
    }

    @Override
    public void setOrdinal(int row, byte value) {
        values[row] = value;
    }

    @Override
    public T[] values() {
        return instances;
    }
}

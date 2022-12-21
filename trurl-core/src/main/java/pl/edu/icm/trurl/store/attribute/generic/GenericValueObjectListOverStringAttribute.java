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
import pl.edu.icm.trurl.store.attribute.StringAttribute;
import pl.edu.icm.trurl.store.attribute.ValueObjectListAttribute;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.joining;

public class GenericValueObjectListOverStringAttribute implements ValueObjectListAttribute {
    private final StringAttribute wrappedAttribute;
    private static final Pattern splitter = Pattern.compile(",");

    public GenericValueObjectListOverStringAttribute(StringAttribute stringAttribute) {
        this.wrappedAttribute = stringAttribute;
    }

    @Override
    public void ensureCapacity(int capacity) {
        wrappedAttribute.ensureCapacity(capacity);
    }

    @Override
    public boolean isEmpty(int row) {
        if (wrappedAttribute.isEmpty(row))
            return true;
        String result = wrappedAttribute.getString(row);
        if (result == null) return true;
        String[] split = splitter.split(result);
        return split.length == 0 || EntityEncoder.decodeId(split[0]) <= 0;
    }

    @Override
    public void setEmpty(int row) {
        String result = wrappedAttribute.getString(row);
        if (result == null) return;
        String[] split = splitter.split(result);
        if (split.length == 0) return;
        StringJoiner stringJoiner = new StringJoiner(splitter.pattern());
        int firstVal = EntityEncoder.decodeId(split[0]);
        if (firstVal > 0)
            split[0] = EntityEncoder.encodeId(-firstVal);
        for (String s : split)
            stringJoiner.add(s);
        wrappedAttribute.setString(row, stringJoiner.toString());
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
        if (result == null) return -1;
        String[] split = splitter.split(result);

        int i = 0;
        for (; i < split.length; i++) {
            int value = EntityEncoder.decodeId(split[i]);
            if (value <= 0) break;
        }

        return i;
    }

    @Override
    public void loadIds(int row, IntSink intSink) {
        String idString = wrappedAttribute.getString(row);
        if ("".equals(idString)) {
            return;
        }
        String[] split = splitter.split(idString);
        for (int i = 0; i < split.length; i++) {
            int value = EntityEncoder.decodeId(split[i]);
            if (value <= 0) break;
            intSink.setInt(i, value);
        }
    }

    @Override
    public int saveIds(int row, int size, int firstNewIndex) {
        checkArgument(firstNewIndex > 0, "Index should be greater than 0. 0 >= " + firstNewIndex);
        checkArgument(size >= 0, "Size should be greater or equal 0. 0 > " + size);
        String stringRow = wrappedAttribute.getString(row);
        String[] stringValues = splitter.split(stringRow != null ? stringRow : "");
        int[] ints = Arrays.stream(stringValues)
                .filter(s -> !s.equals(""))
                .mapToInt(EntityEncoder::decodeId)
                .toArray();
        int oldLength = ints.length;
        if (size <= oldLength) {
            for (int i = 0; i < size; i++)
                ints[i] = Math.abs(ints[i]);
            if (size < oldLength) ints[size] = -Math.abs(ints[size]);
            wrappedAttribute.setString(row,
                    Arrays.stream(ints)
                            .mapToObj(EntityEncoder::encodeId)
                            .collect(joining(splitter.pattern())));
            return firstNewIndex;
        } else {
            ints = Arrays.copyOf(ints, (int) Math.max(size, ints.length * 1.5));
            for (int i = 0; i < oldLength; i++) {
                ints[i] = Math.abs(ints[i]);
            }
            for (int i = 0; i < ints.length - oldLength; i++) {
                ints[oldLength + i] = firstNewIndex + i;
            }
            if (size < ints.length)
                ints[size] = -Math.abs(ints[size]);
            wrappedAttribute.setString(row,
                    Arrays.stream(ints)
                            .mapToObj(EntityEncoder::encodeId)
                            .collect(joining(splitter.pattern())));
            return Math.abs(ints[ints.length - 1]) + 1;
        }
    }
}

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

import com.google.common.base.Strings;
import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.IntSource;
import pl.edu.icm.trurl.store.attribute.IntListAttribute;

import java.util.Arrays;
import java.util.regex.Pattern;

public class IntListArrayAttribute implements IntListAttribute {
    private final String name;
    private int capacity;
    private int[][] values;
    private Pattern splitter = Pattern.compile(",");

    public IntListArrayAttribute(String name, int capacity) {
        this.name = name;
        this.values = new int[0][0];
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            int[][] bigger = Arrays.copyOf(values, target);
            this.values = bigger;
        }
    }

    @Override
    public boolean isEmpty(int row) {
        return row >= values.length || values[row] == null;
    }

    @Override
    public void setEmpty(int row) {
        values[row] = null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String getString(int row) {
        int[] ints = values[row];
        if (ints.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(EntityEncoder.encode(ints[0]));
        for (int i = 1; i < ints.length; i++) {
            sb.append(',');
            sb.append(EntityEncoder.encode(ints[i]));
        }
        return sb.toString();
    }

    @Override
    public void setString(int row, String value) {
        if (!Strings.isNullOrEmpty(value)) {
            String[] split = splitter.split(value);
            int[] result = values[row];
            if (result == null || result.length != split.length) {
                result = new int[split.length];
                values[row] = result;
            }
            for (int i = 0; i < split.length; i++) {
                result[i] = EntityEncoder.decode(split[i]);
            }
        } else {
            setEmpty(row);
        }
    }

    @Override
    public int getSize(int row) {
        int[] result = values[row];
        return result == null ? -1 : result.length;
    }

    @Override
    public void loadInts(int row, IntSink ints) {
        int[] result = values[row];
        if (result != null) {
            for (int i = 0; i < result.length; i++) {
                ints.setInt(i, result[i]);
            }
        }
    }

    @Override
    public void saveInts(int row, int size, IntSource ints) {
        int[] result = values[row];
        if (result == null || result.length != size) {
            result = new int[size];
            values[row] = result;
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = ints.getInt(i);
        }
    }

    @Override
    public boolean isEqual(int row, int size, IntSource ints) {
        int[] result = values[row];
        if (result == null || result.length != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (result[i] != ints.getInt(i)) {
                return false;
            }
        }
        return true;
    }
}

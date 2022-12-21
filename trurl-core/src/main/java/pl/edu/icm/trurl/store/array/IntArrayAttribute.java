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
import pl.edu.icm.trurl.store.attribute.IntAttribute;

import java.util.Arrays;

final public class IntArrayAttribute implements IntAttribute {
    private String name;
    private int capacity;
    private int[] values;
    private final static int NULL = Integer.MIN_VALUE;

    public IntArrayAttribute(String name, int capacity) {
        this.name = name;
        values = new int[0];
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            int[] bigger = Arrays.copyOf(values, target);
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
        return Integer.toString(getInt(row));
    }

    @Override
    public void setString(int row, String value) {
        setInt(row, Strings.isNullOrEmpty(value) ? Integer.MIN_VALUE : Integer.parseInt(value));
    }

    @Override
    public int getInt(int row) {
        return values[row];
    }

    @Override
    public void setInt(int row, int value) {
        values[row] = value;
    }
}

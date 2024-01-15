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

import pl.edu.icm.trurl.store.attribute.StringAttribute;

import java.util.ArrayList;
import java.util.Collections;

final public class StringArrayAttribute implements StringAttribute {
    private final String name;
    private final ArrayList<String> strings;
    private int capacity;

    public StringArrayAttribute(String name, int capacity) {
        this.name = name;
        this.strings = new ArrayList<>(capacity);
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            this.strings.ensureCapacity(target);
            this.strings.addAll(Collections.nCopies(target - strings.size(), null));
        }
    }

    @Override
    public boolean isEmpty(int row) {
        return row >= strings.size() || isNullOrEmpty(strings.get(row));
    }

    @Override
    public void setEmpty(int row) {
        strings.set(row, null);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String getString(int row) {
        String value = strings.get(row);
        return isNullOrEmpty(value) ? "" : value;
    }

    @Override
    public void setString(int row, String value) {
        strings.set(row, value);
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}

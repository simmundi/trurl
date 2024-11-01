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

package pl.edu.icm.trurl.store.basic;

import pl.edu.icm.trurl.store.attribute.ObjectAttribute;

import java.util.ArrayList;
import java.util.Collections;

final public class BasicObjectAttribute implements ObjectAttribute {
    private final String name;
    private final ArrayList objects;
    private int capacity;

    public BasicObjectAttribute(String name, int capacity) {
        this.name = name;
        this.objects = new ArrayList<>(capacity);
        ensureCapacity(capacity);
    }

    @Override
    public void ensureCapacity(int capacity) {
        if (capacity > this.capacity) {
            int target = (int) Math.max(capacity, this.capacity * 1.5);
            this.capacity = target;
            this.objects.ensureCapacity(target);
            this.objects.addAll(Collections.nCopies(target - objects.size(), null));
        }
    }

    @Override
    public boolean isEmpty(int row) {
        return row >= objects.size() || objects.get(row) == null;
    }

    @Override
    public void setEmpty(int row) {
        objects.set(row, null);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Object getObject(int row) {
        return objects.get(row);
    }

    @Override
    public String getString(int row) {
        // noop
        return null;
    }

    @Override
    public void setString(int row, String value) {
        // noop
    }

    @Override
    public void setObject(int row, Object value) {
        objects.set(row, value);
    }
}

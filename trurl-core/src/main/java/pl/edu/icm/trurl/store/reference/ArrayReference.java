/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.store.reference;

import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreInspector;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.IntListAttribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class ArrayReference implements Reference {
    private final String name;
    private final IntListAttribute values;
    private final int minimum;
    private final int margin;
    private final StoreInspector store;

    public ArrayReference(Store store, String name, int minimum, int margin) {
        this.store = store;
        this.name = name;
        this.minimum = minimum;
        this.margin = margin;
        store.addIntList(name);
        store.hideAttribute(name);
        this.values = store.get(name);
    }

    @Override
    public int getId(int row, int index) {
        int[] array = values.getInts(row);
        if (array == null || index >= array.length) {
            return Integer.MIN_VALUE;
        }
        return array[index];
    }

    public void setId(int row, int index, int value) {
        values.getInts(row)[index] = value;
    }

    @Override
    public void setSize(int row, int size) {
        int sizeWithMargin = Math.max(size + margin, minimum);

        int[] array = values.getInts(row);

        if (array == null) {
            array = new int[sizeWithMargin];
            Arrays.fill(array, size, sizeWithMargin, Integer.MIN_VALUE);
            values.setInts(row, array);
        } else if (array.length < sizeWithMargin) {
            int[] oldArray = array;
            array = Arrays.copyOf(array, sizeWithMargin);
            Arrays.fill(array, Math.max(oldArray.length, size), sizeWithMargin, Integer.MIN_VALUE);
            values.setInts(row, array);
        } else if (array.length > sizeWithMargin) {
            array = Arrays.copyOf(array, sizeWithMargin);
            Arrays.fill(array, size, sizeWithMargin, Integer.MIN_VALUE);
            values.setInts(row, array);
        }
    }

    @Override
    public int getExactSize(int row) {
        int[] array = values.getInts(row);
        if (array == null) return 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == Integer.MIN_VALUE) return i;
        }
        return array.length;
    }

    @Override
    public Collection<? extends Attribute> attributes() {
        return Collections.singletonList(values);
    }

    @Override
    public boolean isEmpty(int row) {
        return getId(row, 0) == Integer.MIN_VALUE;
    }
}

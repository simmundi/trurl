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

package pl.edu.icm.trurl.store.join;

import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.ByteAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;

import java.util.Arrays;
import java.util.Collection;


public final class RangedJoin implements Join {
    private final IntAttribute start;
    private final ByteAttribute length;
    private final Store target;
    private final int minimum;
    private final int margin;

    public RangedJoin(Store store, String name, int minimum, int margin) {
        this.minimum = minimum;
        this.margin = margin;
        String startName = name + "_start";
        String lengthName = name + "_length";
        store.addInt(startName);
        store.addByte(lengthName);
        this.start = store.get(startName);
        this.length = store.get(lengthName);
        store.markAttributeAsMeta(startName);
        store.markAttributeAsMeta(lengthName);
        this.target = store.addSubstore(name);
    }

    @Override
    public int getRow(int row, int index) {
        int start = this.start.getInt(row);
        int length = this.length.getByte(row);
        if (start == Integer.MIN_VALUE || length == Byte.MIN_VALUE || index >= length) {
            return Integer.MIN_VALUE;
        }
        return start + index;
    }

    @Override
    public void setSize(int row, int size) {
        int newLength = Math.max(size + margin, minimum);
        int startIndex = start.getInt(row);

        if (startIndex == Integer.MIN_VALUE && size == 0) {
            return;
        }

        if (startIndex == Integer.MIN_VALUE) {
            // allocate
            int freshStart = target.getCounter().next(newLength);
            start.setInt(row, freshStart);
            length.setByte(row, (byte) newLength);
        } else {
            int length = this.length.getByte(row);
            if (newLength > length) {
                // free the old range
                for (int i = startIndex; i < startIndex + length; i++) {
                    this.target.erase(i);
                }
                target.getCounter().free(startIndex, length);
                // allocate a new continuous range
                int freshStart = target.getCounter().next(newLength);
                start.setInt(row, freshStart);
                this.length.setByte(row, (byte) newLength);
            } else {
                // we are reusing the block, so we need to erase the unused part
                // (but we keep it allocated)
                for (int i = size + startIndex; i < startIndex + length; i++) {
                    this.target.erase(i);
                }
            }
        }
    }

    @Override
    public int getExactSize(int row) {
        int start = this.start.getInt(row);
        int length = this.length.getByte(row);
        if (start == Integer.MIN_VALUE || length == Byte.MIN_VALUE) {
            return 0;
        }
        for (int i = 0; i < length; i++) {
            if (target.isEmpty(start + i)) return i;
        }
        return length;
    }

    @Override
    public Collection<? extends Attribute> attributes() {
        return Arrays.asList(start, length);
    }

    @Override
    public Store getTarget() {
        return target;
    }

    @Override
    public boolean isEmpty(int row) {
        return getRow(row, 0) == Integer.MIN_VALUE;
    }
}

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
import pl.edu.icm.trurl.store.attribute.IntAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class SingleJoinWithReverseOnly implements Join {
    private final IntAttribute reverseRowAttribute;
    private final ConcurrentHashMap<Integer, Integer> reverseMap = new ConcurrentHashMap<>();
    private final Store target;

    public SingleJoinWithReverseOnly(Store store, String name) {
        this.target = store.addSubstore(name);
        this.target.addInt("reverse");
        this.target.markAttributeAsMeta("reverse");
        this.reverseRowAttribute = target.get("reverse");
    }

    @Override
    public int getRow(int row, int index) {
        if (index != 0) {
            return Integer.MIN_VALUE;
        }
        return reverseMap.computeIfAbsent(row, r -> {
            final int count = target.getCounter().getCount();
            for (int i = 0; i < count; i++) {
                if (reverseRowAttribute.getInt(i) == r) {
                    return i;
                }
            }
            return Integer.MIN_VALUE;
        });
    }

    @Override
    public void setSize(int row, int size) {
        int targetRow = reverseMap.getOrDefault(row, Integer.MIN_VALUE);
        if (size == 0) {
            if (targetRow != Integer.MIN_VALUE) {
                target.freeIndex(targetRow);
            } else {
                // do nothing
            }
        } else if (size == 1) {
            if (targetRow == Integer.MIN_VALUE) {
                // allocate a new row
                int newTargetRow = target.getCounter().next();
                reverseRowAttribute.setInt(newTargetRow, row);
            } else {
                // don't allocate, but erase the one that was allocated before.
                target.erase(targetRow);
            }
        } else {
            throw new IllegalStateException("SingleJoinWithReverseOnly cannot have its size set to something different than 0 or 1");
        }
    }

    @Override
    public int getExactSize(int row) {
        return isEmpty(row) ? 0 : 1;
    }


    @Override
    public Collection<? extends Attribute> attributes() {
        return Collections.emptyList();
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

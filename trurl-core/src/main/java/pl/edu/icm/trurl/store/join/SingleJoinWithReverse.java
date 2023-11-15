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

public class SingleJoinWithReverse implements Join {
    private IntAttribute rowAttribute;
    private IntAttribute reverseRowAttribute;
    private Store target;

    public SingleJoinWithReverse(Store store, String name) {
        store.addInt(name);
        store.hideAttribute(name);
        this.rowAttribute = store.get(name);
        this.target = store.addSubstore(name);
        this.target.addInt("reverse");
        this.target.hideAttribute("reverse");
        this.reverseRowAttribute = target.get("reverse");
    }

    @Override
    public int getRow(int row, int index) {
        return rowAttribute.getInt(row);
    }

    @Override
    public void setSize(int row, int size) {
        if (size == 0) {
            if (!rowAttribute.isEmpty(row)) {
                int targetRow = rowAttribute.getInt(row);
                rowAttribute.setEmpty(row);
                target.free(targetRow);
            } else {
                // do nothing
            }
        } else if (size == 1) {
            if (rowAttribute.isEmpty(row)) {
                // allocate a new row
                int targetRow = target.getCounter().next();
                rowAttribute.setInt(row, targetRow);
                reverseRowAttribute.setInt(targetRow, row);
            } else {
                // don't allocate, but erase the one that was allocated before.
                int targetRow = rowAttribute.getInt(row);
                target.erase(targetRow);
            }
        } else {
            throw new IllegalStateException("SingleJoin cannot have its size set to something different than 0 or 1");
        }
    }

    @Override
    public int getExactSize(int row) {
        return rowAttribute.isEmpty(row) ? 0 : 1;
    }


    @Override
    public Collection<? extends Attribute> attributes() {
        return Collections.singletonList(rowAttribute);
    }

    @Override
    public Store getTarget() {
        return target;
    }

    @Override
    public boolean isEmpty(int row) {
        return rowAttribute.isEmpty(row);
    }
}

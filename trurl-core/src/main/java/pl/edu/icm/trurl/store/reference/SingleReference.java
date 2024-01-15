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
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;

import java.util.Collection;
import java.util.Collections;

public class SingleReference implements Reference {
    private final IntAttribute idAttribute;

    public SingleReference(Store store, String name) {
        store.addInt(name);
        store.hideAttribute(name);
        this.idAttribute = store.get(name);
    }

    @Override
    public int getId(int row, int index) {
        return index == 0 ? idAttribute.getInt(row) : Integer.MIN_VALUE;
    }

    @Override
    public void setId(int row, int index, int id) {
        idAttribute.setInt(row, id);
    }

    @Override
    public void setSize(int row, int size) {
        if (size == 0 || size == 1) {
            idAttribute.setEmpty(row);
        } else {
            throw new IllegalStateException("SingleReference cannot have its size set to something different than 0 or 1");
        }
    }

    @Override
    public int getExactSize(int row) {
        return idAttribute.isEmpty(row) ? 0 : 1;
    }


    @Override
    public Collection<? extends Attribute> attributes() {
        return Collections.singletonList(idAttribute);
    }

    @Override
    public boolean isEmpty(int row) {
        return getId(row, 0) == Integer.MIN_VALUE;
    }
}

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

import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.Collection;

public interface Reference {
    /**
     * Returns the identifier of the nth entity pointed by the reference.
     * Index is 0-based. Negative numbers are not allowed and will return undefined results.
     * Numbers bigger than the current size of the reference will return Integer.MIN_VALUE; this
     * is also the guard value, informing about the end of the list.
     *
     * @param row
     * @param index
     * @return
     */
    int getId(int row, int index);

    /**
     * Store the identifier of the nth entity pointed by the reference.
     * The caller is responsible for sanity of the index (0-based, never negative, never exceeding the size allocated by setSize)
     * and for the correctness of the id (must be a valid id of an entity).
     * @param row
     * @param index
     * @return
     */
    void setId(int row, int index, int id);

    /**
     * Sets the number of identifiers in the given row.
     *
     * @param row
     * @param size
     * @return
     */
    void setSize(int row, int size);

    /**
     * Retrieves the exact number of elements for this row
     * @param row
     * @return
     */
    int getExactSize(int row);

    /**
     * Returns the attributes used by the reference in the source store
     * @return
     */
    Collection<? extends Attribute> attributes();
}

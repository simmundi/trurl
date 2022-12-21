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

package pl.edu.icm.trurl.store.attribute;

import pl.edu.icm.trurl.store.IntSink;

public interface ValueObjectListAttribute extends Attribute {
    int getSize(int row);

    void loadIds(int row, IntSink intSink);

    /**
     * @param size number of ids to save
     * @param firstNewIndex first index to use if size exceeds the biggest row size ever
     * @return returns firstNewIndex if firstNewIndex wasn't used or (the greatest used index + 1)
     */
    int saveIds(int row, int size, int firstNewIndex);
}

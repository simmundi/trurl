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

package pl.edu.icm.trurl.store;

import pl.edu.icm.trurl.ecs.Counter;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.join.Join;
import pl.edu.icm.trurl.store.reference.Reference;

import java.util.stream.Stream;

public interface StoreInspector {
    /**
     * Returns an attribute of the given name, if exists
     *
     * @param name
     * @param <T>
     * @return attribute or null, if none found
     */
    <T extends Attribute> T get(String name);

    /**
     * returns stream of all the attributes.
     *
     * @return attributes
     */
    Stream<Attribute> attributes();

    Stream<Attribute> visibleAttributes();

    Store getSubstore(String name);

    Counter getCounter();

    Join getJoin(String olives);

    Reference getReference(String olives);

    void erase(int i);

    boolean isEmpty(int i);
}

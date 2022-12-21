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

import pl.edu.icm.trurl.store.attribute.Attribute;

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
     * Part of the cooperative event system - calling this method notifies
     * all the storeListeners.
     *
     * @param fromInclusive
     * @param toExclusive
     * @param excludedListeners list of listeners to ignore while sending the event
     */
    void fireUnderlyingDataChanged(int fromInclusive, int toExclusive, StoreListener... excludedListeners);

    /**
     * returns stream of all the attributes.
     *
     * @return attributes
     */
    Stream<Attribute> attributes();

    /**
     * Returns the max value passed to fireUnderlyingDataChanged event
     * as toExclusive.
     */
    int getCount();

}

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

import net.snowyhollows.bento.soft.SoftEnum;
import net.snowyhollows.bento.soft.SoftEnumManager;
import pl.edu.icm.trurl.store.attribute.AttributeFactory;

/**
 * This is usually used as superinterface of Store.
 * <p>
 * It is used by store clients to configure the store metadata, which is required for importing and exporting data.
 */
public interface StoreConfigurer {
    void addBoolean(String name);

    void addByte(String name);

    void addDouble(String name);

    <E extends Enum<E>> void addStaticCategory(String name, Class<E> enumType);

    <E extends SoftEnum> void addDynamicCategory(String name, SoftEnumManager<E> enumType);

    void addFloat(String name);

    void addInt(String name);

    void addShort(String name);

    void addString(String name);
    Store addReference(String name);

    Store addReferenceList(String referenceName);
    void addRootReference(String name);

    void addRootReferenceList(String name);
}

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

package pl.edu.icm.trurl.store.attribute;

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import net.snowyhollows.bento.soft.SoftEnum;
import net.snowyhollows.bento.soft.SoftEnumManager;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;

@ImplementationSwitch(configKey = "trurl.engine.attributeFactory", cases = {
        @ImplementationSwitch.When(name = "array", implementation = ArrayAttributeFactory.class, useByDefault = true)
})
public interface AttributeFactory {
    BooleanAttribute createBoolean(String name);

    ByteAttribute createByte(String name);

    DoubleAttribute createDouble(String name);

    <E extends Enum<E>> CategoricalStaticAttribute<E> createStaticCategory(String name, Class<E> enumType);

    <E extends SoftEnum> CategoricalDynamicAttribute createDynamicCategory(String name, SoftEnumManager<E> enumType);

    FloatAttribute createFloat(String name);

    IntAttribute createInt(String name);

    ShortAttribute createShort(String name);

    StringAttribute createString(String name);

    IntListAttribute createIntList(String name);
}


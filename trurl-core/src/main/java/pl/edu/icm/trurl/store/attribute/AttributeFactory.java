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
import net.snowyhollows.bento.category.Category;
import net.snowyhollows.bento.category.CategoryManager;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;

@ImplementationSwitch(configKey = "trurl.engine.attributeFactory", cases = {
        @ImplementationSwitch.When(name = "basic", implementation = BasicAttributeFactory.class, useByDefault = true)
})
public interface AttributeFactory {
    BooleanAttribute createBoolean(String name, int capacity);

    ByteAttribute createByte(String name, int capacity);

    DoubleAttribute createDouble(String name, int capacity);

    <E extends Enum<E>> EnumAttribute<E> createEnum(String name, Class<E> enumType, int capacity);

    <E extends Category> CategoryAttribute createCategory(String name, CategoryManager<E> enumType, int capacity);

    FloatAttribute createFloat(String name, int capacity);

    IntAttribute createInt(String name, int capacity);

    ObjectAttribute createObject(String name, int capacity);

    ShortAttribute createShort(String name, int capacity);

    StringAttribute createString(String name, int capacity);

    IntListAttribute createIntList(String name, int capacity);
}


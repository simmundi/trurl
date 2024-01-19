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

package pl.edu.icm.trurl.store.basic;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.Category;
import net.snowyhollows.bento.category.CategoryManager;
import pl.edu.icm.trurl.store.attribute.*;

public final class BasicAttributeFactory implements AttributeFactory {

    @WithFactory
    public BasicAttributeFactory() {
    }

    @Override
    public BooleanAttribute createBoolean(String name, int defaultCapacity) {
        return new BasicBooleanAttribute(name, defaultCapacity);
    }

    @Override
    public ByteAttribute createByte(String name, int defaultCapacity) {
        return new BasicByteAttribute(name, defaultCapacity);
    }

    @Override
    public BasicDoubleAttribute createDouble(String name, int defaultCapacity) {
        return new BasicDoubleAttribute(name, defaultCapacity);
    }

    @Override
    public <E extends Enum<E>> EnumAttribute<E> createEnum(String name, Class<E> enumType, int defaultCapacity) {
        return new BasicEnumAttribute<>(enumType, name, defaultCapacity);
    }

    @Override
    public <E extends Category> pl.edu.icm.trurl.store.attribute.CategoryAttribute<E> createCategory(String name, CategoryManager<E> enumType, int defaultCapacity) {
        return new BasicCategoryAttribute<>(enumType, name, defaultCapacity);
    }

    @Override
    public FloatAttribute createFloat(String name, int defaultCapacity) {
        return new BasicFloatAttribute(name, defaultCapacity);
    }

    @Override
    public IntAttribute createInt(String name, int defaultCapacity) {
        return new BasicIntAttribute(name, defaultCapacity);
    }

    @Override
    public ShortAttribute createShort(String name, int defaultCapacity) {
        return new BasicShortAttribute(name, defaultCapacity);
    }

    @Override
    public StringAttribute createString(String name, int defaultCapacity) {
        return new BasicStringAttribute(name, defaultCapacity);
    }

    @Override
    public IntListAttribute createIntList(String name, int defaultCapacity) { return new BasicIntListAttribute(name, defaultCapacity); }
}

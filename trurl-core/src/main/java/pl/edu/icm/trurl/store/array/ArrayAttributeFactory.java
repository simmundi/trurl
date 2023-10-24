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

package pl.edu.icm.trurl.store.array;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.soft.SoftEnum;
import net.snowyhollows.bento.soft.SoftEnumManager;
import pl.edu.icm.trurl.store.attribute.*;

public final class ArrayAttributeFactory implements AttributeFactory {

    @WithFactory
    public ArrayAttributeFactory() {
    }

    @Override
    public BooleanAttribute createBoolean(String name, int defaultCapacity) {
        return new BooleanArrayAttribute(name, defaultCapacity);
    }

    @Override
    public ByteAttribute createByte(String name, int defaultCapacity) {
        return new ByteArrayAttribute(name, defaultCapacity);
    }

    @Override
    public DoubleArrayAttribute createDouble(String name, int defaultCapacity) {
        return new DoubleArrayAttribute(name, defaultCapacity);
    }

    @Override
    public <E extends Enum<E>> CategoricalStaticAttribute<E> createStaticCategory(String name, Class<E> enumType, int defaultCapacity) {
        return new CategoricalStaticArrayAttribute<>(enumType, name, defaultCapacity);
    }

    @Override
    public <E extends SoftEnum> CategoricalDynamicAttribute<E> createDynamicCategory(String name, SoftEnumManager<E> enumType, int defaultCapacity) {
        return new CategoricalDynamicArrayAttribute<>(enumType, name, defaultCapacity);
    }

    @Override
    public FloatAttribute createFloat(String name, int defaultCapacity) {
        return new FloatArrayAttribute(name, defaultCapacity);
    }

    @Override
    public IntAttribute createInt(String name, int defaultCapacity) {
        return new IntArrayAttribute(name, defaultCapacity);
    }

    @Override
    public ShortAttribute createShort(String name, int defaultCapacity) {
        return new ShortArrayAttribute(name, defaultCapacity);
    }

    @Override
    public StringAttribute createString(String name, int defaultCapacity) {
        return new StringArrayAttribute(name, defaultCapacity);
    }

    @Override
    public IntListAttribute createIntList(String name, int defaultCapacity) { return new IntListArrayAttribute(name, defaultCapacity); }
}

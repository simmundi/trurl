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

package pl.edu.icm.trurl.store.array;

import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreListener;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.generic.GenericEntityListOverIntArrayAttribute;
import pl.edu.icm.trurl.store.attribute.generic.GenericEntityOverIntAttribute;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class ArrayStore implements Store {
    public final static int DEFAULT_INITIAL_CAPACITY = 1000;
    private final CopyOnWriteArrayList<StoreListener> listeners = new CopyOnWriteArrayList();
    private final Map<String, Attribute> attributes = new LinkedHashMap<>(40);
    private final int defaultCapacity;
    private final AtomicInteger count = new AtomicInteger();

    public ArrayStore(int defaultCapacity) {
        this.defaultCapacity = defaultCapacity;
    }

    public ArrayStore() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    @Override
    public <T extends Attribute> T get(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public void addStoreListener(StoreListener storeListener) {
        listeners.add(storeListener);
    }

    @Override
    public void fireUnderlyingDataChanged(int fromInclusive, int toExclusive, StoreListener... excluded) {
        count.set(toExclusive);
        List<StoreListener> excludedList = Arrays.asList(excluded);
        for (StoreListener storeListener : listeners) {
            if (!excludedList.contains(storeListener)) {
                storeListener.onUnderlyingDataChanged(fromInclusive, toExclusive);
            }
        }
    }

    @Override
    public Stream<Attribute> attributes() {
        return attributes.values().stream();
    }

    @Override
    public int getCount() {
        return count.get();
    }

    @Override
    public void addBoolean(String name) {
        attributes.putIfAbsent(name, new BooleanArrayAttribute(name, defaultCapacity));

    }

    @Override
    public void addByte(String name) {
        attributes.putIfAbsent(name, new ByteArrayAttribute(name, defaultCapacity));
    }

    @Override
    public void addDouble(String name) {
        attributes.putIfAbsent(name, new DoubleArrayAttribute(name, defaultCapacity));
    }

    @Override
    public void addEntity(String name) {
        attributes.putIfAbsent(name, new GenericEntityOverIntAttribute(new IntArrayAttribute(name, defaultCapacity)));
    }

    @Override
    public void addEntityList(String name) {
        attributes.putIfAbsent(name, new GenericEntityListOverIntArrayAttribute(new IntListArrayAttribute(name, defaultCapacity)));
    }

    @Override
    public void addValueObjectList(String name) {
        attributes.putIfAbsent(name, new ValueObjectListArrayAttribute(name, defaultCapacity));
    }

    @Override
    public <E extends Enum<E>> void addEnum(String name, Class<E> enumType) {
        attributes.putIfAbsent(name, new EnumArrayAttribute<>(enumType, name, defaultCapacity));
    }

    @Override
    public void addFloat(String name) {
        attributes.putIfAbsent(name, new FloatArrayAttribute(name, defaultCapacity));
    }

    @Override
    public void addInt(String name) {
        attributes.putIfAbsent(name, new IntArrayAttribute(name, defaultCapacity));
    }

    @Override
    public void addShort(String name) {
        attributes.putIfAbsent(name, new ShortArrayAttribute(name, defaultCapacity));
    }

    @Override
    public void addString(String name) {
        attributes.putIfAbsent(name, new StringArrayAttribute(name, defaultCapacity));
    }
}

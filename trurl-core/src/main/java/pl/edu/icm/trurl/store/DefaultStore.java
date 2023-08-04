/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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
import pl.edu.icm.trurl.store.array.*;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.AttributeFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class DefaultStore implements Store {
    private final CopyOnWriteArrayList<StoreListener> listeners = new CopyOnWriteArrayList();
    private final Map<String, Attribute> attributes = new LinkedHashMap<>(40);
    private final AtomicInteger count = new AtomicInteger();
    private final AttributeFactory attributeFactory;

    public DefaultStore(AttributeFactory attributeFactory) {
        this.attributeFactory = attributeFactory;
    }


    @Override
    public <T extends Attribute> T get(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public void createStoreListener(StoreListener storeListener) {
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
    public void createBoolean(String name) {
        attributes.putIfAbsent(name, new BooleanArrayAttribute(name, defaultCapacity));

    }

    @Override
    public void createByte(String name) {
        attributes.putIfAbsent(name, new ByteArrayAttribute(name, defaultCapacity));
    }

    @Override
    public void createDouble(String name) {
        attributes.putIfAbsent(name, new DoubleArrayAttribute(name, defaultCapacity));
    }

    @Override
    public void createEntity(String name) {
        attributes.putIfAbsent(name, new GenericEntityOverIntAttribute(new IntArrayAttribute(name, defaultCapacity)));
    }

    @Override
    public void createEntityList(String name) {
        attributes.putIfAbsent(name, new GenericEntityListOverIntArrayAttribute(new IntListArrayAttribute(name, defaultCapacity)));
    }

    @Override
    public void createValueObjectList(String name) {
        attributes.putIfAbsent(name, new ValueObjectListArrayAttribute(name, defaultCapacity));
    }

    @Override
    public <E extends Enum<E>> void addStaticCategory(String name, Class<E> enumType) {
        attributes.putIfAbsent(name, new CategoricalStaticArrayAttribute<>(enumType, name, defaultCapacity));
    }

    @Override
    public <E extends SoftEnum> void addDynamicCategory(String name, SoftEnumManager<E> enumType) {

    }

    @Override
    public <E extends SoftEnum> void addStaticCategory(String name, SoftEnumManager<E> enumType) {
        attributes.putIfAbsent(name, new CategoricalDynamicArrayAttribute<>(enumType, name, defaultCapacity));
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

    @Override
    public Store addReference(String name) {
        return null;
    }

    @Override
    public Store addReferenceList(String referenceName) {
        return null;
    }

    @Override
    public void addRootReference(String name) {

    }

    @Override
    public void addRootReferenceList(String name) {

    }
}

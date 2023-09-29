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
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.AttributeFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a columnar store.
 * <p>
 * The store gives access to specific attributes (instances of Attribute, which can be
 * downcast to interfaces like ShortAttribute, StringAttribute etc.) and acts as a hub
 * for adding listeners and firing general events.
 * <p>
 * The events are not fired automatically by any part of the store; they are meant
 * for situations where a couple of consumers of the same store wish to let
 * one another know about changes to the data - like when a loader loads data
 * into the store.
 * <p>
 * Within Trurl, the stores are meant to store entities / components.
 * <p>
 * The flow is:
 *
 * <ul>
 *     <li>
 *         code creates a store instance,
 *     </li>
 *     <li>
 *         the instance is passed to Mappers, which use the StoreMetadata
 *     interface to configure types and names of the store's columns
 *     </li>
 *     <li>
 *         the mappers are then attached to the store (i.e. get all the
 *     necessary attributes) and become its listeners
 *     </li>
 *     <li>
 *         the store configuration is used to load raw data from a file
 *     </li>
 *     <li>
 *         the loader fires underlying data changed event; the mappers
 *     handle it and use it to find out about the length of the data and
 *     to fire any required events to their relevant listeners.
 *     </li>
 * </ul>
 */
public final class Store implements StoreConfigurer, StoreInspector, StoreObservable {
    private final String namespace;
    private final Store rootStore;
    private final Map<String, Store> substores = new LinkedHashMap<>();
    private final AttributeFactory attributeFactory;
    private final CopyOnWriteArrayList<StoreListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, Attribute> attributes = new LinkedHashMap<>(40);
    private final int defaultCapacity;
    private final AtomicInteger count = new AtomicInteger();

    public Store(AttributeFactory attributeFactory, int defaultCapacity) {
        this.attributeFactory = attributeFactory;
        this.namespace = "";
        this.defaultCapacity = defaultCapacity;
        this.rootStore = this;
    }

    private Store(Store rootStore, AttributeFactory attributeFactory, String namespace, int defaultCapacity) {
        this.attributeFactory = attributeFactory;
        this.namespace = namespace;
        this.defaultCapacity = defaultCapacity;
        this.rootStore = rootStore;
    }

    public Store flatten() {
        Store flatStore = new Store(attributeFactory, defaultCapacity);
        flatStore.count.set(count.get());
        attributes().forEach(attribute ->
                flatStore.attributes.put(attribute.name(), attribute));
        substores.values().forEach(store -> {
            Store newStore = store.flatten();
            flatStore.count.set(Math.max(flatStore.getCount(), newStore.getCount()));
            flatStore.attributes.putAll(newStore.attributes);
        });
        return flatStore;
    }

    public String getNamespace() {
        return namespace;
    }

    public Store getRootStore() {
        return rootStore;
    }

    public Collection<Store> getSubstores() {
        return substores.values();
    }

    public Collection<Store> allDescendants() {
        return recursivelyAllDescendants().collect(Collectors.toSet());
    }

    public Store getSubstore(String name) {
        return substores.get(name);
    }

    public void createSubstore(String namespace) {
        String substoreNamespace = this.namespace.isEmpty() ? namespace : this.namespace + "." + namespace;
        substores.put(namespace, new Store(rootStore, attributeFactory, substoreNamespace, defaultCapacity));
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
        attributes.putIfAbsent(name, attributeFactory.createBoolean(name));

    }

    @Override
    public void addByte(String name) {
        attributes.putIfAbsent(name, attributeFactory.createByte(name));
    }

    @Override
    public void addDouble(String name) {
        attributes.putIfAbsent(name, attributeFactory.createDouble(name));
    }

    @Override
    public void addEntity(String name) {
        throw new UnsupportedOperationException("Adding entity is not supported.");
    }

    @Override
    public void addEntityList(String name) {
        throw new UnsupportedOperationException("Adding entity list is not supported.");
    }

    @Override
    public void addValueObjectList(String name) {
        throw new UnsupportedOperationException("Adding value object list is not supported.");
    }

    @Override
    public <E extends Enum<E>> void addEnum(String name, Class<E> enumType) {
        attributes.putIfAbsent(name, attributeFactory.createStaticCategory(name, enumType));
    }

    @Override
    public <E extends SoftEnum> void addSoftEnum(String name, SoftEnumManager<E> enumType) {
        attributes.putIfAbsent(name, attributeFactory.createDynamicCategory(name, enumType));
    }

    @Override
    public void addFloat(String name) {
        attributes.putIfAbsent(name, attributeFactory.createFloat(name));
    }

    @Override
    public void addInt(String name) {
        attributes.putIfAbsent(name, attributeFactory.createInt(name));
    }

    @Override
    public void addShort(String name) {
        attributes.putIfAbsent(name, attributeFactory.createShort(name));
    }

    @Override
    public void addString(String name) {
        attributes.putIfAbsent(name, attributeFactory.createString(name));
    }

    private Stream<Store> recursivelyAllDescendants() {
        return substores.values().stream().flatMap(
                store -> Stream.concat(Stream.of(store), store.recursivelyAllDescendants())
        );
    }
}

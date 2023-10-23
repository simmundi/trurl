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
import pl.edu.icm.trurl.ecs.Counter;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.AttributeFactory;
import pl.edu.icm.trurl.store.join.ArrayJoin;
import pl.edu.icm.trurl.store.join.Join;
import pl.edu.icm.trurl.store.join.RandgedJoin;
import pl.edu.icm.trurl.store.reference.ArrayReference;
import pl.edu.icm.trurl.store.reference.Reference;
import pl.edu.icm.trurl.store.reference.SingleReference;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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
 *     interface to configure types and names of the store's columns, joins
 *     and references
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
public final class Store implements StoreConfigurer, StoreInspector {
    private final Map<String, Attribute> allAttributes = new LinkedHashMap<>(40);
    private final CopyOnWriteArrayList<Attribute> visibleAttributes = new CopyOnWriteArrayList<>();
    private final Map<String, Store> substores = new LinkedHashMap<>();
    private final Map<String, Join> joins = new LinkedHashMap<>();
    private final Map<String, Reference> references = new LinkedHashMap<>();

    private final int defaultCapacity;
    private final Counter counter;
    private final String name;
    private final AttributeFactory attributeFactory;

    public Store(AttributeFactory attributeFactory, int defaultCapacity) {
        counter = new Counter(defaultCapacity);
        this.attributeFactory = attributeFactory;
        this.name = "";
        this.defaultCapacity = defaultCapacity;
    }

    private Store(AttributeFactory attributeFactory, String name, int defaultCapacity) {
        counter = new Counter(defaultCapacity);
        this.attributeFactory = attributeFactory;
        this.name = name;
        this.defaultCapacity = defaultCapacity;
    }

    public Collection<Store> allDescendants() {
        return recursivelyAllDescendants().collect(Collectors.toSet());
    }

    @Override
    public void erase(int row) {
        for (Attribute attribute : visibleAttributes) {
            attribute.setEmpty(row);
        }
        for (Join join : joins.values()) {
            join.setSize(row, 0);
        }
        for (Reference reference : references.values()) {
            reference.setSize(row, 0);
        }
        counter.free(row);
    }

    public void ensureCapacity(int capacity) {
        allAttributes.values().forEach(a -> a.ensureCapacity(capacity));
    }

    @Override
    public boolean isEmpty(int row) {
        for (Attribute attribute : allAttributes.values()) {
            if (!attribute.isEmpty(row)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Stream<Attribute> attributes() {
        return allAttributes.values().stream();
    }

    @Override
    public Stream<Attribute> visibleAttributes() {
        return visibleAttributes.stream();
    }

    @Override
    public Store getSubstore(String name) {
        return substores.get(name);
    }

    @Override
    public Counter getCounter() {
        return counter;
    }

    @Override
    public Reference getReference(String name) {
        return references.get(name);
    }

    public Join getJoin(String name) {
        return joins.get(name);
    }

    @Override
    public void addBoolean(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createBoolean(name));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addByte(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createByte(name));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addDouble(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createDouble(name));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }


    @Override
    public void addIntList(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createIntList(name));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public <E extends Enum<E>> void addEnum(String name, Class<E> enumType) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createStaticCategory(name, enumType));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public <E extends SoftEnum> void addSoftEnum(String name, SoftEnumManager<E> enumType) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createDynamicCategory(name, enumType));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addFloat(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createFloat(name));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addInt(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createFloat(name));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addShort(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createShort(name));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addString(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createString(name));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    public ReferenceConfigurer addReference(String name) {
        return new ReferenceConfigurer() {
            @Override
            public void arrayTyped(int minimumSize, int margin) {
                ArrayReference reference = new ArrayReference(Store.this, name, minimumSize, margin);
                references.put(name, reference);
            }

            @Override
            public void single() {
                SingleReference reference = new SingleReference(Store.this, name);
                references.put(name, reference);
            }
        };
    }

    @Override
    public JoinConfigurer addJoin(String name) {
        return new JoinConfigurer() {

            public Store rangeTyped(int minimum, int margin) {
                RandgedJoin join = new RandgedJoin(Store.this, name, minimum, margin);
                joins.put(name, join);
                return getSubstore(name);
            }

            public Store arrayTyped(int minimum, int margin) {
                ArrayJoin join = new ArrayJoin(Store.this, name, minimum, margin);
                joins.put(name, join);
                return getSubstore(name);
            }
        };
    }

    @Override
    public void hideAttribute(String name) {
        visibleAttributes.remove(allAttributes.get(name));
    }

    public Store flatten() {
        throw new UnsupportedOperationException("sadly, not at the moment, too much has changed");
    }

    public String getName() {
        return name;
    }

    public Stream<Store> getSubstores() {
        return substores.values().stream();
    }

    public Store addSubstore(String namespace) {
        String substoreNamespace = this.name.isEmpty() ? namespace : this.name + "." + namespace;
        substores.put(namespace, new Store(attributeFactory, substoreNamespace, defaultCapacity));
        return substores.get(name);
    }

    @Override
    public <T extends Attribute> T get(String name) {
        return (T) allAttributes.get(name);
    }

    private Stream<Store> recursivelyAllDescendants() {
        return substores.values().stream().flatMap(
                store -> Stream.concat(Stream.of(store), store.recursivelyAllDescendants())
        );
    }
}

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
import pl.edu.icm.trurl.store.join.ArrayJoin;
import pl.edu.icm.trurl.store.join.Join;
import pl.edu.icm.trurl.store.join.RangedJoin;
import pl.edu.icm.trurl.store.join.SingleJoin;
import pl.edu.icm.trurl.store.reference.ArrayReference;
import pl.edu.icm.trurl.store.reference.Reference;
import pl.edu.icm.trurl.store.reference.SingleReference;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a columnar store.
 * <p>
 * The store gives access to specific attributes (instances of Attribute, which can be
 * downcast to interfaces like ShortAttribute, StringAttribute etc.). An attribute
 * can be imagined as a function from row number to a value of a specific type.
 * <p>
 * The store can be joined to its substores (which are also stores), which means that
 * each row of this store can have connections to 0:N rows of a specific substore.
 * Such relation is reified as a Join. Joins can be thought of as named functions
 * from row number to a list of row numbers of a specific target store.
 * <p>
 * Each store can also contain references to rows in the top-level
 * store (in case of the top-level store, these are self-references). The references (reified
 * as Reference objects) can be imagined as named functions from row number to a list of
 * row numbers of the top-level store.
 * <p>
 * Reference and Join objects only contain meta-data, the actual data is stored in attributes
 * (accessible, but hidden by default).
 */
public final class Store implements StoreConfigurer, StoreInspector {
    private final Map<String, Attribute> allAttributes = new LinkedHashMap<>(40);
    private final CopyOnWriteArrayList<Attribute> visibleAttributes = new CopyOnWriteArrayList<>();
    private final Map<String, Store> substores = new LinkedHashMap<>();
    private final Map<String, Join> joins = new LinkedHashMap<>();
    private final Map<String, Reference> references = new LinkedHashMap<>();

    private int ensuredCapacity;
    private final Counter counter;
    private final String name;
    private final AttributeFactory attributeFactory;

    public Store(AttributeFactory attributeFactory, int ensuredCapacity) {
        counter = new Counter(ensuredCapacity);
        this.attributeFactory = attributeFactory;
        this.name = "";
        this.ensuredCapacity = ensuredCapacity;
    }

    private Store(AttributeFactory attributeFactory, String name, int ensuredCapacity) {
        counter = new Counter(ensuredCapacity);
        this.attributeFactory = attributeFactory;
        this.name = name;
        this.ensuredCapacity = ensuredCapacity;
    }

    public Collection<Store> allDescendants() {
        return recursivelyAllDescendants().collect(Collectors.toSet());
    }

    /**
     * Erases all the data concerning a row, including child rows connected via joins
     */
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
    }

    /**
     * Like erase, but also returns the row to the pool of free rows (so that it can be reused).
     * Whether child rows are also freed depends on the implementation of the specific joins
     * and references.
     */
    @Override
    public void free(int row) {
        erase(row);
        counter.free(row);
    }

    public void ensureCapacity(int capacity) {
        this.ensuredCapacity = capacity;
        allAttributes.values().forEach(a -> a.ensureCapacity(capacity));
    }


    @Override
    public boolean isEmpty(int row) {
        for (Attribute attribute : visibleAttributes) {
            if (!attribute.isEmpty(row)) {
                return false;
            }
        }
        for (Join join : joins.values()) {
            if (join.getExactSize(row) != 0) {
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
    public<T extends Reference> T getReference(String name) {
        return (T)references.get(name);
    }

    public<T extends Join> T getJoin(String name) {
        return (T) joins.get(name);
    }

    @Override
    public void addBoolean(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createBoolean(name, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addByte(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createByte(name, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addDouble(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createDouble(name, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }


    @Override
    public void addIntList(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createIntList(name, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public <E extends Enum<E>> void addEnum(String name, Class<E> enumType) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createStaticCategory(name, enumType, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public <E extends SoftEnum> void addSoftEnum(String name, SoftEnumManager<E> enumType) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createDynamicCategory(name, enumType, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addFloat(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createFloat(name, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addInt(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createInt(name, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addShort(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createShort(name, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
    public void addString(String name) {
        Attribute former = allAttributes.putIfAbsent(name, attributeFactory.createString(name, ensuredCapacity));
        if (former == null) {
            visibleAttributes.add(allAttributes.get(name));
        }
    }

    @Override
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

            @Override
            public Store rangeTyped(int minimum, int margin) {
                RangedJoin join = new RangedJoin(Store.this, name, minimum, margin);
                joins.put(name, join);
                return getSubstore(name);
            }

            @Override
            public Store arrayTyped(int minimum, int margin) {
                ArrayJoin join = new ArrayJoin(Store.this, name, minimum, margin);
                joins.put(name, join);
                return getSubstore(name);
            }

            @Override
            public Store singleTyped() {
                SingleJoin join = new SingleJoin(Store.this, name);
                joins.put(name, join);
                return getSubstore(name);
            }
        };
    }

    @Override
    public void hideAttribute(String name) {
        visibleAttributes.remove(allAttributes.get(name));
    }

    public String getName() {
        return name;
    }

    public Stream<Store> getSubstores() {
        return substores.values().stream();
    }

    public Store addSubstore(String namespace) {
        String substoreNamespace = this.name.isEmpty() ? namespace : this.name + "." + namespace;
        substores.put(namespace, new Store(attributeFactory, substoreNamespace, ensuredCapacity));
        return substores.get(substoreNamespace);
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

    public int getEnsuredCapacity() {
        return ensuredCapacity;
    }
}

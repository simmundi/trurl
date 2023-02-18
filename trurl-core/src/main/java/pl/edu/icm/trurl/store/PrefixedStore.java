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
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.stream.Stream;

public class PrefixedStore implements Store {
    private final Store wrapped;
    private final StoreConfigurer wrappedMeta;
    private final String prefix;

    public static Store wrap(Store wrappedStore, String prefix) {
        return new PrefixedStore(wrappedStore, prefix);
    }

    public static Store wrap(StoreConfigurer wrappedMeta, String prefix) {
        return new PrefixedStore(wrappedMeta, prefix);
    }

    private PrefixedStore(Store wrappedStore, String prefix) {
        this.wrapped = wrappedStore;
        this.wrappedMeta = wrappedStore;
        this.prefix = prefix;
    }

    private PrefixedStore(StoreConfigurer wrappedMeta, String prefix) {
        this.wrappedMeta = wrappedMeta;
        this.wrapped = null;
        this.prefix = prefix;
    }

    @Override
    public <T extends Attribute> T get(String name) {
        return wrapped.get(wrap(name));
    }

    @Override
    public void addStoreListener(StoreListener storeListener) {
        wrapped.addStoreListener(storeListener);
    }

    @Override
    public void fireUnderlyingDataChanged(int fromInclusive, int toExclusive, StoreListener... excluded) {
        wrapped.fireUnderlyingDataChanged(fromInclusive, toExclusive, excluded);
    }

    @Override
    public Stream<Attribute> attributes() {
        return wrapped.attributes();
    }

    @Override
    public int getCount() {
        return wrapped.getCount();
    }

    @Override
    public void addBoolean(String name) {
        wrappedMeta.addBoolean(wrap(name));
    }

    @Override
    public void addByte(String name) {
        wrappedMeta.addByte(wrap(name));
    }

    @Override
    public void addDouble(String name) {
        wrappedMeta.addDouble(wrap(name));
    }

    @Override
    public void addEntity(String name) {
        wrappedMeta.addEntity(wrap(name));
    }

    @Override
    public void addEntityList(String name) {
        wrappedMeta.addEntityList(wrap(name));
    }

    @Override
    public void addValueObjectList(String name) {
        wrappedMeta.addValueObjectList(wrap(name));
    }

    @Override
    public <E extends Enum<E>> void addEnum(String name, Class<E> enumType) {
        wrappedMeta.addEnum(wrap(name), enumType);
    }

    @Override
    public <E extends SoftEnum> void addSoftEnum(String name, SoftEnumManager<E> enumType) {
        wrappedMeta.addSoftEnum(wrap(name), enumType);
    }

    @Override
    public void addFloat(String name) {
        wrappedMeta.addFloat(wrap(name));
    }

    @Override
    public void addInt(String name) {
        wrappedMeta.addInt(wrap(name));
    }

    @Override
    public void addShort(String name) {
        wrappedMeta.addShort(wrap(name));
    }

    @Override
    public void addString(String name) {
        wrappedMeta.addString(wrap(name));
    }

    private String wrap(String name) {
        return prefix + "." + name;
    }
}

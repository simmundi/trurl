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

package pl.edu.icm.trurl.ecs.mapper;

import pl.edu.icm.trurl.ecs.AbstractSession;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreConfigurer;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public interface Mapper<T> {
    void attachStore(Store store);
    void configureStore(StoreConfigurer metadata);
    T create();
    boolean isPresent(int row);
    void erase(int index);
    void load(AbstractSession session, T component, int index);
    default void save(T component, int row) {
        save(null, component, row);
    }
    void save(AbstractSession owner, T component, int row);
    List<Attribute> attributes();
    default String name() {
        return this.create().getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }
    default T createAndLoad(int row) {
        return createAndLoad(null, row);
    }
    default T createAndLoad(AbstractSession session, int row) {
        T created = create();
        load(session, created, row);
        return created;
    }
    void lifecycleEvent(LifecycleEvent lifecycleEvent);
    default List<Mapper> getChildMappers() {
        return Collections.emptyList();
    }
    default void configureAndAttach(Store store) {
        configureStore(store);
        attachStore(store);
    }
}

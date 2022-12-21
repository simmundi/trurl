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

import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StoreSubset implements StoreInspector {
    private final Map<String, Attribute> attributes = new LinkedHashMap<>();
    private int count;
    private final StoreInspector originalStore;

    StoreSubset(StoreInspector store, Predicate<Attribute> predicate, int count) {
        this.originalStore = store;
        store
                .attributes()
                .filter(predicate)
                .forEach(attribute -> attributes.put(attribute.name(), attribute));
        this.count = count;
    }

    @Override
    public <T extends Attribute> T get(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public void fireUnderlyingDataChanged(int fromInclusive, int toExclusive, StoreListener... excludedListeners) {
        this.originalStore.fireUnderlyingDataChanged(fromInclusive, toExclusive, excludedListeners);
    }

    @Override
    public Stream<Attribute> attributes() {
        return attributes.values().stream();
    }

    @Override
    public int getCount() {
        return count;
    }
}

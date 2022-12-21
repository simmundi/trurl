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

import com.google.common.base.Preconditions;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility for splitting store horizontally (i.e. separating attributes into different stores).
 *
 * Stores split out by this utility are views of the original store, which remains unchanged.
 *
 * StoreSplitter is mutable, i.e. each split operation results in the reminder store containing
 * less attributes.
 */
public class StoreSplitter {
    private StoreInspector store;
    private final MapperSet mapperSet;
    private final List<String> columnNames = new ArrayList<>();

    private StoreSplitter(StoreInspector store, MapperSet mapperSet) {
        this.mapperSet = mapperSet;
        this.store = store;
        this.store
                .attributes()
                .forEach(attribute -> columnNames.add(attribute.name()));
    }

    public static StoreSplitter from(StoreInspector store) {
        return new StoreSplitter(store, null);
    }

    public static StoreSplitter from(Engine engine) {
        return new StoreSplitter(engine.getStore(), engine.getMapperSet());
    }

    public StoreInspector splitComponents(Collection<Class<?>> componentClasses) {
        Preconditions.checkState(mapperSet != null, "For splitting out components - a mapperSet must be provided at construction time");
        return splitMappers(componentClasses.stream().map(cc -> mapperSet.classToMapper(cc)).collect(Collectors.toList()));
    }

    public StoreInspector getReminder() {
        return store;
    }

    public StoreInspector splitMappers(Collection<Mapper<?>> mappers) {
        HashSet<Attribute> targetAttributes = new HashSet<>(Mappers.gatherAttributes(mappers));
        int newCount = mappers.stream()
                .mapToInt(Mapper::getCount)
                .max().orElseThrow(() -> new IllegalArgumentException("No mappers found"));
        return split(attr -> targetAttributes.contains(attr), newCount);
    }

    private StoreInspector split(Predicate<Attribute> toSplit, int newCount) {
        Predicate<Attribute> toRemain = a -> !toSplit.test(a);
        StoreSubset reminder = new StoreSubset(store, toRemain, store.getCount());
        StoreSubset result = new StoreSubset(store, toSplit, newCount);
        this.store = reminder;
        return result;
    }

}

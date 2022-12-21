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

package pl.edu.icm.trurl.generator;

import com.squareup.javapoet.TypeName;
import pl.edu.icm.trurl.ecs.annotation.CollectionType;
import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import java.util.Optional;
import java.util.stream.Stream;

public class SyntheticPropertiesSynthesizer {

    public Stream<ComponentProperty> synthesize(ComponentProperty base) {
        switch (base.type) {
            case EMBEDDED_LIST:
                CollectionType collectionType = Optional.ofNullable(base.attribute.getAnnotation(MappedCollection.class))
                        .map(MappedCollection::collectionType).orElse(CollectionType.RANGE);
                switch (collectionType) {
                    case ARRAY_LIST:
                        return valueObjectList(base);
                    case RANGE:
                        return embeddedList(base);
                    default:
                        throw new IllegalStateException("Unexpected value: " + collectionType);
                }
            default:
                return Stream.of(base);
        }
    }

    private Stream<ComponentProperty> embeddedList(ComponentProperty base) {
        return Stream.of(
                base,
                new ComponentProperty(
                        base.name + "_start",
                        base.namespace,
                        PropertyType.INT_PROP,
                        null,
                        null, TypeName.INT, true, Optional.empty()),
                new ComponentProperty(
                        base.name + "_length",
                        base.namespace,
                        PropertyType.BYTE_PROP,
                        null,
                        null, TypeName.INT, true, Optional.empty()));
    }

    private Stream<ComponentProperty> valueObjectList(ComponentProperty base) {
        return Stream.of(
                base,
                new ComponentProperty(
                        base.name + "_ids",
                        base.namespace,
                        PropertyType.VALUE_OBJECT_LIST_PROP,
                        null,
                        null, null, true, Optional.empty()));
    }
}

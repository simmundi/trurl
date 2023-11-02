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

package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

public class CommonFieldsFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public CommonFieldsFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return beanMetadata.getComponentProperties().stream()
                .flatMap(property -> {
                    if (property.type.columnType != null) {
                        return Stream.of(FieldSpec
                                .builder(property.type.columnType, property.fieldName, Modifier.PRIVATE)
                                .build());
                    } else if (property.isUsingReferences()) {
                        return reference(property);
                    } else if (property.isUsingJoin() && property.isUsingMappers()) {
                        return join(property);
                    } else if (property.isUsingJoin()) {
                        return join(property);
                    } else if (property.isUsingMappers() && !property.isUsingJoin()) {
                        return embedded(property);
                    } else {
                        throw new RuntimeException("Unknown property type: " + property.type);
                    }
                });
    }

    private Stream<? extends FieldSpec> embedded(ComponentProperty property) {
        return Stream.of(FieldSpec
                .builder(property.getMapperType(), property.fieldName, Modifier.PRIVATE)
                .build());
    }

    private Stream<FieldSpec> reference(ComponentProperty property) {
        return Stream.of(FieldSpec
                .builder(property.getReferenceType(), property.fieldName, Modifier.PRIVATE)
                .build());
    }

    private Stream<FieldSpec> join(ComponentProperty property) {
        return Stream.of(FieldSpec
                        .builder(property.getMapperType(), property.fieldName, Modifier.PRIVATE)
                        .build(),
                FieldSpec.builder(property.getJoinType(), property.fieldName + "Join", Modifier.PRIVATE)
                        .build());
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.empty();
    }

}

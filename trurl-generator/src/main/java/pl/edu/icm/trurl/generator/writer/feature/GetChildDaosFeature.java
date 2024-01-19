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

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetChildDaosFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public GetChildDaosFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideGetChildMappers(beanMetadata));
    }

    private MethodSpec overrideGetChildMappers(BeanMetadata beanMetadata) {
        List<ComponentProperty> childMappers = beanMetadata
                .getComponentProperties().stream()
                .filter(p -> p.type == PropertyType.EMBEDDED_PROP || p.type == PropertyType.EMBEDDED_LIST_PROP)
                .collect(Collectors.toList());
        return MethodSpec.methodBuilder("getChildDaos")
                .returns(CommonTypes.MAPPER_LIST)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addCode(childMappers.isEmpty() ? emptyList() : listMappers(childMappers))
                .build();
    }

    private CodeBlock listMappers(List<ComponentProperty> mapperProperties) {
        return CodeBlock.builder()
                .addStatement("return $T.asList($L)", CommonTypes.ARRAYS, mapperProperties.stream()
                        .map(p -> p.fieldName)
                        .collect(Collectors.joining(", ")))
                .build();
    }

    private CodeBlock emptyList() {
        return CodeBlock.builder()
                .addStatement("return $T.emptyList()", CommonTypes.COLLECTIONS)
                .build();
    }
}

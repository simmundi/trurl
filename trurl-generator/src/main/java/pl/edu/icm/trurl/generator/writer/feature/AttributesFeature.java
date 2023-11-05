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
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttributesFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public AttributesFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideAttributes(beanMetadata));
    }

    private MethodSpec overrideAttributes(BeanMetadata beanMetadata) {
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();
        return MethodSpec.methodBuilder("attributes")
                .returns(CommonTypes.ATTRIBUTE_LIST)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("$T result = new $T()", CommonTypes.ATTRIBUTE_LIST, CommonTypes.ATTRIBUTE_ARRAY_LIST)
                .addStatement("result.addAll($T.asList($L))", CommonTypes.ARRAYS, properties.stream()
                        .filter(p -> p.type.columnType != null)
                        .map(p -> p.fieldName)
                        .collect(Collectors.joining(", ")))
                .addStatement("$T.<$T>asList($L).stream().forEach(mapper -> result.addAll(mapper.attributes()))", CommonTypes.ARRAYS, CommonTypes.MAPPER, properties.stream()
                        .filter(p -> p.type == PropertyType.EMBEDDED_PROP)
                        .map(p -> p.fieldName)
                        .collect(Collectors.joining(", ")))
                .addStatement("return result")
                .build();
    }


}

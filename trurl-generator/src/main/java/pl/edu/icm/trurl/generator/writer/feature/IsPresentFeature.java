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
import com.squareup.javapoet.TypeName;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IsPresentFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public IsPresentFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideIsPresent(beanMetadata));
    }

    private MethodSpec overrideIsPresent(BeanMetadata beanMetadata) {
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("isPresent")
                .addParameter(TypeName.INT, "row")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN);

        method.addStatement("return $L",
                properties.stream()
                        .map(prop -> {
                            if (prop.type == PropertyType.EMBEDDED_PROP) {
                                return CodeBlock.of("$L.isPresent(row)", prop.fieldName);
                            } else if (prop.type == PropertyType.EMBEDDED_LIST_PROP) {
                                return CodeBlock.of("!$LJoin.isEmpty(row)", prop.fieldName);
                            } else {
                                return CodeBlock.of("!$L.isEmpty(row)", prop.fieldName);
                            }
                        }).collect(CodeBlock.joining(" || ")));

        return method.build();
    }

}

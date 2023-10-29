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
import com.squareup.javapoet.TypeName;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Stream;

public class EraseFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public EraseFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideAttachStore(beanMetadata));
    }

    private MethodSpec overrideAttachStore(BeanMetadata beanMetadata) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("erase")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "row");
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();
        for (ComponentProperty property : properties) {
            switch (property.type) {
                case EMBEDDED_LIST_PROP:
                    methodSpec.addStatement("$LJoin.setSize(row, 0)", property.fieldName);
                    break;
                case EMBEDDED_PROP:
                    methodSpec.addStatement("$L.erase(row)", property.fieldName);
                    break;
                case ENTITY_PROP: // fallthrough
                case ENTITY_LIST_PROP:
                    methodSpec.addStatement("$L.setSize(row, 0)", property.fieldName);
                    break;

                default:
                    methodSpec.addStatement("$L.setEmpty(row)", property.fieldName);
            }
        }

        return methodSpec.build();
    }
}

/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Stream;

public class AttachStoreFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public AttachStoreFeature(BeanMetadata beanMetadata) {
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
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();

        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("attachStore")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(CommonTypes.STORE, "store");

        methodSpec.addStatement("this.store = store");

        for (ComponentProperty property : properties) {
            switch (property.type) {
                case EMBEDDED_LIST_PROP:
                case EMBEDDED_DENSE_PROP:
                    methodSpec.addStatement("$LJoin = store.getJoin(daoPrefix + $S)", property.fieldName, property.qname);
                    methodSpec.addStatement("$L.attachStore($LJoin.getTarget())", property.fieldName, property.fieldName);
                    break;
                case EMBEDDED_PROP:
                    methodSpec.addStatement("$L.attachStore(store)", property.fieldName);
                    break;
                case ENTITY_LIST_PROP: // fallthrough
                case ENTITY_PROP:
                    methodSpec.addStatement("$L = store.getReference(daoPrefix + $S)", property.fieldName, property.qname);
                    break;
                default:
                    methodSpec.addStatement("$L = store.get(daoPrefix + $S)",
                            property.fieldName,
                            property.qname);
            }
        }
        return methodSpec.build();
    }
}

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
import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;
import pl.edu.icm.trurl.store.StoreConfigurer;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ConfigureStoreFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public ConfigureStoreFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.of(storeField());
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideConfigureStore(beanMetadata));
    }

    private FieldSpec storeField() {
        return FieldSpec.builder(CommonTypes.STORE, "store", Modifier.PRIVATE).build();
    }

    private MethodSpec overrideConfigureStore(BeanMetadata beanMetadata) {
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();
        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("configureStore")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(CommonTypes.COMPONENT_STORE_METADATA, "meta");

        for (ComponentProperty property : properties) {
            String name = property.qname;
            switch (property.type) {
                case INT_PROP:
                    methodSpec.addStatement("meta.addInt($S)", name);
                    break;
                case BOOLEAN_PROP:
                    methodSpec.addStatement("meta.addBoolean($S)", name);
                    break;
                case BYTE_PROP:
                    methodSpec.addStatement("meta.addByte($S)", name);
                    break;
                case DOUBLE_PROP:
                    methodSpec.addStatement("meta.addDouble($S)", name);
                    break;
                case SOFT_ENUM_PROP:
                    methodSpec.addStatement("meta.addSoftEnum($S, $L)", name, property.name + "Manager");
                    break;
                case ENUM_PROP:
                    methodSpec.addStatement("meta.addEnum($S, $T.class)", name, property.businessType);
                    break;
                case FLOAT_PROP:
                    methodSpec.addStatement("meta.addFloat($S)", name);
                    break;
                case SHORT_PROP:
                    methodSpec.addStatement("meta.addShort($S)", name);
                    break;
                case STRING_PROP:
                    methodSpec.addStatement("meta.addString($S)", name);
                    break;
                case EMBEDDED_LIST:
                    String methodName = property.type == PropertyType.EMBEDDED_LIST ? "rangeTyped" : "arrayTyped";
                    Optional<MappedCollection> mappedCollection = Optional.ofNullable(property.attribute.getAnnotation(MappedCollection.class));
                    int sizeMin = mappedCollection.map(MappedCollection::minReservation).orElse(1);
                    int sizeMargin = mappedCollection.map(MappedCollection::margin).orElse(2);

                    methodSpec.addStatement("$L = mappers.create($T.class, mapperPrefix + $S)", property.name, property.businessType, ".");
                    methodSpec.addStatement("$L.configureStore(meta.addReference($S).$L($L, $L).toSubstore())", property.name, name, methodName, sizeMin, sizeMargin);
                    break;
                case EMBEDDED_PROP:
                    methodSpec.addStatement("$L = mappers.create($T.class, mapperPrefix + $S)", property.name, property.businessType, ".");
                    methodSpec.addStatement("$L.configureStore(meta)");
                    break;

                default:
                    throw new IllegalStateException("Unknown entity type " + property.type);
            }
        }

        return methodSpec.build();
    }


}

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
import pl.edu.icm.trurl.ecs.annotation.CollectionType;
import pl.edu.icm.trurl.ecs.annotation.Mapped;
import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.ecs.annotation.Reverse;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

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
                    methodSpec.addStatement("meta.addInt(mapperPrefix + $S)", name);
                    break;
                case BOOLEAN_PROP:
                    methodSpec.addStatement("meta.addBoolean(mapperPrefix + $S)", name);
                    break;
                case BYTE_PROP:
                    methodSpec.addStatement("meta.addByte(mapperPrefix + $S)", name);
                    break;
                case DOUBLE_PROP:
                    methodSpec.addStatement("meta.addDouble(mapperPrefix + $S)", name);
                    break;
                case SOFT_ENUM_PROP:
                    methodSpec.addStatement("meta.addCategory(mapperPrefix + $S, $L)", name, property.name + "Manager");
                    break;
                case ENUM_PROP:
                    methodSpec.addStatement("meta.addEnum(mapperPrefix + $S, $T.class)", name, property.unwrappedTypeName);
                    break;
                case FLOAT_PROP:
                    methodSpec.addStatement("meta.addFloat(mapperPrefix + $S)", name);
                    break;
                case SHORT_PROP:
                    methodSpec.addStatement("meta.addShort(mapperPrefix + $S)", name);
                    break;
                case STRING_PROP:
                    methodSpec.addStatement("meta.addString(mapperPrefix + $S)", name);
                    break;
                case EMBEDDED_LIST_PROP:
                    Optional<MappedCollection> mappedCollection = Optional.ofNullable(property.attribute.getAnnotation(MappedCollection.class));
                    int sizeMin = mappedCollection.map(MappedCollection::minReservation).orElse(1);
                    int sizeMargin = mappedCollection.map(MappedCollection::margin).orElse(2);
                    String methodName = mappedCollection.map(MappedCollection::collectionType).orElse(CollectionType.RANGE) == CollectionType.RANGE ? "rangeTyped" : "arrayTyped";

                    methodSpec.addStatement("$L = ($T) mappers.create($T.class, mapperPrefix + $S)", property.fieldName, property.getMapperType(), property.unwrappedTypeName, property.name);
                    methodSpec.addStatement("$L.configureStore(meta.addJoin(mapperPrefix + $S).$L($L, $L))", property.fieldName, name, methodName, sizeMin, sizeMargin);
                    break;
                case EMBEDDED_PROP:
                    methodSpec.addStatement("$L = ($T) mappers.create($T.class, mapperPrefix + $S)", property.fieldName, property.getMapperType(), property.unwrappedTypeName, property.name);
                    methodSpec.addStatement("$L.configureStore(meta)", property.fieldName);
                    break;
                case EMBEDDED_DENSE_PROP:
                    methodSpec.addStatement("$L = ($T) mappers.create($T.class, mapperPrefix + $S)", property.fieldName, property.getMapperType(), property.unwrappedTypeName, property.name);
                    Reverse reverse = Optional.ofNullable(property.attribute.getAnnotation(Mapped.class)).map(Mapped::reverse).orElse(Reverse.NO_REVERSE_ATTRIBUTE);
                    switch (reverse) {
                        case NO_REVERSE_ATTRIBUTE:
                            methodSpec.addStatement("$L.configureStore(meta.addJoin(mapperPrefix + $S).singleTyped())", property.fieldName, name);
                            break;
                        case WITH_REVERSE_ATTRIBUTE:
                            methodSpec.addStatement("$L.configureStore(meta.addJoin(mapperPrefix + $S).singleTypedWithReverse())", property.fieldName, name);
                            break;
                        case ONLY_REVERSE_ATTRIBUTE:
                            methodSpec.addStatement("$L.configureStore(meta.addJoin(mapperPrefix + $S).singleTypedWithReverseOnly())", property.fieldName, name);
                            break;
                        default:
                            throw new IllegalStateException("Unknown reverse type " + reverse);
                    }
//                    methodSpec.addStatement("$L.configureStore(meta.addJoin(mapperPrefix + $S).singleTyped())", property.fieldName, name);
                    break;
                case ENTITY_LIST_PROP:
                    methodSpec.addStatement("meta.addReference(mapperPrefix + $S).arrayTyped($L, $L)", name, 1, 2);
                    break;
                case ENTITY_PROP:
                    methodSpec.addStatement("meta.addReference(mapperPrefix + $S).single()", name);
                    break;
                default:
                    throw new IllegalStateException("Unknown entity type " + property.type);
            }
        }

        return methodSpec.build();
    }


}

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
import pl.edu.icm.trurl.ecs.annotation.CollectionType;
import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentFeature;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.Optional;
import java.util.stream.Stream;

public class SaveFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public SaveFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideSave(), writeStoreValues());
    }

    private MethodSpec overrideSave() {
        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CommonTypes.SESSION, "session")
                .addParameter(beanMetadata.componentName, "component")
                .addParameter(TypeName.INT, "row")
                .addAnnotation(Override.class);

        if (beanMetadata.componentFeatures.contains(ComponentFeature.CAN_RESOLVE_CONFLICTS)) {
            methodSpec.addCode(CodeBlock.builder()
                    .beginControlFlow("if (!parallelMode)")
                    .addStatement("storeValues(component, row)")
                    .addStatement("return")
                    .endControlFlow()
                    .addStatement("int ownerId = session.getOwnerId()")
                    .beginControlFlow("while (true)")
                    .addStatement("int currentOwnerId = owners.get(row)")
                    .addStatement("if (currentOwnerId < 0) continue")
                    .beginControlFlow("if (owners.compareAndSet(row, currentOwnerId, -ownerId))")
                    .addStatement("$T resolved = component", beanMetadata.componentName)
                    .beginControlFlow("if (currentOwnerId != component.getOwnerId())")
                    .addStatement("$T other = create()", beanMetadata.componentName)
                    .addStatement("fetchValues(session, other, row)")
                    .addStatement("resolved = component.resolve(other)")
                    .endControlFlow()
                    .addStatement("storeValues(resolved, row)")
                    .addStatement("owners.set(row, ownerId)")
                    .addStatement("break")
                    .endControlFlow()
                    .endControlFlow()
                    .build());

        } else {
            methodSpec.addStatement("storeValues(component, row)");
        }

        return methodSpec.build();
    }

    private MethodSpec writeStoreValues() {
        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("storeValues")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(beanMetadata.componentName, "component")
                .addParameter(TypeName.INT, "row");

        if (beanMetadata.componentFeatures.contains(ComponentFeature.CAN_BE_NORMALIZED)) {
            methodSpec.addStatement("component.normalize()");
        }

        methodSpec
                .addCode(CodeBlock.builder()
                        .addStatement("int current = count.get()")
                        .addStatement("if (row < current && !isModified(component, row)) return")
                        .beginControlFlow("while (row >= current)")
                        .addStatement("boolean ok = count.compareAndSet(current, row + 1)")
                        .addStatement("current = count.get()")
                        .addStatement("if (!ok) continue")
                        .addStatement("ensureCapacity(current)")
                        .endControlFlow().build());

        methodSpec
                .addStatement("mapperListeners.fireSavingComponent(component, row)");

        for (ComponentProperty property : beanMetadata.getComponentProperties()) {
            if (property.synthetic) continue;
            switch (property.type) {
                case INT_PROP:
                    methodSpec.addStatement("$L.setInt(row, component.$L())", property.name, property.getterName);
                    break;
                case BOOLEAN_PROP:
                    methodSpec.addStatement("$L.setBoolean(row, component.$L())", property.name, property.getterName);
                    break;
                case BYTE_PROP:
                    methodSpec.addStatement("$L.setByte(row, component.$L())", property.name, property.getterName);
                    break;
                case DOUBLE_PROP:
                    methodSpec.addStatement("$L.setDouble(row, component.$L())", property.name, property.getterName);
                    break;
                case ENUM_PROP:
                    methodSpec.addStatement("$L.setEnum(row, component.$L())", property.name, property.getterName);
                    break;
                case SOFT_ENUM_PROP:
                    methodSpec.addStatement("$L.setEnum(row, component.$L())", property.name, property.getterName);
                    break;
                case FLOAT_PROP:
                    methodSpec.addStatement("$L.setFloat(row, component.$L())", property.name, property.getterName);
                    break;
                case SHORT_PROP:
                    methodSpec.addStatement("$L.setShort(row, component.$L())", property.name, property.getterName);
                    break;
                case STRING_PROP:
                    methodSpec.addStatement("$L.setString(row, component.$L())", property.name, property.getterName);
                    break;
                case ENTITY_LIST_PROP:
                    methodSpec.addStatement("$L.saveIds(row, component.$L().size(), (idx) -> component.$L().get(idx).getId())", property.name, property.getterName, property.getterName);
                    break;
                case ENTITY_PROP:
                    methodSpec.addStatement("$L.setEntity(row, component.$L())", property.name, property.getterName);
                    break;
                case EMBEDDED_PROP:
                    methodSpec.addStatement("if (component.$L() != null) $L.save(component.$L(), row)", property.getterName, property.name, property.getterName);
                    break;
                case EMBEDDED_LIST:
                    createEmbeddedList(methodSpec, property);
                    break;
                default:
                    throw new IllegalStateException("Unknown property type " + property.type);
            }
        }

        return methodSpec
                .build();
    }

    private void createEmbeddedList(MethodSpec.Builder methodSpec, ComponentProperty property) {
        {
            Optional<MappedCollection> mappedCollection = Optional.ofNullable(property.attribute.getAnnotation(MappedCollection.class));
            CollectionType collectionType = mappedCollection.map(MappedCollection::collectionType).orElse(CollectionType.RANGE);
            switch (collectionType) {
                case ARRAY_LIST:
                    methodSpec
                            .addCode(
                                    CodeBlock.builder()
                                            .beginControlFlow("")
                                            .addStatement("if ($L.getCount() == 0) $L.setCount(1)", property.name, property.name)
                                            .addStatement("int size = component.$L().size()", property.getterName)
                                            .addStatement("int lastIndexOrCount = $L_ids.saveIds(row, size, $L.getCount())", property.name, property.name)
                                            .addStatement("$L.setCount(lastIndexOrCount)", property.name)
                                            .addStatement("int[] ids = new int[size]")
                                            .addStatement("$L_ids.loadIds(row, (index, value) -> ids[index] = value)", property.name)
                                            .beginControlFlow("for (int i = 0; i < size; i++)")
                                            .addStatement("$L.save(component.$L().get(i), ids[i])", property.name, property.getterName)
                                            .endControlFlow()
                                            .endControlFlow()
                                            .build()
                            );
                    break;
                case RANGE:
                    int sizeMin = mappedCollection.map(MappedCollection::minReservation).orElse(1);
                    int sizeMargin = mappedCollection.map(MappedCollection::margin).orElse(2);

                    methodSpec
                            .addCode(
                                    CodeBlock.builder()
                                            .beginControlFlow("")
                                            .addStatement("int size = component.$L().size()", property.getterName)
                                            .beginControlFlow("if (size > 127)")
                                            .addStatement("throw new IllegalStateException(\"Embedded lists over 127 elements are not supported\")")
                                            .endControlFlow()
                                            .beginControlFlow("if (size == 0 && !$L_start.isEmpty(row))", property.name)
                                            .addStatement("int start = $L_start.getInt(row)", property.name)
                                            .addStatement("$L.setEmpty(start)", property.name)
                                            .endControlFlow()
                                            .beginControlFlow("if (size > 0 && $L_start.isEmpty(row))", property.name)
                                            .addStatement("byte sizeMin = $L", sizeMin)
                                            .addStatement("byte sizeMargin = $L", sizeMargin)
                                            .addStatement("byte length = (byte) (Math.max(size, sizeMin - sizeMargin ) + sizeMargin)")
                                            .addStatement("$L_start.setInt(row, $L.getAndUpdateCount(length))", property.name, property.name)
                                            .addStatement("$L_length.setByte(row, length)", property.name)
                                            .endControlFlow()
                                            .beginControlFlow("if (size > 0)", property.name)
                                            .addStatement("int length = $L_length.getByte(row)", property.name)
                                            .addStatement("int start = $L_start.getInt(row)", property.name)
                                            .beginControlFlow("if (size > length)")
                                            .addStatement("throw new IllegalStateException(\"resizing this list over \" + length + \" is not supported\")")
                                            .endControlFlow()
                                            .beginControlFlow("for (int i = 0; i < size; i++)")
                                            .addStatement("$L.save(component.$L().get(i), i + start)", property.name, property.getterName)
                                            .endControlFlow()
                                            .beginControlFlow("if (size < length)")
                                            .addStatement("$L.setEmpty(start + size)", property.name)
                                            .endControlFlow()
                                            .endControlFlow()
                                            .endControlFlow()
                                            .build()
                            );
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + collectionType);
            }
        }
    }
}

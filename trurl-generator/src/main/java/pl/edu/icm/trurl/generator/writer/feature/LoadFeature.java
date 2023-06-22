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

import com.squareup.javapoet.ClassName;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class LoadFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public LoadFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return beanMetadata.componentFeatures.contains(ComponentFeature.CAN_RESOLVE_CONFLICTS)
                ? Stream.of(FieldSpec.builder(CommonTypes.ATOMIC_INTEGER_ARRAY, "owners", Modifier.PRIVATE, Modifier.VOLATILE)
                        .initializer("new $T(0)", CommonTypes.ATOMIC_INTEGER_ARRAY).build(),
                FieldSpec.builder(ClassName.BOOLEAN, "parallelMode").build())
                : Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(
                overrideLoad(),
                fetchValues()
        );
    }

    private MethodSpec overrideLoad() {
        ClassName component = beanMetadata.componentName;
        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("load")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CommonTypes.SESSION, "session")
                .addParameter(component, "component")
                .addParameter(TypeName.INT, "row")
                .addAnnotation(Override.class);
        if (beanMetadata.componentFeatures.contains(ComponentFeature.CAN_RESOLVE_CONFLICTS)) {
            methodSpec
                    .addCode(CodeBlock.builder()
                            .beginControlFlow("if (!parallelMode)")
                            .add(callFetchValues())
                            .addStatement("return")
                            .endControlFlow()
                            .beginControlFlow("while (true)")
                            .addStatement("int currentOwner = owners.get(row)")
                            .addStatement("if (currentOwner < 0) continue")
                            .addStatement("if (currentOwner == 0 && !owners.compareAndSet(row, 0, currentOwner)) continue;")
                            .add(callFetchValues())
                            .beginControlFlow("if (owners.get(row) == currentOwner)")
                            .addStatement("component.setOwnerId(currentOwner)")
                            .addStatement("break")
                            .endControlFlow()
                            .endControlFlow()
                            .build());
        } else {
            methodSpec.addCode(callFetchValues());
        }
        return methodSpec.build();
    }

    private CodeBlock callFetchValues() {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock
                .addStatement("fetchValues(session, component, row)");
        if (beanMetadata.componentFeatures.contains(ComponentFeature.REQUIRES_ORIGINAL_COPY)) {
            codeBlock
                    .addStatement("$T copy = create()", beanMetadata.componentName)
                    .addStatement("fetchValues(session, copy, row)")
                    .addStatement("component.setOriginalCopy(copy)");
        }
        if (beanMetadata.componentFeatures.contains(ComponentFeature.IS_DIRTY_MARKED)) {
            codeBlock
                    .addStatement("component.markAsClean()");
        }
        if (beanMetadata.componentFeatures.contains(ComponentFeature.REQUIRES_SETUP)) {
            codeBlock
                    .addStatement("component.setup()");
        }
        return codeBlock.build();
    }

    private MethodSpec fetchValues() {
        ClassName component = beanMetadata.componentName;
        List<ComponentProperty> componentProperties = beanMetadata.getComponentProperties();
        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("fetchValues")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(CommonTypes.SESSION, "session")
                .addParameter(component, "component")
                .addParameter(TypeName.INT, "row");

        for (ComponentProperty property : componentProperties) {
            if (property.synthetic) continue;

            switch (property.type) {
                case INT_PROP:
                    methodSpec.addStatement("component.$L($L.getInt(row))", property.setterName, property.name);
                    break;
                case BOOLEAN_PROP:
                    methodSpec.addStatement("component.$L($L.getBoolean(row))", property.setterName, property.name);
                    break;
                case BYTE_PROP:
                    methodSpec.addStatement("component.$L($L.getByte(row))", property.setterName, property.name);
                    break;
                case DOUBLE_PROP:
                    methodSpec.addStatement("component.$L($L.getDouble(row))", property.setterName, property.name);
                    break;
                case ENUM_PROP:
                    methodSpec.addStatement("component.$L(($T)$L.getEnum(row))", property.setterName, property.businessType, property.name);
                    break;
                case SOFT_ENUM_PROP:
                    methodSpec.addStatement("component.$L(($T)$L.getEnum(row))", property.setterName, property.businessType, property.name);
                    break;
                case FLOAT_PROP:
                    methodSpec.addStatement("component.$L($L.getFloat(row))", property.setterName, property.name);
                    break;
                case SHORT_PROP:
                    methodSpec.addStatement("component.$L($L.getShort(row))", property.setterName, property.name);
                    break;
                case STRING_PROP:
                    methodSpec.addStatement("component.$L($L.getString(row))", property.setterName, property.name);
                    break;
                case EMBEDDED_PROP:
                    String instanceName = property.name + "Instance";
                    methodSpec.addCode(CodeBlock.builder()
                            .beginControlFlow("if ($L.isPresent(row))", property.name)
                            .addStatement("$T $L = ($T)$L.create()", property.businessType, instanceName, property.businessType, property.name)
                            .addStatement("$L.load(session, $L, row)", property.name, instanceName)
                            .addStatement("component.$L($L)", property.setterName, instanceName)
                            .endControlFlow()
                            .build());

                    break;
                case EMBEDDED_LIST:
                    createEmbeddedList(methodSpec, property);
                    break;
                default:
                    throw new IllegalStateException("Unknown entity type " + property.type);
            }
        }

        return methodSpec.build();
    }

    private void createEmbeddedList(MethodSpec.Builder methodSpec, ComponentProperty property) {
        {
            CollectionType collectionType = Optional.ofNullable(property.attribute.getAnnotation(MappedCollection.class))
                    .map(MappedCollection::collectionType).orElse(CollectionType.RANGE);
            switch (collectionType) {
                case ARRAY_LIST:
                    methodSpec
                            .addCode(
                                    CodeBlock.builder()
                                            .beginControlFlow("if (!$L_ids.isEmpty(row))", property.name)
                                            .addStatement("int[] ids = new int[$L_ids.getSize(row)]", property.name)
                                            .addStatement("$L_ids.loadIds(row, (index, value) -> ids[index] = value)", property.name)
                                            .beginControlFlow("for (int id : ids)")
                                            .addStatement("$T element = ($T) $L.create()", property.businessType, property.businessType, property.name)
                                            .addStatement("$L.load(session, element, id)", property.name)
                                            .addStatement("component.$L().add(element)", property.getterName)
                                            .endControlFlow()
                                            .endControlFlow()
                                            .build()
                            );
                    break;
                case RANGE:
                    methodSpec
                            .addCode(
                                    CodeBlock.builder()
                                            .beginControlFlow("if (!$L_start.isEmpty(row))", property.name)
                                            .addStatement("int length = $L_length.getByte(row)", property.name)
                                            .addStatement("int start = $L_start.getInt(row)", property.name)
                                            .addStatement("component.$L().clear()", property.getterName)
                                            .beginControlFlow("for (int i = start; i < start + length; i++)")
                                            .addStatement("if (!$L.isPresent(i)) break", property.name)
                                            .addStatement("$T element = ($T) $L.create()", property.businessType, property.businessType, property.name)
                                            .addStatement("$L.load(session, element, i)", property.name)
                                            .addStatement("component.$L().add(element)", property.getterName)
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

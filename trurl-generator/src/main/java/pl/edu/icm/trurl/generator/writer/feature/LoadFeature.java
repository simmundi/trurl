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

import com.squareup.javapoet.*;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentFeature;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.List;
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
                .addParameter(CommonTypes.ABSTRACT_SESSION, "session")
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
//                            .addStatement("if (currentOwner == 0 && !owners.compareAndSet(row, 0, currentOwner)) continue")
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
                .addParameter(CommonTypes.ABSTRACT_SESSION, "session")
                .addParameter(component, "component")
                .addParameter(TypeName.INT, "row");

        for (ComponentProperty property : componentProperties) {
            switch (property.type) {
                case INT_PROP:
                    methodSpec.addStatement("component.$L($L.getInt(row))", property.setterName, property.fieldName);
                    break;
                case BOOLEAN_PROP:
                    methodSpec.addStatement("component.$L($L.getBoolean(row))", property.setterName, property.fieldName);
                    break;
                case BYTE_PROP:
                    methodSpec.addStatement("component.$L($L.getByte(row))", property.setterName, property.fieldName);
                    break;
                case DOUBLE_PROP:
                    methodSpec.addStatement("component.$L($L.getDouble(row))", property.setterName, property.fieldName);
                    break;
                case ENUM_PROP:
                    methodSpec.addStatement("component.$L(($T)$L.getEnum(row))", property.setterName, property.unwrappedTypeName, property.fieldName);
                    break;
                case SOFT_ENUM_PROP:
                    methodSpec.addStatement("component.$L(($T)$L.getEnum(row))", property.setterName, property.unwrappedTypeName, property.fieldName);
                    break;
                case FLOAT_PROP:
                    methodSpec.addStatement("component.$L($L.getFloat(row))", property.setterName, property.fieldName);
                    break;
                case SHORT_PROP:
                    methodSpec.addStatement("component.$L($L.getShort(row))", property.setterName, property.fieldName);
                    break;
                case STRING_PROP:
                    methodSpec.addStatement("component.$L($L.getString(row))", property.setterName, property.fieldName);
                    break;
                case EMBEDDED_PROP:
                    createEmbedded(methodSpec, property);
                    break;
                case EMBEDDED_DENSE_PROP:
                    createEmbeddedDense(methodSpec, property);
                    break;
                case EMBEDDED_LIST_PROP:
                    createEmbeddedList(methodSpec, property);
                    break;
                case ENTITY_LIST_PROP:
                    createEntityList(methodSpec, property);
                    break;
                case ENTITY_PROP:
                    createEntity(methodSpec, property);
                    break;
                default:
                    throw new IllegalStateException("Unknown entity type " + property.type);
            }
        }

        return methodSpec.build();
    }

    private void createEmbeddedDense(MethodSpec.Builder methodSpec, ComponentProperty property) {
        String instanceName = "$" + property.name + "Instance";
        String targetRow = "$" + property.name + "TargetRow";
        methodSpec.addCode(CodeBlock.builder()
                .addStatement("int $L = $LJoin.getRow(row, 0)", targetRow, property.fieldName)
                .beginControlFlow("if ($L != Integer.MIN_VALUE)", targetRow)
                .addStatement("$T $L = $L.createAndLoad(session, $L)", property.unwrappedTypeName, instanceName, property.fieldName, targetRow)
                .addStatement("component.$L($L)", property.setterName, instanceName)
                .endControlFlow()
                .build());
    }

    private void createEmbedded(MethodSpec.Builder methodSpec, ComponentProperty property) {
        String instanceName = "$" + property.name + "Instance";
        methodSpec.addCode(CodeBlock.builder()
                .beginControlFlow("if ($L.isPresent(row))", property.fieldName)
                .addStatement("$T $L = $L.createAndLoad(session, row)", property.unwrappedTypeName, instanceName, property.fieldName)
                .addStatement("component.$L($L)", property.setterName, instanceName)
                .endControlFlow()
                .build());
    }

    private void createEmbeddedList(MethodSpec.Builder methodSpec, ComponentProperty property) {
        String instanceName = "$" + property.name + "Instance";
        methodSpec.addCode(CodeBlock.builder()
                .beginControlFlow("for (int idx = 0;;idx++)")
                .addStatement("int embeddedRow = $LJoin.getRow(row, idx)", property.fieldName)
                .addStatement("if (embeddedRow == Integer.MIN_VALUE || !$L.isPresent(embeddedRow)) break", property.fieldName)
                .addStatement("$T $L = $L.createAndLoad(session, embeddedRow)", property.unwrappedTypeName, instanceName, property.fieldName)
                .addStatement("component.$L().add($L)", property.getterName, instanceName)
                .endControlFlow()
                .build());
    }

    private void createEntity(MethodSpec.Builder methodSpec, ComponentProperty property) {
        methodSpec.addCode(CodeBlock.builder()
                .beginControlFlow("if (!$L.isEmpty(row))", property.fieldName)
                .addStatement("component.$L(session.getEntity($L.getId(row, 0)))", property.setterName, property.fieldName)
                .endControlFlow()
                .build());
    }

    private void createEntityList(MethodSpec.Builder methodSpec, ComponentProperty property) {
        methodSpec.addCode(CodeBlock.builder()
                .beginControlFlow("for (int idx = 0;;idx++)")
                .addStatement("int embeddedId = $L.getId(row, idx)", property.fieldName)
                .addStatement("if (embeddedId == Integer.MIN_VALUE) break;")
                .addStatement("component.$L().add(session.getEntity(embeddedId))", property.getterName)
                .endControlFlow()
                .build());
    }

}

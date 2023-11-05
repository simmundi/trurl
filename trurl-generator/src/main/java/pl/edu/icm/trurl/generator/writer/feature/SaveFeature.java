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
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentFeature;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
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

        for (ComponentProperty property : beanMetadata.getComponentProperties()) {
            switch (property.type) {
                case INT_PROP:
                    methodSpec.addStatement("$L.setInt(row, component.$L())", property.fieldName, property.getterName);
                    break;
                case BOOLEAN_PROP:
                    methodSpec.addStatement("$L.setBoolean(row, component.$L())", property.fieldName, property.getterName);
                    break;
                case BYTE_PROP:
                    methodSpec.addStatement("$L.setByte(row, component.$L())", property.fieldName, property.getterName);
                    break;
                case DOUBLE_PROP:
                    methodSpec.addStatement("$L.setDouble(row, component.$L())", property.fieldName, property.getterName);
                    break;
                case ENUM_PROP:
                    methodSpec.addStatement("$L.setEnum(row, component.$L())", property.fieldName, property.getterName);
                    break;
                case SOFT_ENUM_PROP:
                    methodSpec.addStatement("$L.setEnum(row, component.$L())", property.fieldName, property.getterName);
                    break;
                case FLOAT_PROP:
                    methodSpec.addStatement("$L.setFloat(row, component.$L())", property.fieldName, property.getterName);
                    break;
                case SHORT_PROP:
                    methodSpec.addStatement("$L.setShort(row, component.$L())", property.fieldName, property.getterName);
                    break;
                case STRING_PROP:
                    methodSpec.addStatement("$L.setString(row, component.$L())", property.fieldName, property.getterName);
                    break;
                case EMBEDDED_PROP:
                    createEmbedded(methodSpec, property);
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
                    throw new IllegalStateException("Unknown property type " + property.type);
            }
        }

        return methodSpec
                .build();
    }

    private void createEntity(MethodSpec.Builder methodSpec, ComponentProperty property) {
        methodSpec
                .beginControlFlow("if (component.$L() != null)", property.getterName)
                .addStatement("$L.setId(row, 0, component.$L().getId())", property.fieldName, property.getterName)
                .nextControlFlow("else")
                .addStatement("$L.setSize(row, 0)", property.fieldName)
                .endControlFlow();
    }

    private void createEmbedded(MethodSpec.Builder methodSpec, ComponentProperty property) {
        methodSpec
                .beginControlFlow("if (component.$L() != null)", property.getterName)
                .addStatement("$L.save(component.$L(), row)", property.fieldName, property.getterName)
                .nextControlFlow("else")
                .addStatement("$L.erase(row)", property.fieldName)
                .endControlFlow();
    }

    private void createEmbeddedList(MethodSpec.Builder methodSpec, ComponentProperty property) {
        String instanceName = "$" + property.name + "Instance";
        methodSpec.beginControlFlow("")
                .addStatement("int size = component.$L().size()", property.getterName)
                .addStatement("$LJoin.setSize(row, size)", property.fieldName)
                .beginControlFlow("for (int idx = 0; idx < size; idx++)")
                .addStatement("$T $L = component.$L().get(idx)", property.unwrappedTypeName, instanceName, property.getterName)
                .addStatement("int targetRow = $LJoin.getRow(row, idx)", property.fieldName)
                .addStatement("$L.save($L, targetRow)", property.fieldName, instanceName)
                .endControlFlow()
                .endControlFlow();
    }

    private void createEntityList(MethodSpec.Builder methodSpec, ComponentProperty property) {
        String instanceName = "$" + property.name + "Instance";
        methodSpec.beginControlFlow("")
                .addStatement("int size = component.$L().size()", property.getterName)
                .addStatement("$L.setSize(row, size)", property.fieldName)
                .beginControlFlow("for (int idx = 0; idx < size; idx++)")
                .addStatement("$T $L = component.$L().get(idx)", CommonTypes.ENTITY, instanceName, property.getterName)
                .addStatement("$L.setId(row, idx, $L.getId())", property.fieldName, instanceName)
                .endControlFlow()
                .endControlFlow();
    }
}

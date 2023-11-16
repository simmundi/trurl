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
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import javax.lang.model.element.Modifier;
import java.util.function.Function;
import java.util.stream.Stream;

public class ColumnarAccessFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public ColumnarAccessFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(
                propertiesForAttributeAccess().map(this::createAttributeAccessor),
                propertiesForMapperAccess().map(this::createMapperAccessor),
                propertiesForSingleReferenceAccess().map(this::createSingleReferenceAccessor)
        ).flatMap(Function.identity());
    }

    private Stream<ComponentProperty> propertiesForAttributeAccess() {
        return beanMetadata.getComponentProperties()
                .stream()
                .filter(property -> property.getterName != null && property.type.columnType != null);
    }



    private Stream<ComponentProperty> propertiesForMapperAccess() {
        return beanMetadata.getComponentProperties()
                .stream()
                .filter(property -> property.type == PropertyType.EMBEDDED_LIST_PROP || property.type == PropertyType.EMBEDDED_PROP);
    }

    private Stream<ComponentProperty> propertiesForSingleReferenceAccess() {
        return beanMetadata.getComponentProperties()
                .stream()
                .filter(property -> property.type == PropertyType.ENTITY_PROP);
    }

    private MethodSpec createMapperAccessor(ComponentProperty property) {
        return  MethodSpec.methodBuilder(property.getterName + "Mapper")
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(CommonTypes.MAPPER, property.unwrappedTypeName))
                .addStatement("return $L", property.fieldName)
                .build();
    }

    private MethodSpec createSingleReferenceAccessor(ComponentProperty property) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(property.getterName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.INT, "row")
                .returns(ClassName.INT);

        return methodSpec.addCode(CodeBlock.builder()
                .beginControlFlow("if (!$L.isEmpty(row))", property.fieldName)
                .addStatement("return $L.getId(row, 0)", property.fieldName)
                .nextControlFlow("else")
                .addStatement("return Integer.MIN_VALUE")
                .endControlFlow()
                .build()).build();
    }

    private MethodSpec createAttributeAccessor(ComponentProperty property) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(property.getterName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.INT, "row")
                .returns(property.typeName);

        switch (property.type) {
            case INT_PROP:
                methodSpec.addStatement("return $L.getInt(row)", property.fieldName);
                break;
            case BOOLEAN_PROP:
                methodSpec.addStatement("return $L.getBoolean(row)", property.fieldName);
                break;
            case BYTE_PROP:
                methodSpec.addStatement("return $L.getByte(row)", property.fieldName);
                break;
            case DOUBLE_PROP:
                methodSpec.addStatement("return $L.getDouble(row)", property.fieldName);
                break;
            case ENUM_PROP: // fallthrough
            case SOFT_ENUM_PROP:
                methodSpec.addStatement("return ($T)$L.getEnum(row)", property.unwrappedTypeName, property.fieldName);
                break;
            case FLOAT_PROP:
                methodSpec.addStatement("return $L.getFloat(row)", property.fieldName);
                break;
            case SHORT_PROP:
                methodSpec.addStatement("return $L.getShort(row)", property.fieldName);
                break;
            case STRING_PROP:
                methodSpec.addStatement("return $L.getString(row)", property.fieldName);
                break;
            default:
                throw new IllegalStateException("Unknown entity type " + property.type);
        }
        return methodSpec.build();
    }
}

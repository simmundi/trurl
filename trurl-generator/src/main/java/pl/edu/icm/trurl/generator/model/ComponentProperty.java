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

package pl.edu.icm.trurl.generator.model;

import com.google.common.base.Strings;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.snowyhollows.bento.category.Category;
import pl.edu.icm.trurl.ecs.dao.annotation.*;
import pl.edu.icm.trurl.generator.CommonTypes;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Locale;
import java.util.Optional;

public class ComponentProperty {
    public final String name;
    public final String fieldName;
    public final String namespace;
    public final PropertyType type;
    public final TypeName typeName;
    public final String getterName;
    public final String setterName;
    public final ClassName unwrappedTypeName;
    public final String qname;
    public final Element attribute;

    public ComponentProperty(String name, String namespace, PropertyType type, String getterName, ClassName unwrappedTypeName, TypeName typeName, Optional<? extends Element> optionalAttribute) {
        this.typeName = typeName;
        this.fieldName = "$" + name;
        this.name = name;
        this.namespace = namespace;
        this.type = type;
        this.getterName = getterName;
        this.setterName = "set" + name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        this.unwrappedTypeName = unwrappedTypeName;
        this.qname = Strings.isNullOrEmpty(namespace) ? name : namespace + "." + name;
        this.attribute = optionalAttribute.orElse(null);
    }

    public static boolean isGetter(ExecutableElement method) {
        TypeName returnType = ClassName.get(method.getReturnType());
        String methodName = method.getSimpleName().toString();
        return (methodName.startsWith("get") && returnType != TypeName.VOID)
                || (methodName.startsWith("is") && returnType == TypeName.BOOLEAN);
    }

    public static ComponentProperty create(ProcessingEnvironment processingEnvironment, BeanPropertyMetadata meta) {
        String methodName = meta.getter.getSimpleName().toString();
        String propertyName = findPropertyName(methodName);
        Optional<? extends Element> optionalAttribute = meta.getter.getEnclosingElement().getEnclosedElements().stream()
                .filter(e -> !(e instanceof ExecutableElement))
                .filter(e -> e.getSimpleName().toString().equals(propertyName))
                .filter(e -> !e.getModifiers().contains(Modifier.VOLATILE))
                .filter(e -> e.getAnnotation(NotMapped.class) == null)
                .findFirst();
        PropertyType type = fromColumnType(processingEnvironment, meta.getter.getReturnType(), optionalAttribute);
        ClassName wrappedType = findWrappedType(processingEnvironment, meta);

        return new ComponentProperty(propertyName,
                meta.namespace,
                type,
                methodName,
                wrappedType,
                TypeName.get(meta.getter.getReturnType()),
                optionalAttribute);
    }

    private static ClassName findWrappedType(ProcessingEnvironment processingEnvironment, BeanPropertyMetadata meta) {
        ClassName indirectBusinessType = findIndirectBusinessType(processingEnvironment, meta.getter.getReturnType());
        TypeName returnType = ClassName.get(meta.getter.getReturnType());
        ClassName returnClassName = returnType instanceof ClassName ? (ClassName) returnType : null;
        return Optional
                .ofNullable(indirectBusinessType)
                .orElse(returnClassName);
    }

    private static String findPropertyName(String methodName) {
        String prefix = methodName.startsWith("get") ? "get" : "is";
        String propertyName = methodName.substring(prefix.length(), prefix.length() + 1).toLowerCase(Locale.ROOT)
                + methodName.substring(prefix.length() + 1);
        return propertyName;
    }

    /**
     * In some cases, the direct type (List, Enum) is different from the business type which it wraps.
     * e.g. a getMedicalRecords() method returns a list of MedicalRecord; the actual type is List, but
     * we need to create a dao for MedicalRecord.
     * <p>
     * This method uses case-by-case rules to establish the relevant wrapped type.
     */
    private static ClassName findIndirectBusinessType(ProcessingEnvironment processingEnvironment, TypeMirror typeMirror) {
        TypeName typeName = TypeName.get(typeMirror);
        if (isEnum(processingEnvironment, typeMirror, typeName)) {
            return (ClassName) ClassName.get(typeMirror);
        } else if (isList(typeName)) {
            return (ClassName) ((ParameterizedTypeName) typeName).typeArguments.get(0);
        } else {
            return null;
        }
    }

    private static boolean isList(TypeName typeName) {
        return typeName instanceof ParameterizedTypeName
                && ((ParameterizedTypeName) typeName).rawType.equals(CommonTypes.LIST);
    }

    private static boolean isEnum(ProcessingEnvironment processingEnvironment, TypeMirror typeMirror, TypeName typeName) {
        return !typeName.isPrimitive() && processingEnvironment.getTypeUtils().asElement(typeMirror).getKind() == ElementKind.ENUM;
    }

    private static PropertyType fromColumnType(ProcessingEnvironment processingEnvironment, TypeMirror typeMirror, Optional<? extends Element> optionalAttribute) {
        TypeName typeName = TypeName.get(typeMirror);
        if (typeName.equals(TypeName.DOUBLE)) {
            return PropertyType.DOUBLE_PROP;
        } else if (typeName.equals(TypeName.SHORT)) {
            return PropertyType.SHORT_PROP;
        } else if (typeName.equals(TypeName.FLOAT)) {
            return PropertyType.FLOAT_PROP;
        } else if (typeName.equals(TypeName.BYTE)) {
            return PropertyType.BYTE_PROP;
        } else if (typeName.equals(TypeName.INT)) {
            return PropertyType.INT_PROP;
        } else if (typeName.equals(TypeName.BOOLEAN)) {
            return PropertyType.BOOLEAN_PROP;
        } else if (typeName.equals(CommonTypes.LANG_STRING)) {
            return PropertyType.STRING_PROP;
        } else if (isEnum(processingEnvironment, typeMirror, typeName)) {
            return PropertyType.ENUM_PROP;
        } else if (typeName.equals(CommonTypes.ENTITY_LIST)) {
            return PropertyType.ENTITY_LIST_PROP;
        } else if (typeName.equals(CommonTypes.ENTITY)) {
            return PropertyType.ENTITY_PROP;
        } else if (typeName instanceof ParameterizedTypeName
                && ((ParameterizedTypeName) typeName).rawType.equals(CommonTypes.LIST)) {
            return PropertyType.EMBEDDED_LIST_PROP;
        } else if (isCategory(processingEnvironment, typeMirror)) {
            return PropertyType.SOFT_ENUM_PROP;
        } else if (optionalAttribute.flatMap(e -> Optional.ofNullable(e.getAnnotation(Mapped.class))).map(m -> m.type() == Type.DENSE).orElse(false)) {
            return PropertyType.EMBEDDED_DENSE_PROP;
        } else {
            return PropertyType.EMBEDDED_PROP;
        }
    }

    private static boolean isCategory(ProcessingEnvironment processingEnvironment, TypeMirror typeMirror) {
        TypeElement typeElement = processingEnvironment.getElementUtils().getTypeElement(Category.class.getCanonicalName());
        return processingEnvironment.getTypeUtils().isAssignable(typeMirror, typeElement.asType());
    }

    public boolean isAttachedToAttribute() {
        return attribute != null;
    }

    public boolean isUsingDaos() {
        return getDaoType() != null;
    }

    public boolean isListBased() {
        return type == PropertyType.EMBEDDED_LIST_PROP || type == PropertyType.ENTITY_LIST_PROP;
    }

    public boolean isUsingReferences() {
        return getReferenceType() != null;
    }

    public ClassName getDaoType() {
        if (type == PropertyType.EMBEDDED_LIST_PROP || type == PropertyType.EMBEDDED_PROP || type == PropertyType.EMBEDDED_DENSE_PROP) {
            return ClassName.get(unwrappedTypeName.packageName(), unwrappedTypeName.simpleName() + "Dao");
        } else {
            return null;
        }
    }

    public ClassName getReferenceType() {
        if (type == PropertyType.ENTITY_LIST_PROP) {
            return CommonTypes.ARRAY_REFERENCE;
        } else if (type == PropertyType.ENTITY_PROP) {
            return CommonTypes.SINGLE_REFERENCE;
        } else {
            return null;
        }
    }

    public ClassName getJoinType() {
        Mapped mappedAnnotation = attribute.getAnnotation(Mapped.class);
        MappedCollection collectionAnnotation = attribute.getAnnotation(MappedCollection.class);
        if (mappedAnnotation != null && mappedAnnotation.type() == Type.DENSE) {
            ReverseReference reverseReference = Optional.ofNullable(attribute.getAnnotation(Mapped.class)).map(Mapped::reverse).orElse(ReverseReference.NO_REVERSE_ATTRIBUTE);
            switch (reverseReference) {
                case NO_REVERSE_ATTRIBUTE:
                    return CommonTypes.SINGLE_JOIN;
                case WITH_REVERSE_ATTRIBUTE:
                    return CommonTypes.SINGLE_JOIN_REVERSE;
                case ONLY_REVERSE_ATTRIBUTE:
                    return CommonTypes.SINGLE_JOIN_REVERSE_ONLY;
                default:
                    throw new IllegalStateException("Unsupported reverseReference type " + reverseReference);
            }
        } else if (type != PropertyType.EMBEDDED_LIST_PROP) {
            return null;
        } else if (collectionAnnotation == null) {
            return CommonTypes.RANGED_JOIN;
        } else {
            return collectionAnnotation.collectionType() == CollectionType.RANGE ? CommonTypes.RANGED_JOIN : CommonTypes.ARRAY_JOIN;
        }
    }

    public boolean isUsingJoin() {
        return getJoinType() != null;
    }
}

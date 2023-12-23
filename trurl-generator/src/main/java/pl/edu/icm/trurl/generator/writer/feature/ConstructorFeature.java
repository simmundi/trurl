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
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.dao.annotation.EnumManagedBy;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstructorFeature implements Feature {
    private final BeanMetadata beanMetadata;
    private final List<ComponentProperty> componentProperties;

    public ConstructorFeature(BeanMetadata beanMetadata, Types typeUtilities) {
        this.beanMetadata = beanMetadata;
        this.componentProperties = beanMetadata.getComponentProperties();
    }

    @Override
    public Stream<FieldSpec> fields() {
        Stream<FieldSpec> daoFields = usesDaos()
                ? Stream.of(FieldSpec.builder(CommonTypes.DAOS, "daos", Modifier.FINAL).build())
                : Stream.empty();
        Stream<FieldSpec> categoryManagerFields = getCategoryProperties()
                .map(property -> FieldSpec.builder(
                                ParameterizedTypeName.get(CommonTypes.CATEGORY_MANAGER, property.unwrappedTypeName), property.name + "Manager")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build());
        Stream<FieldSpec> daoPrefix = Stream.of(FieldSpec.builder(CommonTypes.LANG_STRING, "daoPrefix").build());
        return Stream.of(
                categoryManagerFields,
                daoFields,
                daoPrefix).flatMap(s -> s);
    }

    private Stream<ComponentProperty> getCategoryProperties() {
        return beanMetadata.getComponentProperties().stream()
                .filter(property -> property.type == PropertyType.SOFT_ENUM_PROP);
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(constructor(beanMetadata));
    }

    private MethodSpec constructor(BeanMetadata beanMetadata) {
        List<ComponentProperty> properties = getCategoryProperties().collect(Collectors.toList());

        AnnotationSpec withFactory = AnnotationSpec.builder(WithFactory.class)
                .addMember("exactName", "$S", "DaoOf" + beanMetadata.componentName.simpleName() + "Factory").build();

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(withFactory);

        if (usesDaos()) {
            constructorBuilder.addParameter(CommonTypes.DAOS, "daos");
            constructorBuilder.addStatement("this.daos = daos");
        }

        constructorBuilder.addParameter(CommonTypes.LANG_STRING, "daoPrefix");

        for (ComponentProperty property : properties) {
            EnumManagedBy managedBy = property.attribute.getAnnotation(EnumManagedBy.class);
            TypeName param = managedBy == null
                    ? ParameterizedTypeName.get(CommonTypes.CATEGORY_MANAGER, property.unwrappedTypeName)
                    : getTypeName(managedBy);
            String name = property.name + "Manager";
            constructorBuilder.addParameter(param, name);
            constructorBuilder.addStatement("this.$L = $L", name, name);
        }
        constructorBuilder.addStatement("this.daoPrefix = $S.equals(daoPrefix) ? $S : daoPrefix + $S", "", "", ".");
        return constructorBuilder.build();
    }

    private boolean usesDaos() {
        return componentProperties.stream().anyMatch(p -> p.isUsingMappers());
    }

    private TypeName getTypeName(EnumManagedBy managedBy) {
        try {
            return TypeName.get(managedBy.value());
        } catch (MirroredTypeException mirroredTypeException) {
            return ClassName.get(mirroredTypeException.getTypeMirror());
        }
    }
}

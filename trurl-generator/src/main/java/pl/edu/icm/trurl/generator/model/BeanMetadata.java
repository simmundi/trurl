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

import com.squareup.javapoet.ClassName;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class BeanMetadata {
    public final TypeElement componentClass;
    public final String namespace;
    public final ClassName componentName;

    public final Set<ComponentFeature> componentFeatures;

    private final ProcessingEnvironment processingEnvironment;

    public BeanMetadata(ProcessingEnvironment processingEnvironment,
                        TypeElement componentClass,
                        String namespace,
                        Set<ComponentFeature> componentFeatures) {
        this.processingEnvironment = processingEnvironment;
        this.componentClass = componentClass;
        this.namespace = namespace;
        this.componentName = ClassName.get(componentClass);
        this.componentFeatures = componentFeatures;
    }

    public List<ComponentProperty> getComponentProperties() {
        List<ComponentProperty> properties = new ArrayList<>();

        for (Element enclosedElement : componentClass.getEnclosedElements()) {
            if (enclosedElement instanceof ExecutableElement) {
                ExecutableElement method = (ExecutableElement) enclosedElement;

                if (ComponentProperty.isGetter(method)) {
                    ComponentProperty property = ComponentProperty.create(processingEnvironment, new BeanPropertyMetadata(
                            method,
                            componentClass.getAnnotation(WithDao.class).namespace()
                    ));
                    if (property.isAttachedToAttribute()) {
                        properties.add(property);
                    }
                }
            }
        }
        return properties
                .stream()
                .collect(Collectors.toList());
    }
}

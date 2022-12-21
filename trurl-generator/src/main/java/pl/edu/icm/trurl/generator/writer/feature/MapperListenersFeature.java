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

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.CommonTypes;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

public class MapperListenersFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public MapperListenersFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.of(mapperListenersField());
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideGetMapperListeners(beanMetadata));
    }

    private FieldSpec mapperListenersField() {
        return FieldSpec.builder(CommonTypes.MAPPER_LISTENERS, "mapperListeners")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", CommonTypes.MAPPER_LISTENERS)
                .build();
    }

    private MethodSpec overrideGetMapperListeners(BeanMetadata beanMetadata) {
        return MethodSpec
                .methodBuilder("getMapperListeners")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("return mapperListeners")
                .returns(CommonTypes.MAPPER_LISTENERS)
                .build();
    }
}

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
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentFeature;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;

public class FireEventFeature implements Feature {

    private final BeanMetadata beanMetadata;

    public FireEventFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return of();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return of(overrideLifecycleEvent());
    }

    private MethodSpec overrideLifecycleEvent() {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("fireEvent")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CommonTypes.LIFECYCLE_EVENT, "event");

        if (beanMetadata.componentFeatures.contains(ComponentFeature.CAN_RESOLVE_CONFLICTS)) {
            methodSpec.addCode(CodeBlock.builder()
                    .beginControlFlow("switch (event)")
                    .addStatement("case PRE_PARALLEL_ITERATION: this.owners = new $T(this.store.getEnsuredCapacity()); this.parallelMode = true; break", CommonTypes.ATOMIC_INTEGER_ARRAY)
                    .addStatement("case POST_PARALLEL_ITERATION: this.parallelMode = false; break")
                    .endControlFlow()
                    .build());
        }

        return methodSpec.build();
    }
}

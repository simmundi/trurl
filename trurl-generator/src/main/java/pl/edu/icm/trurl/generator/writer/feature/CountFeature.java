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
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;

public class CountFeature implements Feature {

    @Override
    public Stream<FieldSpec> fields() {
        return of(FieldSpec.builder(
                        AtomicInteger.class,
                        "count",
                        Modifier.PRIVATE)
                .initializer("new $T()", AtomicInteger.class)
                .build());
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(
                overrideGetCount(),
                buildGetAndUpdateCount(),
                overrideSetCount());
    }

    private MethodSpec overrideSetCount() {
        return MethodSpec.methodBuilder("setCount")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "count")
                .addStatement("this.count.set(count)")
                .addStatement("ensureCapacity(count)")
                .build();
    }

    private MethodSpec overrideGetCount() {
        return MethodSpec.methodBuilder("getCount")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addStatement("return this.count.get()")
                .build();

    }

    private MethodSpec buildGetAndUpdateCount() {
        return MethodSpec.methodBuilder("getAndUpdateCount")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "delta")
                .returns(TypeName.INT)
                .addStatement("int newCount = this.count.getAndAdd(delta)")
                .addStatement("ensureCapacity(newCount + delta)")
                .addStatement("return newCount")
                .build();
    }
}

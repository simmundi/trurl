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

package pl.edu.icm.trurl.generator;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.generator.writer.MapperWriter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MapperGenerator extends AbstractProcessor {
    private ProcessingEnvironment processingEnvironment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment = processingEnvironment;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return doProcess(roundEnv);
        } catch (Exception e) {
            processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    Arrays.stream(e.getStackTrace()).map(Objects::toString).collect(Collectors.joining(" \n")));
            throw e;
        }
    }

    private boolean doProcess(RoundEnvironment roundEnv) {

        try {
            BeanFinder beanFinder = new BeanFinder();
            MapperWriter mapperWriter = new MapperWriter();
            SyntheticPropertiesSynthesizer syntheticPropertiesSynthesizer = new SyntheticPropertiesSynthesizer();

            beanFinder
                    .findBeans(processingEnvironment, roundEnv, syntheticPropertiesSynthesizer)
                    .forEach(beanMetadata -> mapperWriter.writeMapper(processingEnvironment, beanMetadata));


        } catch (Exception e) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage() == null ? "(null)" : e.getMessage());
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, stackTraceElement.toString());
            }
        }
        return true;

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(WithMapper.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}

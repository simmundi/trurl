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

package pl.edu.icm.trurl.generator.writer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.writer.feature.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DaoWriter {

    public void writeDao(ProcessingEnvironment processingEnvironment, BeanMetadata beanMetadata) {
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.OTHER, "starting: " + beanMetadata.componentName);
        ParameterizedTypeName superInterface = ParameterizedTypeName.get(CommonTypes.DAO, beanMetadata.componentName);
        ClassName daoName = daoNameFor(beanMetadata.componentName);

        TypeSpec.Builder dao =
                TypeSpec.classBuilder(daoName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(superInterface);

        List<Feature> features = Arrays.asList(
                new CommonFieldsFeature(beanMetadata),
                new ConstructorFeature(beanMetadata, processingEnvironment.getTypeUtils()),
                new AttachStoreFeature(beanMetadata),
                new ConfigureStoreFeature(beanMetadata),
                new CreateFeature(beanMetadata),
                new IsPresentFeature(beanMetadata),
                new LoadFeature(beanMetadata),
                new SaveFeature(beanMetadata),
                new GetAttributesFeature(beanMetadata),
                new ColumnarAccessFeature(beanMetadata),
                new EraseFeature(beanMetadata),
                new GetChildDaosFeature(beanMetadata),
                new MapEntitiesFeature(beanMetadata),
                new FireEventFeature(beanMetadata));

        features.stream()
                .flatMap(Feature::fields)
                .forEach(field -> dao.addField(field));

        features.stream()
                .flatMap(Feature::methods)
                .forEach(method -> dao.addMethod(method));

        try {
            String packageName = ClassName.get(beanMetadata.componentClass).packageName();
            JavaFile.builder(packageName, dao.build()).build().writeTo(processingEnvironment.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClassName daoNameFor(ClassName type) {
        return ClassName.get(type.packageName(), type.simpleName() + "Dao");
    }
}

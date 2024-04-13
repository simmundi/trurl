/*
 * Copyright (c) 2024 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import pl.edu.icm.trurl.ecs.dao.annotation.ReverseReference;
import pl.edu.icm.trurl.ecs.dao.annotation.RootDao;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;

import javax.lang.model.element.Modifier;
import java.util.Locale;
import java.util.stream.Stream;

public class DenseDaoFeature implements Feature {
    private final BeanMetadata beanMetadata;
    private final ClassName daoName;
    private final ReverseReference reverseReference;
    private final String name;

    public DenseDaoFeature(BeanMetadata beanMetadata, ClassName daoName) {
        this.beanMetadata = beanMetadata;
        this.daoName = daoName;
        RootDao rootDaoConfig = beanMetadata.componentClass.getAnnotation(RootDao.class);
        this.reverseReference = rootDaoConfig == null ? ReverseReference.NO_REVERSE_ATTRIBUTE : rootDaoConfig.reverseReference();
        this.name = rootDaoConfig == null || rootDaoConfig.name().equals("") ? beanMetadata.componentName.simpleName().toLowerCase(Locale.ROOT) : rootDaoConfig.name();
    }

    private ClassName classFor(ReverseReference reverseReference) {
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
    }

    private String methodNameFor(ReverseReference reverseReference) {
        switch (reverseReference) {
            case NO_REVERSE_ATTRIBUTE:
                return "singleTyped";
            case WITH_REVERSE_ATTRIBUTE:
                return "singleTypedWithReverse";
            case ONLY_REVERSE_ATTRIBUTE:
                return "singleTypedWithReverseOnly";
            default:
                throw new IllegalStateException("Unsupported reverseReference type " + reverseReference);
        }
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.of(
                FieldSpec.builder(classFor(reverseReference), "join")
                        .addModifiers(Modifier.PRIVATE)
                        .build(),
                FieldSpec.builder(String.class, "name")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .initializer("$S", name)
                        .build(),
                FieldSpec.builder(daoName, "dao")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build()
        );
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(
                constructor(),
                attachStore(),
                configureStore(),
                create(),
                isPresent(),
                erase(),
                load(),
                save2(),
                save3(),
                createAndLoad(),
                createAndLoad2(),
                getChildDaos(),
                fireEvent(),
                getAttributes(),
                stubEntities(),
                joinedRow()
        );
    }

    private MethodSpec stubEntities() {
        return MethodSpec.methodBuilder("stubEntities")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(beanMetadata.componentName, "component")
                .addStatement("dao.stubEntities(component)")
                .build();
    }

    private MethodSpec constructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(daoName, "dao")
                .addParameter(String.class, "name")
                .addStatement("this.dao = dao")
                .build();
    }

    // name
    private MethodSpec name() {
        return MethodSpec.methodBuilder("name")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(String.class)
                .addStatement("return dao.name()")
                .build();
    }

    private MethodSpec fireEvent() {
        return MethodSpec.methodBuilder("fireEvent")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(CommonTypes.LIFECYCLE_EVENT, "event")
                .addStatement("dao.fireEvent(event)")
                .build();
    }

    private MethodSpec getChildDaos() {
        return MethodSpec.methodBuilder("getChildDaos")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(CommonTypes.DAO_LIST)
                .addStatement("return dao.getChildDaos()")
                .build();
    }

    private MethodSpec createAndLoad() {
        return MethodSpec.methodBuilder("createAndLoad")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(int.class, "row")
                .returns(beanMetadata.componentName)
                .addStatement("return dao.createAndLoad(joinedRow(row))")
                .build();
    }

    private MethodSpec createAndLoad2() {
        return MethodSpec.methodBuilder("createAndLoad")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(CommonTypes.SESSION, "session")
                .addParameter(int.class, "row")
                .returns(beanMetadata.componentName)
                .addStatement("return dao.createAndLoad(session, joinedRow(row))")
                .build();
    }
    private static MethodSpec joinedRow() {
        return MethodSpec.methodBuilder("joinedRow")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(int.class, "row")
                .returns(int.class)
                .addStatement("return join.getRow(row, 0)")
                .build();
    }

    private static MethodSpec getAttributes() {
        return MethodSpec.methodBuilder("getAttributes")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(CommonTypes.ATTRIBUTE_LIST)
                .addStatement("return dao.getAttributes()")
                .build();
    }

    private MethodSpec save3() {
        return MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(CommonTypes.SESSION, "owner")
                .addParameter(beanMetadata.componentName, "component")
                .addParameter(int.class, "row")
                .addStatement("dao.save(owner, component, joinedRow(row))")
                .build();
    }

    private MethodSpec save2() {
        return MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(beanMetadata.componentName, "component")
                .addParameter(int.class, "row")
                .addStatement("dao.save(component, joinedRow(row))")
                .build();
    }

    private MethodSpec load() {
        return MethodSpec.methodBuilder("load")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(CommonTypes.SESSION, "session")
                .addParameter(beanMetadata.componentName, "component")
                .addParameter(int.class, "row")
                .addStatement("dao.load(session, component, joinedRow(row))")
                .build();
    }

    private static MethodSpec erase() {
        return MethodSpec.methodBuilder("erase")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(int.class, "row")
                .addStatement("dao.erase(joinedRow(row))")
                .build();
    }

    private static MethodSpec isPresent() {
        return MethodSpec.methodBuilder("isPresent")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(int.class, "row")
                .returns(boolean.class)
                .addStatement("return dao.isPresent(joinedRow(row))")
                .build();
    }

    private MethodSpec create() {
        return MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(beanMetadata.componentName)
                .addStatement("return dao.create()")
                .build();
    }

    private MethodSpec configureStore() {
        return MethodSpec.methodBuilder("configureStore")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(CommonTypes.COMPONENT_STORE_METADATA, "metadata")
                .addStatement("$T name = dao.name()", String.class)
                .addStatement("$T targetStore = metadata.addJoin(name).$L()",
                        CommonTypes.STORE,
                        methodNameFor(reverseReference))
                .addStatement("dao.configureStore(targetStore)")
                .build();
    }

    private static MethodSpec attachStore() {
        return MethodSpec.methodBuilder("attachStore")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(CommonTypes.STORE, "store")
                .addStatement("join = store.getJoin(dao.name())")
                .addStatement("dao.attachStore(join.getTarget())")
                .build();
    }
}

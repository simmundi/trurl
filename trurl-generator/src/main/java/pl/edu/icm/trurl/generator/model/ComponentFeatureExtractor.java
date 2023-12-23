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

import pl.edu.icm.trurl.ecs.dao.feature.CanBeNormalized;
import pl.edu.icm.trurl.ecs.dao.feature.CanResolveConflicts;
import pl.edu.icm.trurl.ecs.dao.feature.IsDirtyMarked;
import pl.edu.icm.trurl.ecs.dao.feature.RequiresOriginalCopy;
import pl.edu.icm.trurl.ecs.dao.feature.RequiresSetup;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.EnumSet;
import java.util.Set;

public class ComponentFeatureExtractor {
    private final ProcessingEnvironment processingEnvironment;
    private final TypeMirror isDirtyMarkedInterface;
    private final TypeMirror requiresOriginalCopyInterface;
    private final TypeMirror canResolveConflictsInterface;
    private final TypeMirror canBeNormalized;
    private final TypeMirror requiresSetupInterface;
    private final Types types;
    private final Elements elements;

    public ComponentFeatureExtractor(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        types = processingEnvironment.getTypeUtils();
        elements = processingEnvironment.getElementUtils();
        isDirtyMarkedInterface = elements.getTypeElement(IsDirtyMarked.class.getCanonicalName()).asType();
        requiresOriginalCopyInterface = elements.getTypeElement(RequiresOriginalCopy.class.getCanonicalName()).asType();
        canResolveConflictsInterface = elements.getTypeElement(CanResolveConflicts.class.getCanonicalName()).asType();
        requiresSetupInterface = elements.getTypeElement(RequiresSetup.class.getCanonicalName()).asType();
        canBeNormalized = elements.getTypeElement(CanBeNormalized.class.getCanonicalName()).asType();
    }

    public Set<ComponentFeature> extractFeatures(TypeElement typeElement) {
        TypeMirror type = typeElement.asType();
        EnumSet<ComponentFeature> results = EnumSet.noneOf(ComponentFeature.class);
        if (types.isAssignable(type, isDirtyMarkedInterface)) {
            results.add(ComponentFeature.IS_DIRTY_MARKED);
        }
        if (types.isSubtype(types.erasure(type), types.erasure(requiresOriginalCopyInterface))) {
            results.add(ComponentFeature.REQUIRES_ORIGINAL_COPY);
        }
        if (types.isSubtype(types.erasure(type), types.erasure(canResolveConflictsInterface))) {
            results.add(ComponentFeature.CAN_RESOLVE_CONFLICTS);
        }
        if (types.isSubtype(types.erasure(type), types.erasure(canBeNormalized))) {
            results.add(ComponentFeature.CAN_BE_NORMALIZED);
        }
        if (types.isAssignable(type, types.erasure(requiresSetupInterface))) {
            results.add(ComponentFeature.REQUIRES_SETUP);
        }
        return results;
    }
}

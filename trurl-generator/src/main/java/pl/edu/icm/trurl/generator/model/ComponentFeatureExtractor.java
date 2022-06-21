package pl.edu.icm.trurl.generator.model;

import pl.edu.icm.trurl.ecs.mapper.feature.CanBeNormalized;
import pl.edu.icm.trurl.ecs.mapper.feature.CanResolveConflicts;
import pl.edu.icm.trurl.ecs.mapper.feature.IsDirtyMarked;
import pl.edu.icm.trurl.ecs.mapper.feature.RequiresOriginalCopy;

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
    private final Types types;
    private final Elements elements;

    public ComponentFeatureExtractor(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        types = processingEnvironment.getTypeUtils();
        elements = processingEnvironment.getElementUtils();
        isDirtyMarkedInterface = elements.getTypeElement(IsDirtyMarked.class.getCanonicalName()).asType();
        requiresOriginalCopyInterface = elements.getTypeElement(RequiresOriginalCopy.class.getCanonicalName()).asType();
        canResolveConflictsInterface = elements.getTypeElement(CanResolveConflicts.class.getCanonicalName()).asType();
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
        return results;
    }
}

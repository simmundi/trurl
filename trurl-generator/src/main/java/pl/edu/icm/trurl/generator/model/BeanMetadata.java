package pl.edu.icm.trurl.generator.model;

import com.squareup.javapoet.ClassName;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.generator.SyntheticPropertiesSynthesizer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class BeanMetadata {
    public final TypeElement componentClass;
    public final String namespace;
    public final ClassName componentName;

    public final Set<ComponentFeature> componentFeatures;

    private final ProcessingEnvironment processingEnvironment;
    private final SyntheticPropertiesSynthesizer synthesizer;

    public BeanMetadata(ProcessingEnvironment processingEnvironment,
                        TypeElement componentClass,
                        String namespace,
                        Set<ComponentFeature> componentFeatures,
                        SyntheticPropertiesSynthesizer syntheticPropertiesSynthesizer) {
        this.processingEnvironment = processingEnvironment;
        this.componentClass = componentClass;
        this.namespace = namespace;
        this.componentName = ClassName.get(componentClass);
        this.componentFeatures = componentFeatures;
        this.synthesizer = syntheticPropertiesSynthesizer;
    }

    public List<ComponentProperty> getComponentProperties() {
        List<ComponentProperty> properties = new ArrayList<>();

        for (Element enclosedElement : componentClass.getEnclosedElements()) {
            if (enclosedElement instanceof ExecutableElement) {
                ExecutableElement method = (ExecutableElement) enclosedElement;

                if (ComponentProperty.isGetter(method)) {
                    ComponentProperty property = ComponentProperty.create(processingEnvironment, new BeanPropertyMetadata(
                            method,
                            componentClass.getAnnotation(WithMapper.class).namespace()
                    ));
                    if (property.isAttachedToAttribute()) {
                        properties.add(property);
                    }
                }
            }
        }
        return properties
                .stream()
                .flatMap(synthesizer::synthesize)
                .collect(Collectors.toList());
    }
}

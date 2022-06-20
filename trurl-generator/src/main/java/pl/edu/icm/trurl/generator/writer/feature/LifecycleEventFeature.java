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

public class LifecycleEventFeature implements Feature {

    private final BeanMetadata beanMetadata;

    public LifecycleEventFeature(BeanMetadata beanMetadata) {
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
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("lifecycleEvent")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CommonTypes.LIFECYCLE_EVENT, "event");

        if (beanMetadata.componentFeatures.contains(ComponentFeature.CAN_RESOLVE_CONFLICTS)) {
            methodSpec.addCode(CodeBlock.builder()
                    .beginControlFlow("switch (event)")
                    .addStatement("case PRE_PARALLEL_ITERATION: this.owners = new $T(this.count.get()); break", CommonTypes.ATOMIC_INTEGER_ARRAY)
                    .addStatement("case POST_PARALLEL_ITERATION: this.owners = null; break")
                    .endControlFlow()
                    .build());
        }

        return methodSpec.build();
    }
}

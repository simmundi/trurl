package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Stream;

public class EnsureCapacityFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public EnsureCapacityFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideEnsureCapacity(beanMetadata));
    }

    private MethodSpec overrideEnsureCapacity(BeanMetadata beanMetadata) {
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();
        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("ensureCapacity")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.INT, "capacity");

        for (ComponentProperty property : properties) {
            methodSpec.addStatement("this.$L.ensureCapacity(capacity)", property.name);
        }

        return methodSpec.build();
    }

}

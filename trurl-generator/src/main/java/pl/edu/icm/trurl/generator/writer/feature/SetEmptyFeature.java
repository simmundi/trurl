package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Stream;

public class SetEmptyFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public SetEmptyFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideAttachStore(beanMetadata));
    }

    private MethodSpec overrideAttachStore(BeanMetadata beanMetadata) {
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("setEmpty")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "row");
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();
        for (ComponentProperty property : properties) {
            if (property.synthetic) continue;

            switch (property.type) {
                default:
                    methodSpec.addStatement("$L.setEmpty(row)", property.name);
            }
        }

        return methodSpec.build();
    }
}

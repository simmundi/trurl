package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

public class EmptyRowFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public EmptyRowFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideAddEmptyRow(beanMetadata));
    }

    private MethodSpec overrideAddEmptyRow(BeanMetadata beanMetadata) {
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("_addEmptyRow")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class);
        for (ComponentProperty prop : beanMetadata.getComponentProperties()) {
            method.addStatement("$L.addEmpty()", prop.name);
        }
        return method.build();
    }

}

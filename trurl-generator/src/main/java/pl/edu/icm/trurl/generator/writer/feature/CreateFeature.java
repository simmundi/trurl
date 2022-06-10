package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import pl.edu.icm.trurl.generator.model.BeanMetadata;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

public class CreateFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public CreateFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideCreate(beanMetadata));
    }

    private MethodSpec overrideCreate(BeanMetadata beanMetadata) {
        ClassName component = beanMetadata.componentName;
        return MethodSpec
                .methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(component)
                .addStatement("return new $T()", component)
                .build();
    }
}

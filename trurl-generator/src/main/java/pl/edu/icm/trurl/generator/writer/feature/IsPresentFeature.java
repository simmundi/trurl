package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IsPresentFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public IsPresentFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideIsPresent(beanMetadata));
    }

    private MethodSpec overrideIsPresent(BeanMetadata beanMetadata) {
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();
        MethodSpec.Builder method = MethodSpec
                .methodBuilder("isPresent")
                .addParameter(TypeName.INT, "row")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.BOOLEAN);


        method.addStatement("return $L",
                properties.stream()
                        .filter(prop -> (prop.type != PropertyType.EMBEDDED_PROP && prop.type != PropertyType.EMBEDDED_LIST) || !prop.synthetic)
                        .map(prop ->
                                prop.type != PropertyType.EMBEDDED_PROP && prop.type != PropertyType.EMBEDDED_LIST ?
                                        "!" + prop.name + ".isEmpty(row)" : prop.name + ".isPresent(row)")
                        .collect(Collectors.joining(" || ")));

        return method.build();
    }

}

package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttributesFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public AttributesFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideAttributes(beanMetadata));
    }

    private MethodSpec overrideAttributes(BeanMetadata beanMetadata) {
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();
        return MethodSpec.methodBuilder("attributes")
                .returns(CommonTypes.ATTRIBUTE_LIST)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("$T result = new $T()", CommonTypes.ATTRIBUTE_LIST, CommonTypes.ATTRIBUTE_ARRAY_LIST)
                .addStatement("result.addAll($T.asList($L))", CommonTypes.ARRAYS, properties.stream()
                        .filter(p -> p.type != PropertyType.EMBEDDED_PROP && p.type != PropertyType.EMBEDDED_LIST)
                        .map(p -> p.name)
                        .collect(Collectors.joining(", ")))
                .addStatement("$T.<$T>asList($L).stream().forEach(mapper -> result.addAll(mapper.attributes()))", CommonTypes.ARRAYS, CommonTypes.MAPPER, properties.stream()
                        .filter(p -> p.type == PropertyType.EMBEDDED_PROP || p.type == PropertyType.EMBEDDED_LIST)
                        .map(p -> p.name)
                        .collect(Collectors.joining(", ")))
                .addStatement("return result")
                .build();
    }


}

package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import pl.edu.icm.trurl.generator.model.BeanMetadata;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

public class CommonFieldsFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public CommonFieldsFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return beanMetadata.getComponentProperties().stream()
                .map(property -> FieldSpec
                        .builder(property.type.columnType, property.name, Modifier.PRIVATE)
                        .build());
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.empty();
    }

}

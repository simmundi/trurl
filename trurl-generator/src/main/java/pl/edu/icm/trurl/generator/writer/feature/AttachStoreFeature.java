package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Stream;

public class AttachStoreFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public AttachStoreFeature(BeanMetadata beanMetadata) {
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
        List<ComponentProperty> properties = beanMetadata.getComponentProperties();

        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("attachStore")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(CommonTypes.STORE, "store");


        methodSpec.addStatement("this.store = store");

        for (ComponentProperty property : properties) {
            switch (property.type) {
                case EMBEDDED_PROP:
                    methodSpec.addStatement("$L.attachStore($T.wrap(store, $S))",
                            property.name, CommonTypes.PREFIXED_STORE, property.name);
                    break;
                case EMBEDDED_LIST:
                    methodSpec.addStatement("$L.attachStore($T.wrap(store, $S))",
                            property.name, CommonTypes.PREFIXED_STORE, property.name);
                    break;
                default:
                    methodSpec.addStatement("$L = ($T) store.get($S)",
                            property.name,
                            property.type.columnType,
                            property.qname);
            }
        }

        methodSpec.addStatement("this.setCount(store.getCount())");
        return methodSpec.build();
    }
}

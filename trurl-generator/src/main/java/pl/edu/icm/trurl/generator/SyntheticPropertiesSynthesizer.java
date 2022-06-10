package pl.edu.icm.trurl.generator;

import com.squareup.javapoet.TypeName;
import pl.edu.icm.trurl.generator.model.ComponentProperty;
import pl.edu.icm.trurl.generator.model.PropertyType;

import java.util.Optional;
import java.util.stream.Stream;

public class SyntheticPropertiesSynthesizer {

    public Stream<ComponentProperty> synthesize(ComponentProperty base) {
        switch (base.type) {
            case EMBEDDED_LIST:
                return embeddedList(base);
            default:
                return Stream.of(base);
        }
    }

    private Stream<ComponentProperty> embeddedList(ComponentProperty base) {
        return Stream.of(
                base,
                new ComponentProperty(
                        base.name + "_start",
                        base.namespace,
                        PropertyType.INT_PROP,
                        null,
                        null, TypeName.INT, true, Optional.empty()),
                new ComponentProperty(
                        base.name + "_length",
                        base.namespace,
                        PropertyType.BYTE_PROP,
                        null,
                        null, TypeName.INT, true, Optional.empty()));
    }
}
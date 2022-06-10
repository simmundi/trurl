package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.stream.Stream;

public class LoadFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public LoadFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideLoad(beanMetadata));
    }

    private MethodSpec overrideLoad(BeanMetadata beanMetadata) {
        ClassName component = beanMetadata.componentName;
        List<ComponentProperty> componentProperties = beanMetadata.getComponentProperties();
        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("load")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CommonTypes.SESSION, "session")
                .addParameter(component, "component")
                .addParameter(TypeName.INT, "row")
                .addAnnotation(Override.class);

        for (ComponentProperty property : componentProperties) {
            if (property.synthetic) continue;

            switch (property.type) {
                case INT_PROP:
                    methodSpec.addStatement("component.$L($L.getInt(row))", property.setterName, property.name);
                    break;
                case BOOLEAN_PROP:
                    methodSpec.addStatement("component.$L($L.getBoolean(row))", property.setterName, property.name);
                    break;
                case BYTE_PROP:
                    methodSpec.addStatement("component.$L($L.getByte(row))", property.setterName, property.name);
                    break;
                case DOUBLE_PROP:
                    methodSpec.addStatement("component.$L($L.getDouble(row))", property.setterName, property.name);
                    break;
                case ENUM_PROP:
                    methodSpec.addStatement("component.$L(($T)$L.getEnum(row))", property.setterName, property.businessType, property.name);
                    break;
                case FLOAT_PROP:
                    methodSpec.addStatement("component.$L($L.getFloat(row))", property.setterName, property.name);
                    break;
                case SHORT_PROP:
                    methodSpec.addStatement("component.$L($L.getShort(row))", property.setterName, property.name);
                    break;
                case STRING_PROP:
                    methodSpec.addStatement("component.$L($L.getString(row))", property.setterName, property.name);
                    break;
                case ENTITY_LIST_PROP:
                    methodSpec.addStatement("$L.loadIds(row, (idx, id) -> component.$L().add(session.getEntity(id)))", property.name, property.getterName);
                    break;
                case ENTITY_PROP:
                    methodSpec.addStatement("component.$L($L.getEntity(row, session))", property.setterName, property.name);
                    break;
                case EMBEDDED_PROP:
                    String instanceName = property.name + "Instance";
                    methodSpec.addCode(CodeBlock.builder()
                            .beginControlFlow("if ($L.isPresent(row))", property.name)
                            .addStatement("$T $L = ($T)$L.create()", property.businessType, instanceName, property.businessType, property.name)
                            .addStatement("$L.load(session, $L, row)", property.name, instanceName)
                            .addStatement("component.$L($L)", property.setterName, instanceName)
                            .endControlFlow()
                            .build());

                    break;
                case EMBEDDED_LIST:
                    createEmbeddedList(methodSpec, property);
                    break;
                default:
                    throw new IllegalStateException("Unknown entity type " + property.type);
            }
        }

        return methodSpec.build();
    }

    private void createEmbeddedList(MethodSpec.Builder methodSpec, ComponentProperty property) {
        {
            methodSpec
                    .addCode(
                            CodeBlock.builder()
                                    .beginControlFlow("if (!$L_start.isEmpty(row))", property.name)
                                    .addStatement("int length = $L_length.getByte(row)", property.name)
                                    .addStatement("int start = $L_start.getInt(row)", property.name)
                                    .beginControlFlow("for (int i = start; i < start + length; i++)")
                                    .addStatement("if (!$L.isPresent(i)) break", property.name)
                                    .addStatement("$T element = ($T) $L.create()", property.businessType, property.businessType, property.name)
                                    .addStatement("$L.load(session, element, i)", property.name)
                                    .addStatement("component.$L().add(element)", property.getterName)
                                    .endControlFlow()
                                    .endControlFlow()
                                    .build()
                    );
        }
    }
}

package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import pl.edu.icm.trurl.ecs.annotation.MappedCollection;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentProperty;

import javax.lang.model.element.Modifier;
import java.util.Optional;
import java.util.stream.Stream;

public class SaveFeature implements Feature {
    private final BeanMetadata beanMetadata;

    public SaveFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(overrideSave(beanMetadata));
    }

    private MethodSpec overrideSave(BeanMetadata beanMetadata) {
        ClassName component = beanMetadata.componentName;
        MethodSpec.Builder methodSpec = MethodSpec
                .methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CommonTypes.LANG_OBJECT, "component")
                .addParameter(TypeName.INT, "row")
                .addAnnotation(Override.class)
                .addStatement("$T source = ($T)component", component, component)
                .addStatement("int current = count.get()")
                .addStatement("if (row < current && !isModified(component, row)) return")
                .addCode(CodeBlock.builder()
                        .beginControlFlow("while (row >= current)")
                        .addStatement("boolean ok = count.compareAndSet(current, row + 1)")
                        .addStatement("current = count.get()")
                        .addStatement("if (!ok) continue")
                        .addStatement("ensureCapacity(current)")
                        .endControlFlow().build());

        methodSpec
                .addStatement("mapperListeners.fireSavingComponent(source, row)");

        for (ComponentProperty property : beanMetadata.getComponentProperties()) {
            if (property.synthetic) continue;
            switch (property.type) {
                case INT_PROP:
                    methodSpec.addStatement("$L.setInt(row, source.$L())", property.name, property.getterName);
                    break;
                case BOOLEAN_PROP:
                    methodSpec.addStatement("$L.setBoolean(row, source.$L())", property.name, property.getterName);
                    break;
                case BYTE_PROP:
                    methodSpec.addStatement("$L.setByte(row, source.$L())", property.name, property.getterName);
                    break;
                case DOUBLE_PROP:
                    methodSpec.addStatement("$L.setDouble(row, source.$L())", property.name, property.getterName);
                    break;
                case ENUM_PROP:
                    methodSpec.addStatement("$L.setEnum(row, source.$L())", property.name, property.getterName);
                    break;
                case FLOAT_PROP:
                    methodSpec.addStatement("$L.setFloat(row, source.$L())", property.name, property.getterName);
                    break;
                case SHORT_PROP:
                    methodSpec.addStatement("$L.setShort(row, source.$L())", property.name, property.getterName);
                    break;
                case STRING_PROP:
                    methodSpec.addStatement("$L.setString(row, source.$L())", property.name, property.getterName);
                    break;
                case ENTITY_LIST_PROP:
                    methodSpec.addStatement("$L.saveIds(row, source.$L().size(), (idx) -> source.$L().get(idx).getId())", property.name, property.getterName, property.getterName);
                    break;
                case ENTITY_PROP:
                    methodSpec.addStatement("$L.setEntity(row, source.$L())", property.name, property.getterName);
                    break;
                case EMBEDDED_PROP:
                    methodSpec.addStatement("if (source.$L() != null) $L.save(source.$L(), row)", property.getterName, property.name, property.getterName);
                    break;
                case EMBEDDED_LIST:
                    createEmbeddedList(methodSpec, property);
                    break;
                default:
                    throw new IllegalStateException("Unknown entity type " + property.type);
            }
        }

        return methodSpec
                .build();
    }

    private void createEmbeddedList(MethodSpec.Builder methodSpec, ComponentProperty property) {
        {
            Optional<MappedCollection> mappedCollection = Optional.ofNullable(property.attribute.getAnnotation(MappedCollection.class));
            int sizeMin = mappedCollection.map(MappedCollection::minReservation).orElse(1);
            int sizeMargin = mappedCollection.map(MappedCollection::margin).orElse(2);

            methodSpec
                    .addCode(
                            CodeBlock.builder()
                                    .beginControlFlow("")
                                    .addStatement("int size = source.$L().size()", property.getterName)
                                    .beginControlFlow("if (size > 127)")
                                    .addStatement("throw new IllegalStateException(\"Embedded lists over 127 elements are not supported\")")
                                    .endControlFlow()
                                    .beginControlFlow("if (size > 0 && $L_start.isEmpty(row))", property.name)
                                    .addStatement("byte sizeMin = $L", sizeMin)
                                    .addStatement("byte sizeMargin = $L", sizeMargin)
                                    .addStatement("byte length = (byte) (Math.max(size, sizeMin - sizeMargin ) + sizeMargin)")
                                    .addStatement("$L_start.setInt(row, $L.getCount())", property.name, property.name)
                                    .addStatement("$L_length.setByte(row, length)", property.name)
                                    .addStatement("$L.setCount($L.getCount() + length)", property.name, property.name)
                                    .endControlFlow()
                                    .beginControlFlow("if (size > 0)", property.name)
                                    .addStatement("int length = $L_length.getByte(row)", property.name)
                                    .addStatement("int start = $L_start.getInt(row)", property.name)
                                    .addStatement("int end = start + size")
                                    .beginControlFlow("if (size > length)")
                                    .addStatement("throw new IllegalStateException(\"resizing this list over \" + length + \" is not supported\")")
                                    .endControlFlow()
                                    .beginControlFlow("for (int i = 0; i < size; i++)")
                                    .addStatement("$L.save(source.$L().get(i), i + start)", property.name, property.getterName)
                                    .endControlFlow()
                                    .beginControlFlow("if (size < length)")
                                    .addStatement("$L.setEmpty(start + size)", property.name)
                                    .endControlFlow()
                                    .endControlFlow()
                                    .endControlFlow()
                                    .build()
                    );
        }
    }
}

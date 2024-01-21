package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.*;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.PropertyType;

import javax.lang.model.element.Modifier;
import java.util.stream.Stream;

public class MapEntitiesFeature implements Feature{
    private final BeanMetadata beanMetadata;

    public MapEntitiesFeature(BeanMetadata beanMetadata) {
        this.beanMetadata = beanMetadata;
    }

    @Override
    public Stream<FieldSpec> fields() {
        return Stream.empty();
    }

    @Override
    public Stream<MethodSpec> methods() {
        return Stream.of(mapEntitiesMethod(), stubEntitiesMethod());
    }

    private MethodSpec stubEntitiesMethod() {
        return MethodSpec.methodBuilder("stubEntities")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(beanMetadata.componentName, "component")
                .addStatement("mapEntities(component, e -> $T.stub(e.getId()))", CommonTypes.ENTITY)
                .build();
    }

    private MethodSpec mapEntitiesMethod() {
        return MethodSpec.methodBuilder("mapEntities")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(beanMetadata.componentName, "component")
                .addParameter(CommonTypes.ENTITY_MAPPER, "mapper")
                .addCode(mapDirectFields())
                .addCode(mapDirectCollections())
                .addCode(recurse())
                .build();
    }

    private CodeBlock mapDirectFields() {
        CodeBlock.Builder mapDirectFields = CodeBlock.builder();
        beanMetadata.getComponentProperties().stream().filter(p -> p.type == PropertyType.ENTITY_PROP).forEach(p -> {
            mapDirectFields.addStatement("component.$L(mapper.apply(component.$L()))", p.setterName, p.getterName);
        });
        return mapDirectFields.build();
    }

    private CodeBlock mapDirectCollections() {
        CodeBlock.Builder mapDirectCollections = CodeBlock.builder();
        beanMetadata.getComponentProperties().stream().filter(p -> p.type == PropertyType.ENTITY_LIST_PROP).forEach(p -> {
            mapDirectCollections
                    .addStatement("$T $L = component.$L().listIterator()", CommonTypes.ENTITY_LIST_ITERATOR, p.name, p.getterName)
                    .beginControlFlow("while ($L.hasNext())", p.name)
                    .addStatement("$L.set(mapper.apply($L.next()))", p.name, p.name)
                    .endControlFlow();
        });
        return mapDirectCollections.build();
    }

    private CodeBlock recurse() {
        CodeBlock.Builder recurse = CodeBlock.builder();
        beanMetadata.getComponentProperties().stream().filter(p -> p.isListBased() && p.isUsingDaos() ).forEach(p -> {
            recurse
                    .addStatement("$T $L = component.$L()", ParameterizedTypeName.get(CommonTypes.LIST, p.unwrappedTypeName), p.name, p.getterName)
                    .addStatement("int $LSize = $L.size()", p.name, p.name)
                    .beginControlFlow("for (int i = 0; i < $LSize; i++)", p.name)
                    .addStatement("$L.mapEntities($L.get(i), mapper)", p.fieldName, p.name)
                    .endControlFlow();
        });
        beanMetadata.getComponentProperties().stream().filter(p -> !p.isListBased() && p.isUsingDaos()).forEach(p -> {
            recurse
                    .addStatement("$T $L = component.$L()", p.unwrappedTypeName, p.name, p.getterName)
                    .beginControlFlow("if ($L != null)", p.name)
                    .addStatement("$L.mapEntities($L, mapper)", p.fieldName, p.name)
                    .endControlFlow()
                    ;
        });
        return recurse.build();
    }

}

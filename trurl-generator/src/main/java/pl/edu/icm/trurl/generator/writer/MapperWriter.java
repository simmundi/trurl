package pl.edu.icm.trurl.generator.writer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import pl.edu.icm.trurl.generator.CommonTypes;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.writer.feature.AttachStoreFeature;
import pl.edu.icm.trurl.generator.writer.feature.AttributesFeature;
import pl.edu.icm.trurl.generator.writer.feature.ColumnarAccessFeature;
import pl.edu.icm.trurl.generator.writer.feature.CommonFieldsFeature;
import pl.edu.icm.trurl.generator.writer.feature.ConfigureStoreFeature;
import pl.edu.icm.trurl.generator.writer.feature.CountFeature;
import pl.edu.icm.trurl.generator.writer.feature.CreateFeature;
import pl.edu.icm.trurl.generator.writer.feature.EnsureCapacityFeature;
import pl.edu.icm.trurl.generator.writer.feature.Feature;
import pl.edu.icm.trurl.generator.writer.feature.IsModifiedFeature;
import pl.edu.icm.trurl.generator.writer.feature.IsPresentFeature;
import pl.edu.icm.trurl.generator.writer.feature.LifecycleEventFeature;
import pl.edu.icm.trurl.generator.writer.feature.LoadFeature;
import pl.edu.icm.trurl.generator.writer.feature.MapperListenersFeature;
import pl.edu.icm.trurl.generator.writer.feature.SaveFeature;
import pl.edu.icm.trurl.generator.writer.feature.SetEmptyFeature;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MapperWriter {

    public void writeMapper(ProcessingEnvironment processingEnvironment, BeanMetadata beanMetadata) {
        ParameterizedTypeName superInterface = ParameterizedTypeName.get(CommonTypes.MAPPER, beanMetadata.componentName);
        ClassName mapperName = mapperNameFor(beanMetadata.componentName);

        TypeSpec.Builder mapper =
                TypeSpec.classBuilder(mapperName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(superInterface);

        List<Feature> features = Arrays.asList(
                new CommonFieldsFeature(beanMetadata),
                new CountFeature(),
                new SetEmptyFeature(beanMetadata),
                new IsModifiedFeature(beanMetadata),
                new AttachStoreFeature(beanMetadata),
                new ConfigureStoreFeature(beanMetadata),
                new CreateFeature(beanMetadata),
                new IsPresentFeature(beanMetadata),
                new LoadFeature(beanMetadata),
                new SaveFeature(beanMetadata),
                new EnsureCapacityFeature(beanMetadata),
                new AttributesFeature(beanMetadata),
                new MapperListenersFeature(beanMetadata),
                new ColumnarAccessFeature(beanMetadata),
                new LifecycleEventFeature(beanMetadata));

        features.stream()
                .flatMap(Feature::fields)
                .forEach(field -> mapper.addField(field));

        features.stream()
                .flatMap(Feature::methods)
                .forEach(method -> mapper.addMethod(method));

        try {
            String packageName = ClassName.get(beanMetadata.componentClass).packageName();
            JavaFile.builder(packageName, mapper.build()).build().writeTo(processingEnvironment.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClassName mapperNameFor(ClassName type) {
        return ClassName.get(type.packageName(), type.simpleName() + "Mapper");
    }
}

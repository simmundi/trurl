package pl.edu.icm.trurl.generator;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.generator.writer.MapperWriter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MapperGenerator extends AbstractProcessor {
    private ProcessingEnvironment processingEnvironment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment = processingEnvironment;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            return doProcess(roundEnv);
        } catch (Exception e) {
            processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    Arrays.stream(e.getStackTrace()).map(Objects::toString).collect(Collectors.joining(" \n")));
            throw e;
        }
    }

    private boolean doProcess(RoundEnvironment roundEnv) {

        BeanFinder beanFinder = new BeanFinder();
        MapperWriter mapperWriter = new MapperWriter();
        SyntheticPropertiesSynthesizer syntheticPropertiesSynthesizer = new SyntheticPropertiesSynthesizer();

        beanFinder
                .findBeans(processingEnvironment, roundEnv, syntheticPropertiesSynthesizer)
                .forEach(beanMetadata -> mapperWriter.writeMapper(processingEnvironment, beanMetadata));

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(WithMapper.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}

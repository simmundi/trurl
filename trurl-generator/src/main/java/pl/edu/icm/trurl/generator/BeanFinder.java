package pl.edu.icm.trurl.generator;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.generator.model.BeanMetadata;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

public class BeanFinder {

    public Stream<BeanMetadata> findBeans(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment, SyntheticPropertiesSynthesizer syntheticPropertiesSynthesizer) {
        return roundEnvironment
                .getElementsAnnotatedWith(WithMapper.class)
                .stream().map(element ->
                        new BeanMetadata(
                                processingEnvironment,
                                (TypeElement) element,
                                element.getAnnotation(WithMapper.class).namespace(),
                                syntheticPropertiesSynthesizer
                        ));
    }
}

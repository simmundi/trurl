package pl.edu.icm.trurl.generator;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.generator.model.BeanMetadata;
import pl.edu.icm.trurl.generator.model.ComponentFeatureExtractor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

public class BeanFinder {

    public Stream<BeanMetadata> findBeans(ProcessingEnvironment processingEnvironment, RoundEnvironment roundEnvironment, SyntheticPropertiesSynthesizer syntheticPropertiesSynthesizer) {
        ComponentFeatureExtractor featureExtractor = new ComponentFeatureExtractor(processingEnvironment);
        return roundEnvironment
                .getElementsAnnotatedWith(WithMapper.class)
                .stream()
                .map(element -> (TypeElement) element )
                .map(element ->
                        new BeanMetadata(
                                processingEnvironment,
                                element,
                                element.getAnnotation(WithMapper.class).namespace(),
                                featureExtractor.extractFeatures(element),
                                syntheticPropertiesSynthesizer
                        ));
    }
}

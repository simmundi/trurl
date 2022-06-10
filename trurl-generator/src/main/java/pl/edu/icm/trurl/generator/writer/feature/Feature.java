package pl.edu.icm.trurl.generator.writer.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import java.util.stream.Stream;

public interface Feature {
    Stream<FieldSpec> fields();
    Stream<MethodSpec> methods();
}

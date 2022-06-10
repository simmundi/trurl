package pl.edu.icm.trurl.generator.model;

import javax.lang.model.element.ExecutableElement;

final class BeanPropertyMetadata {
    public final ExecutableElement method;
    public final String namespace;

    public BeanPropertyMetadata(ExecutableElement method, String namespace) {
        this.method = method;
        this.namespace = namespace;
    }
}

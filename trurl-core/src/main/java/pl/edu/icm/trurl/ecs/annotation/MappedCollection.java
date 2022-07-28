package pl.edu.icm.trurl.ecs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface MappedCollection {
    CollectionType collectionType() default CollectionType.RANGE;
    int minReservation() default 1;
    int margin() default 2;
}

package cz.cuni.mff.spl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Main SPL annotation. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SPL {
    /** SPL formula. */
    String[] formula() default {};

    /** Method aliases. */
    String[] methods() default {};

    /** Generator aliases. */
    String[] generators() default {};
}

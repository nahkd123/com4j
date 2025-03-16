package io.github.nahkd123.com4j.conversion;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Any static field annotated with this annotation will be used as memory
 * layout.
 * </p>
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ForeignConvertibleLayout {
}

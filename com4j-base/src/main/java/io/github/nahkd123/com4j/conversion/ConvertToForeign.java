package io.github.nahkd123.com4j.conversion;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotate method with this annotation to indicate the method is for converting
 * from Java object to foreign value. This can be used on either static method
 * or instance method. If the method is static, the signature must either be
 * {@code (T)F} or {@code (T, Arena)F}. If the method is instance, the signature
 * must either be {@code ()F} or {@code (Arena)F} (T is the type of Java object
 * and F is the type of foreign value).
 * </p>
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ConvertToForeign {
}

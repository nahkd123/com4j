package io.github.nahkd123.com4j.conversion;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

/**
 * <p>
 * Annotate a class with this annotation to mark it as foreign-convertible.
 * Foreign-convertible means it can be converted into any type that can be
 * represented as {@link MemoryLayout}.
 * </p>
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ForeignConvertible {
	/**
	 * <p>
	 * The target type that the annotate class can be converted into. For struct,
	 * use {@link MemorySegment} class with a field of type {@link MemoryLayout}
	 * annotated with {@link ForeignConvertibleLayout}.
	 * </p>
	 */
	Class<?> value();
}

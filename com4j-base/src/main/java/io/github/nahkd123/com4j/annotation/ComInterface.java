package io.github.nahkd123.com4j.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface ComInterface {
	/**
	 * <p>
	 * The IID of COM interface that the target Java interface is implementing.
	 * </p>
	 */
	String value();
}

package io.github.nahkd123.com4j.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface ComMethod {
	/**
	 * <p>
	 * The index of this method in COM virtual table. This will be used to mirror
	 * from COM function pointer to Java method of COM to Java wrapper, or mirror
	 * from Java method to COM function pointer of Java to COM wrapper.
	 * </p>
	 */
	int index();
}

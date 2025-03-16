package io.github.nahkd123.com4j;

import java.util.HashMap;
import java.util.Map;

import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.win32.Guid;

public class ComUtils {
	private static final Map<Class<?>, Guid> iids = new HashMap<>();

	public static <T extends IUnknown> Guid iidOf(Class<T> interfaceType) {
		return iids.computeIfAbsent(interfaceType, clazz -> {
			ComInterface annotation = clazz.getDeclaredAnnotation(ComInterface.class);
			if (annotation == null) throw new IllegalArgumentException("%s does not have ComInterface annotation"
				.formatted(interfaceType));
			return Guid.of(annotation.value());
		});
	}

	@SuppressWarnings("unchecked")
	public static <T extends IUnknown> boolean isAssignableTo(Guid iid, Class<T> interfaceType) {
		if (!IUnknown.class.isAssignableFrom(interfaceType)) return false;
		if (iidOf(interfaceType).equals(iid)) return true;
		return isAssignableTo(iid, (Class<T>) interfaceType.getSuperclass());
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends IUnknown> findComInterface(Class<?> anyType) {
		if (!IUnknown.class.isAssignableFrom(anyType)) return null;
		if (anyType.isAnnotationPresent(ComInterface.class)) return (Class<? extends IUnknown>) anyType;
		return findComInterface(anyType.getSuperclass());
	}
}

package io.github.nahkd123.com4j.impl.generator;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MethodDescriptor(Type ret, List<Type> params) {
	public static MethodDescriptor of(Type ret, Type... params) {
		return new MethodDescriptor(ret, List.of(params));
	}

	public static MethodDescriptor ofVoid(Type... params) {
		return new MethodDescriptor(Type.of(void.class), List.of(params));
	}

	public static MethodDescriptor deriveFrom(Method method) {
		return new MethodDescriptor(Type.of(method.getReturnType()), Stream
			.of(method.getParameterTypes())
			.map(Type::of)
			.toList());
	}

	public String descriptorString() {
		return "(%s)%s".formatted(
			params.stream()
				.map(Type::descriptor)
				.collect(Collectors.joining("")),
			ret.descriptor());
	}

	@Override
	public final String toString() {
		return descriptorString();
	}
}

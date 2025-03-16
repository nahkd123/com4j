package io.github.nahkd123.com4j.impl.generator;

import java.lang.reflect.Constructor;

import org.objectweb.asm.MethodVisitor;

public interface Method {
	Type owner();

	String name();

	MethodDescriptor descriptor();

	default void accept(MethodVisitor visitor, int opcode) {
		visitor.visitMethodInsn(opcode,
			owner().internal(),
			name(),
			descriptor().descriptorString(),
			owner().isInterface());
	}

	static OfExisting of(java.lang.reflect.Method reflected) {
		Type owner = Type.of(reflected.getDeclaringClass());
		String name = reflected.getName();
		MethodDescriptor descriptor = MethodDescriptor.deriveFrom(reflected);
		return new OfExisting(owner, name, descriptor);
	}

	static OfExisting of(Constructor<?> reflected) {
		Type owner = Type.of(reflected.getDeclaringClass());
		Class<?>[] classes = reflected.getParameterTypes();
		Type[] params = new Type[classes.length];
		for (int i = 0; i < params.length; i++) params[i] = Type.of(classes[i]);
		return new OfExisting(owner, "<init>", MethodDescriptor.ofVoid(params));
	}

	public record OfExisting(Type owner, String name, MethodDescriptor descriptor) implements Method {
	}
}

package io.github.nahkd123.com4j.impl.generator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public interface Var {
	void acceptGet(MethodVisitor visitor);

	void acceptSet(MethodVisitor visitor);

	static OfField ofInstance(Type owner, String name, Type value) {
		return new OfField(Opcodes.GETFIELD, Opcodes.PUTFIELD, owner, name, value);
	}

	static OfField ofStatic(Type owner, String name, Type value) {
		return new OfField(Opcodes.GETSTATIC, Opcodes.PUTSTATIC, owner, name, value);
	}

	static OfLocal ofLocal(Type localType, int ordinal) {
		return new OfLocal(localType, ordinal);
	}

	static OfField of(Field reflected) {
		Type owner = Type.of(reflected.getDeclaringClass());
		String name = reflected.getName();
		Type value = Type.of(reflected.getType());
		return Modifier.isStatic(reflected.getModifiers())
			? ofStatic(owner, name, value)
			: ofInstance(owner, name, value);
	}

	record OfField(int opGet, int opSet, Type owner, String name, Type value) implements Var {
		@Override
		public void acceptGet(MethodVisitor visitor) {
			visitor.visitFieldInsn(opGet, owner.internal(), name, value.descriptor());
		}

		@Override
		public void acceptSet(MethodVisitor visitor) {
			visitor.visitFieldInsn(opSet, owner.internal(), name, value.descriptor());
		}

		@Override
		public final String toString() {
			return "%s.%s: %s".formatted(owner, name, value);
		}
	}

	record OfLocal(Type type, int ordinal) implements Var {
		@Override
		public void acceptGet(MethodVisitor visitor) {
			type.acceptGetLocal(visitor, ordinal);
		}

		@Override
		public void acceptSet(MethodVisitor visitor) {
			type.acceptSetLocal(visitor, ordinal);
		}
	}
}

package io.github.nahkd123.com4j.impl.generator;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public interface Type {
	static Type BYTE = Type.of(byte.class);
	static Type SHORT = Type.of(short.class);
	static Type INT = Type.of(int.class);
	static Type LONG = Type.of(long.class);
	static Type FLOAT = Type.of(float.class);
	static Type DOUBLE = Type.of(double.class);
	static Type BOOL = Type.of(boolean.class);
	static Type CHAR = Type.of(char.class);
	static Type STRING = Type.of(String.class);

	String descriptor();

	String internal();

	String name();

	boolean isInterface();

	void acceptGetLocal(MethodVisitor visitor, int ordinal);

	void acceptSetLocal(MethodVisitor visitor, int ordinal);

	default Var.OfField field(String name, Type type) {
		return Var.ofInstance(this, name, type);
	}

	default Var.OfField staticField(String name, Type type) {
		return Var.ofStatic(this, name, type);
	}

	default Method method(String name, MethodDescriptor descriptor) {
		return new Method.OfExisting(this, name, descriptor);
	}

	default void acceptNew(MethodVisitor visitor) {
		visitor.visitTypeInsn(Opcodes.NEW, internal());
	}

	void acceptNewArray(MethodVisitor visitor);

	void acceptReturn(MethodVisitor visitor);

	default Var var(int ordinal) {
		return Var.ofLocal(this, ordinal);
	}

	static Type of(Class<?> clazz) {
		return new OfClass(clazz);
	}

	record OfClass(Class<?> clazz) implements Type {
		private static final Map<Class<?>, int[]> OPCODES = Map.of(
			void.class, new int[] { -1, -1, -1, Opcodes.RETURN },
			byte.class, new int[] { Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.T_BYTE, -1 },
			short.class, new int[] { Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.T_SHORT, -1 },
			int.class, new int[] { Opcodes.ILOAD, Opcodes.ISTORE, Opcodes.T_INT, Opcodes.IRETURN },
			long.class, new int[] { Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.T_LONG, Opcodes.LRETURN },
			float.class, new int[] { Opcodes.FLOAD, Opcodes.FSTORE, Opcodes.T_FLOAT, Opcodes.FRETURN },
			double.class, new int[] { Opcodes.DLOAD, Opcodes.DSTORE, Opcodes.T_DOUBLE, Opcodes.DRETURN });
		private static final int[] DEFAULT_OPCODES = { Opcodes.ALOAD, Opcodes.ASTORE, -1, Opcodes.ARETURN };

		@Override
		public String descriptor() {
			return clazz.descriptorString();
		}

		@Override
		public String internal() {
			return clazz.getName().replace('.', '/');
		}

		@Override
		public String name() {
			return clazz.getName();
		}

		@Override
		public boolean isInterface() { return clazz.isInterface(); }

		@Override
		public void acceptGetLocal(MethodVisitor visitor, int ordinal) {
			visitor.visitVarInsn(OPCODES.getOrDefault(clazz, DEFAULT_OPCODES)[0], ordinal);
		}

		@Override
		public void acceptSetLocal(MethodVisitor visitor, int ordinal) {
			visitor.visitVarInsn(OPCODES.getOrDefault(clazz, DEFAULT_OPCODES)[1], ordinal);
		}

		@Override
		public void acceptNewArray(MethodVisitor visitor) {
			int[] arr = OPCODES.getOrDefault(clazz, DEFAULT_OPCODES);
			if (arr[2] != -1) visitor.visitIntInsn(Opcodes.NEWARRAY, arr[2]);
			else visitor.visitTypeInsn(Opcodes.ANEWARRAY, internal());
		}

		@Override
		public void acceptReturn(MethodVisitor visitor) {
			visitor.visitInsn(OPCODES.getOrDefault(clazz, DEFAULT_OPCODES)[3]);
		}

		@Override
		public final String toString() {
			return name();
		}
	}
}

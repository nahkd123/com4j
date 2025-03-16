package io.github.nahkd123.com4j.impl.generator;

import java.util.List;
import java.util.function.Consumer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassGenerator implements Type, Scoped {
	private ClassVisitor visitor;
	private int modifiers;
	private String name, descriptor, internal;
	private Type superclass;
	private List<Type> interfaces;

	public ClassGenerator(ClassVisitor visitor, int modifiers, String name, Type superclass, List<Type> interfaces) {
		this.visitor = visitor;
		this.modifiers = modifiers;
		this.name = name;
		this.superclass = superclass;
		this.interfaces = interfaces;
		this.descriptor = "L%s;".formatted(name);
		this.internal = name.replace('.', '/');
	}

	@Override
	public String descriptor() {
		return descriptor;
	}

	@Override
	public String internal() {
		return internal;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void acceptGetLocal(MethodVisitor visitor, int ordinal) {
		visitor.visitVarInsn(Opcodes.ALOAD, ordinal);
	}

	@Override
	public void acceptSetLocal(MethodVisitor visitor, int ordinal) {
		visitor.visitVarInsn(Opcodes.ASTORE, ordinal);
	}

	@Override
	public void acceptReturn(MethodVisitor visitor) {
		visitor.visitInsn(Opcodes.ARETURN);
	}

	@Override
	public boolean isInterface() { return (modifiers & Opcodes.ACC_INTERFACE) != 0; }

	public ClassVisitor getVisitor() { return visitor; }

	public int getModifiers() { return modifiers; }

	public Type getSuperclass() { return superclass; }

	public List<Type> getInterfaces() { return interfaces; }

	@Override
	public void acceptNewArray(MethodVisitor visitor) {
		visitor.visitTypeInsn(Opcodes.ANEWARRAY, internal);
	}

	@Override
	public void begin() {
		visitor.visit(
			Opcodes.V23,
			modifiers, internal,
			null,
			superclass != null ? superclass.internal() : null,
			interfaces != null ? interfaces.stream().map(Type::internal).toArray(String[]::new) : null);
	}

	@Override
	public void end() {
		visitor.visitEnd();
	}

	public Var declareField(int modifiers, String name, Type type, Object value) {
		FieldVisitor fieldVisitor = visitor.visitField(modifiers, name, type.descriptor(), null, value);
		fieldVisitor.visitEnd();
		return (modifiers & Opcodes.ACC_STATIC) == 0
			? Var.ofInstance(this, name, type)
			: Var.ofStatic(this, name, type);
	}

	public Var declareField(int modifiers, String name, Type type) {
		return declareField(modifiers, name, type, null);
	}

	public MethodGenerator declareMethod(int modifiers, String name, MethodDescriptor descriptor, List<Type> exceptions, Consumer<MethodGenerator> callback) {
		MethodVisitor methodVisitor = visitor.visitMethod(
			modifiers,
			name,
			descriptor.descriptorString(),
			null,
			exceptions != null ? exceptions.stream().map(Type::internal).toArray(String[]::new) : new String[0]);
		MethodGenerator method = new MethodGenerator(methodVisitor, modifiers, superclass, name, descriptor, exceptions);
		methodVisitor.visitCode();
		callback.accept(method);
		methodVisitor.visitMaxs(0, 0);
		methodVisitor.visitEnd();
		return method;
	}

	public MethodGenerator declareConstructor(int modifiers, List<Type> params, List<Type> exceptions, Consumer<MethodGenerator> callback) {
		return declareMethod(
			modifiers,
			"<init>",
			MethodDescriptor.ofVoid(params.toArray(Type[]::new)),
			exceptions,
			callback);
	}

	public void declareStaticInit(Consumer<MethodGenerator> callback) {
		declareMethod(Opcodes.ACC_STATIC, "<clinit>", MethodDescriptor.ofVoid(), List.of(), callback);
	}

	public static ClassGenerator accept(ClassVisitor visitor, int modifiers, String name, Type superclass, List<Type> interfaces, Consumer<ClassGenerator> callback) {
		ClassGenerator g = new ClassGenerator(visitor, modifiers, name, superclass, interfaces);
		g.begin();
		callback.accept(g);
		g.end();
		return g;
	}

	@Override
	public String toString() {
		return name;
	}
}

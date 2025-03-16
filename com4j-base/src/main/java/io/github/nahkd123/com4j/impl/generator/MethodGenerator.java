package io.github.nahkd123.com4j.impl.generator;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodGenerator implements Method {
	private MethodVisitor visitor;
	private int modifiers;
	private Type owner;
	private String name;
	private MethodDescriptor descriptor;
	private List<Type> exceptions;
	private List<Var> extraLocalVars = new ArrayList<>();

	public MethodGenerator(MethodVisitor visitor, int modifiers, Type owner, String name, MethodDescriptor descriptor, List<Type> exceptions) {
		this.visitor = visitor;
		this.modifiers = modifiers;
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
		this.exceptions = exceptions;
	}

	@Override
	public Type owner() {
		return owner;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public MethodDescriptor descriptor() {
		return descriptor;
	}

	public int getModifiers() { return modifiers; }

	public MethodVisitor getVisitor() { return visitor; }

	public List<Type> getExceptions() { return exceptions; }

	public List<Var> getExtraLocalVars() { return extraLocalVars; }

	public int getTotalLocalVariables() {
		return ((modifiers & Opcodes.ACC_STATIC) == 0 ? 1 : 0) +
			descriptor.params().size() +
			extraLocalVars.size();
	}

	public Var declareLocal(Type type) {
		int newOrdinal = getTotalLocalVariables();
		Var newVar = Var.ofLocal(type, newOrdinal);
		extraLocalVars.add(newVar);
		return newVar;
	}

	public Var localVar(int ordinal) {
		int position = ordinal;

		if ((modifiers & Opcodes.ACC_STATIC) == 0) {
			if (position == 0) return owner.var(ordinal);
			position--;
		}

		if (position < descriptor.params().size()) return descriptor.params().get(position).var(ordinal);
		position -= descriptor.params().size();
		
		if (position < extraLocalVars.size()) return extraLocalVars.get(position);
		throw new IllegalArgumentException("Unknown local variable ordinal: %d".formatted(ordinal));
	}

	public MethodGenerator configureMax(int stackSize) {
		visitor.visitMaxs(stackSize, getTotalLocalVariables());
		return this;
	}

	public MethodGenerator label(Label label) {
		visitor.visitLabel(label);
		return this;
	}

	public MethodGenerator jump(int opcode, Label label) {
		visitor.visitJumpInsn(opcode, label);
		return this;
	}

	public MethodGenerator push(Var var) {
		var.acceptGet(visitor);
		return this;
	}

	public MethodGenerator insn(int opcode) {
		visitor.visitInsn(opcode);
		return this;
	}

	public MethodGenerator pushNull() { return insn(Opcodes.ACONST_NULL); }
	public MethodGenerator returnInsn() { return insn(Opcodes.RETURN); }
	public MethodGenerator areturn() { return insn(Opcodes.ARETURN); }
	public MethodGenerator dup() { return insn(Opcodes.DUP); }
	public MethodGenerator aaload() { return insn(Opcodes.AALOAD); }
	public MethodGenerator aastore() { return insn(Opcodes.AASTORE); }
	public MethodGenerator athrow() { return insn(Opcodes.ATHROW); }

	public MethodGenerator ldc(Object value) {
		visitor.visitLdcInsn(value);
		return this;
	}

	public final MethodGenerator invoke(int opcode, Method method, Runnable... params) {
		for (Runnable param : params) if (param != null) param.run();
		method.accept(visitor, opcode);
		return this;
	}

	@SafeVarargs
	public final MethodGenerator invokeSpecial(Method method, Runnable... params) {
		return invoke(Opcodes.INVOKESPECIAL, method, params);
	}

	@SafeVarargs
	public final MethodGenerator invokeInterface(Method method, Runnable... params) {
		return invoke(Opcodes.INVOKEINTERFACE, method, params);
	}

	@SafeVarargs
	public final MethodGenerator invokeStatic(Method method, Runnable... params) {
		return invoke(Opcodes.INVOKESTATIC, method, params);
	}

	@SafeVarargs
	public final MethodGenerator invokeVirtual(Method method, Runnable... params) {
		return invoke(Opcodes.INVOKEVIRTUAL, method, params);
	}

	public MethodGenerator set(Var var, Runnable param) {
		if (param != null) param.run();
		var.acceptSet(visitor);
		return this;
	}

	public MethodGenerator set(Var var) {
		return set(var, null);
	}

	public MethodGenerator newInst(Type type) {
		type.acceptNew(visitor);
		return this;
	}

	public MethodGenerator newArray(Type type, Runnable param) {
		if (param != null) param.run();
		type.acceptNewArray(visitor);
		return this;
	}

	public MethodGenerator newArray(Type type) {
		return newArray(type, null);
	}

	public MethodGenerator aastore(Runnable index, Runnable value) {
		if (index != null) index.run();
		if (value != null) value.run();
		return aastore();
	}

	public MethodGenerator aaload(Runnable index) {
		index.run();
		return aaload();
	}

	public MethodGenerator athrow(Runnable param) {
		if (param != null) param.run();
		return athrow();
	}

	public MethodGenerator inline(Runnable callback) {
		callback.run();
		return this;
	}

	public MethodGenerator xreturn(Type type) {
		type.acceptReturn(visitor);
		return this;
	}

	public MethodGenerator xreturn(Type type, Runnable param) {
		if (param != null) param.run();
		return xreturn(type);
	}

	public MethodGenerator tryCatchHandler(Label scopeStart, Label scopeEnd, Label handler, Type exception) {
		visitor.visitTryCatchBlock(scopeStart, scopeEnd, handler, exception != null ? exception.internal() : null);
		return this;
	}
}

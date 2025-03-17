package io.github.nahkd123.com4j.impl.wrapper;

import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.conversion.ConvertFromForeign;
import io.github.nahkd123.com4j.conversion.ConvertToForeign;
import io.github.nahkd123.com4j.conversion.ForeignConvertible;
import io.github.nahkd123.com4j.conversion.ForeignConvertibleLayout;
import io.github.nahkd123.com4j.impl.generator.ClassGenerator;
import io.github.nahkd123.com4j.impl.generator.Method;
import io.github.nahkd123.com4j.impl.generator.MethodDescriptor;
import io.github.nahkd123.com4j.impl.generator.MethodGenerator;
import io.github.nahkd123.com4j.impl.generator.Type;
import io.github.nahkd123.com4j.impl.generator.Var;
import io.github.nahkd123.com4j.itf.IUnknown;

// @formatter:off
public record ComWrapperInfo<T extends IUnknown>(Class<T> interfaceType, Class<? extends T> wrapperType, MethodHandle constructor) {
	private static final Type $AddressLayout = Type.of(AddressLayout.class);
	private static final Type $ValueLayout = Type.of(ValueLayout.class);
	private static final Type $StructLayout = Type.of(StructLayout.class);
	private static final Type $MemorySegment = Type.of(MemorySegment.class);
	private static final Type $MemoryLayout = Type.of(MemoryLayout.class);
	private static final Type $Linker = Type.of(Linker.class);
	private static final Type $FunctionDescriptor = Type.of(FunctionDescriptor.class);
	private static final Type $Arena = Type.of(Arena.class);
	private static final Type $MethodHandle = Type.of(MethodHandle.class);
	private static final Var $ValueLayout$ADDRESS = $ValueLayout.staticField("ADDRESS", $AddressLayout);
	private static final Method $MemorySegment$getA = $MemorySegment.method("get", MethodDescriptor.of($MemorySegment, $AddressLayout, Type.LONG));
	private static final Method $AddressLayout$withName = $AddressLayout.method("withName", MethodDescriptor.of($AddressLayout, Type.of(String.class)));
	private static final Method $AddressLayout$withTargetLayout = $AddressLayout.method("withTargetLayout", MethodDescriptor.of($AddressLayout, $MemoryLayout));
	private static final Method $MemoryLayout$structLayout = $MemoryLayout.method("structLayout", MethodDescriptor.of($StructLayout, Type.of(MemoryLayout[].class)));
	private static final Method $FunctionDescriptor$ofVoid = $FunctionDescriptor.method("ofVoid", MethodDescriptor.of($FunctionDescriptor, Type.of(MemoryLayout[].class)));
	private static final Method $FunctionDescriptor$of = $FunctionDescriptor.method("of", MethodDescriptor.of($FunctionDescriptor, $MemoryLayout, Type.of(MemoryLayout[].class)));
	private static final Method $Arena$ofConfined = $Arena.method("ofConfined", MethodDescriptor.of($Arena));
	private static final Method $Arena$close = $Arena.method("close", MethodDescriptor.ofVoid());

	private static MethodGenerator pushMemoryLayout(MethodGenerator g, Class<?> type) {
		// Primitives
		if (type == byte.class) return g.push($ValueLayout.staticField("JAVA_BYTE", Type.of(ValueLayout.OfByte.class)));
		if (type == short.class) return g.push($ValueLayout.staticField("JAVA_SHORT", Type.of(ValueLayout.OfShort.class)));
		if (type == int.class) return g.push($ValueLayout.staticField("JAVA_INT", Type.of(ValueLayout.OfInt.class)));
		if (type == long.class) return g.push($ValueLayout.staticField("JAVA_LONG", Type.of(ValueLayout.OfLong.class)));
		if (type == float.class) return g.push($ValueLayout.staticField("JAVA_FLOAT", Type.of(ValueLayout.OfFloat.class)));
		if (type == double.class) return g.push($ValueLayout.staticField("JAVA_DOUBLE", Type.of(ValueLayout.OfDouble.class)));
		if (type == boolean.class) return g.push($ValueLayout.staticField("JAVA_BOOLEAN", Type.of(ValueLayout.OfBoolean.class)));
		if (type == char.class) return g.push($ValueLayout.staticField("JAVA_CHAR", Type.of(ValueLayout.OfChar.class)));
		if (type == MemorySegment.class) return g.push($ValueLayout$ADDRESS);

		// Conversions
		if (type.isAnnotationPresent(ForeignConvertible.class)) {
			Class<?> target = type.getAnnotation(ForeignConvertible.class).value();

			if (target == MemorySegment.class) {
				Field reflectedLayoutField = Stream.of(type.getDeclaredFields())
					.filter(f -> Modifier.isStatic(f.getModifiers()) && f.isAnnotationPresent(ForeignConvertibleLayout.class))
					.findAny()
					.orElseThrow(() -> new IllegalArgumentException("%s does not have any static field with @%s".formatted(type, ForeignConvertibleLayout.class.getSimpleName())));
				return g.push(Var.of(reflectedLayoutField));
			}

			return pushMemoryLayout(g, target);
		}

		throw new IllegalArgumentException("Unable to derive MemoryLayout for %s".formatted(type));
	}

	private static boolean requiresArena(Class<?> type) {
		if (type.isPrimitive()) return false;
		if (type == MemorySegment.class) return false;

		if (type.isAnnotationPresent(ForeignConvertible.class)) {
			java.lang.reflect.Method reflectedMethod = Stream.of(type.getDeclaredMethods())
				.filter(m -> m.isAnnotationPresent(ConvertToForeign.class))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("%s does not have any method with @%s".formatted(type, ConvertToForeign.class.getSimpleName())));
			Class<?>[] params = reflectedMethod.getParameterTypes();

			// There are 2 methods at play here:
			// MyType.convertToForeign(MyType, Arena);
			// MyType#convertToFireign(Arena);
			return Stream.of(params).anyMatch(p -> p == Arena.class);
		}

		throw new IllegalArgumentException("Unable to find foreign value converter for %s".formatted(type));
	}

	private static MethodGenerator invokeConvertToForeign(MethodGenerator g, Class<?> type, Runnable source, Runnable arena) {
		if (type.isPrimitive()) return g.inline(source);
		if (type == MemorySegment.class) return g.inline(source);

		if (type.isAnnotationPresent(ForeignConvertible.class)) {
			java.lang.reflect.Method reflectedMethod = Stream.of(type.getDeclaredMethods())
				.filter(m -> m.isAnnotationPresent(ConvertToForeign.class))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("%s does not have any method with @%s".formatted(type, ConvertToForeign.class.getSimpleName())));
			Method method = Method.of(reflectedMethod);
			int invokeOpcode = Modifier.isStatic(reflectedMethod.getModifiers())
				? Opcodes.INVOKESTATIC
				: Opcodes.INVOKEVIRTUAL;
			return g.invoke(invokeOpcode, method, source, arena);
		}

		throw new IllegalArgumentException("Unable to find foreign value converter for %s".formatted(type));
	}

	private static MethodGenerator invokeConvertFromForeign(MethodGenerator g, Class<?> type, Runnable source) {
		if (type.isPrimitive()) return g.inline(source);
		if (type == MemorySegment.class) return g.inline(source);

		if (type.isAnnotationPresent(ForeignConvertible.class)) {
			Executable executable = Stream.of(type.getDeclaredConstructors())
				.filter(m -> m.isAnnotationPresent(ConvertFromForeign.class))
				.findAny()
				.map(c -> (Executable) c)
				.or(() -> Stream.of(type.getDeclaredMethods())
					.filter(m -> m.isAnnotationPresent(ConvertFromForeign.class))
					.findAny()
					.map(m -> (Executable) m))
				.orElseThrow(() -> new IllegalArgumentException("%s does not have any constructor or method with @%s".formatted(type, ConvertToForeign.class.getSimpleName())));
			int invokeOpcode = executable instanceof Constructor ? Opcodes.INVOKESPECIAL
				: Modifier.isStatic(executable.getModifiers()) ? Opcodes.INVOKESTATIC
				: Opcodes.INVOKEVIRTUAL;
			Method method = executable instanceof Constructor ctor
				? Method.of(ctor)
				: executable instanceof java.lang.reflect.Method m ? Method.of(m)
				: null;
			if (executable instanceof Constructor) return g.newInst(Type.of(type)).dup().invokeSpecial(method, source);
			return g.invoke(invokeOpcode, method, source);
		}

		throw new IllegalArgumentException("Unable to find foreign value converter for %s".formatted(type));
	}

	private static Type getForeignType(Class<?> type) {
		if (type.isPrimitive()) return Type.of(type);
		if (type == MemorySegment.class) return Type.of(type);

		if (type.isAnnotationPresent(ForeignConvertible.class)) {
			Class<?> target = type.getAnnotation(ForeignConvertible.class).value();
			return getForeignType(target);
		}

		throw new IllegalArgumentException("Unable to derive foreign type for %s".formatted(type));
	}

	private static MethodDescriptor getForeignDescriptor(java.lang.reflect.Method target) {
		List<Type> paramTypes = new ArrayList<>();
		paramTypes.add($MemorySegment);
		Stream.of(target.getParameterTypes()).map(ComWrapperInfo::getForeignType).forEach(paramTypes::add);
		return MethodDescriptor.of(
			getForeignType(target.getReturnType()),
			paramTypes.toArray(Type[]::new));
	}

	private static MethodGenerator invoke(MethodGenerator g, java.lang.reflect.Method target, Runnable arena, Runnable handle, Runnable self, Runnable... params) {
		MethodDescriptor foreignDescriptor = getForeignDescriptor(target);
		Method invoker = $MethodHandle.method("invoke", foreignDescriptor);
		Class<?>[] paramClasses = target.getParameterTypes();

		return invokeConvertFromForeign(g, target.getReturnType(), () -> {
			g.inline(handle).inline(self);
			for (int i = 0; i < paramClasses.length; i++) invokeConvertToForeign(g, paramClasses[i], params[i], arena);
			g.invokeVirtual(invoker);
		});
	}

	public static byte[] generateWrapperClass(Class<? extends IUnknown> interfaceType, List<java.lang.reflect.Method> comMethods) {
		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		String name = "%s.GeneratedWrapper".formatted(ComWrapperInfo.class.getPackageName());

		Type superclassType = Type.of(interfaceType);
		Method superclassInit = superclassType.method("<init>", MethodDescriptor.ofVoid(
			$MemorySegment,
			Type.of(Runnable.class)));

		ClassGenerator.accept(classWriter,
			ACC_PUBLIC | ACC_SUPER, name,
			superclassType,
			List.of(Type.of(ComWrapper.class)),
			wrapperClass -> {
				Var vtblLayoutField = wrapperClass.declareField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "vtblLayout", $MemoryLayout);
				Var linkerField = wrapperClass.declareField(ACC_PRIVATE, "linker", $Linker);
				Var vtblField = wrapperClass.declareField(ACC_PRIVATE, "vtbl", Type.of(MemorySegment.class));

				// <init>
				wrapperClass.declareConstructor(ACC_PUBLIC, List.of($MemorySegment, $Linker), List.of(), method -> {
					Var self = method.localVar(0);
					Var segment = method.localVar(1);
					Var linker = method.localVar(2);

					method
						.push(self).invokeSpecial(superclassInit, () -> method.push(segment), () -> method.pushNull())
						.push(self).set(linkerField, () -> method.push(linker))
						.push(self).set(vtblField, () -> method.push(segment).invokeInterface(
							$MemorySegment$getA,
							() -> method.push($ValueLayout$ADDRESS).invokeInterface($AddressLayout$withTargetLayout, () -> method.push(vtblLayoutField)),
							() -> method.ldc(0L)))
						.returnInsn();
				});

				// ComWrapper interface
				wrapperClass.declareMethod(ACC_PUBLIC, "getLinker", MethodDescriptor.of($Linker), null, m -> m.push(m.localVar(0)).push(linkerField).areturn());
				wrapperClass.declareMethod(ACC_PUBLIC, "getVtbl", MethodDescriptor.of($MemorySegment), null, m -> m.push(m.localVar(0)).push(vtblField).areturn());
				wrapperClass.declareMethod(ACC_PUBLIC, "getVtblLayout", MethodDescriptor.of($MemoryLayout), null, m -> m.push(vtblLayoutField).areturn());

				// Methods annotated with @ComMethod
				for (java.lang.reflect.Method comMethod : comMethods) {
					String methodName = comMethod.getName();
					MethodDescriptor descriptor = MethodDescriptor.deriveFrom(comMethod);

					Class<?> retClass = comMethod.getReturnType();
					Class<?>[] paramClasses = comMethod.getParameterTypes();
					boolean requiresArena = requiresArena(retClass) | Stream.of(paramClasses).anyMatch(ComWrapperInfo::requiresArena);

					Var descriptorField = wrapperClass.declareField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "f%s".formatted(methodName), $FunctionDescriptor);
					Var handleField = wrapperClass.declareField(ACC_PRIVATE, "h%s".formatted(methodName), Type.of(MethodHandle.class));

					wrapperClass.declareMethod(comMethod.getModifiers() & ~Modifier.ABSTRACT, methodName, descriptor, null, method -> {
						Type retType = method.descriptor().ret();
						Var self = method.localVar(0);
						Var arena = requiresArena ? method.declareLocal($Arena) : null;

						Runnable[] paramCodes = new Runnable[paramClasses.length];
						for (int i = 0; i < paramCodes.length; i++) {
							Var param = method.localVar(1 + i);
							paramCodes[i] = () -> method.push(param);
						}

						if (requiresArena) {
							Var retHolder = method.declareLocal(retType);
							Var exHolder = method.declareLocal(Type.of(Throwable.class));
							Label tryStart = new Label();
							Label tryEnd = new Label();
							Label exceptionHandler = new Label();
							Label trySuccess = new Label();

							method
								.tryCatchHandler(tryStart, tryEnd, exceptionHandler, Type.of(Throwable.class))
								.push(self).set(handleField, () -> method.push(self).invokeVirtual(
									wrapperClass.method("computeMethod", MethodDescriptor.of(
										Type.of(MethodHandle.class),
										Type.of(MethodHandle.class),
										Type.STRING,
										$FunctionDescriptor)),
									() -> method.push(self).push(handleField),
									() -> method.ldc(comMethod.getName()),
									() -> method.push(descriptorField)))
								.set(arena, () -> method.invokeStatic($Arena$ofConfined))

								.label(tryStart)
								.set(retHolder, () -> invoke(
									method, comMethod,
									() -> method.push(arena),
									() -> method.push(self).push(handleField),
									() -> method.push(self).invokeVirtual(wrapperClass.method("getComPointer", MethodDescriptor.of($MemorySegment))),
									paramCodes))

								.label(tryEnd)
								.jump(Opcodes.GOTO, trySuccess)

								.label(exceptionHandler).set(exHolder)
								.push(arena).invokeInterface($Arena$close)
								.athrow(() -> method.push(exHolder))

								.label(trySuccess)
								.push(arena).invokeInterface($Arena$close)
								.xreturn(retType, () -> method.push(retHolder));
						} else {
							method.push(self)
								.set(handleField, () -> method.push(self).invokeVirtual(
									wrapperClass.method("computeMethod", MethodDescriptor.of(
										Type.of(MethodHandle.class),
										Type.of(MethodHandle.class),
										Type.STRING,
										$FunctionDescriptor)),
									() -> method.push(self).push(handleField),
									() -> method.ldc(comMethod.getName()),
									() -> method.push(descriptorField)))
								.xreturn(retType, () -> invoke(
									method, comMethod,
									null,
									() -> method.push(self).push(handleField),
									() -> method.push(self).invokeVirtual(wrapperClass.method("getComPointer", MethodDescriptor.of($MemorySegment))),
									paramCodes));
						}
					});
				}

				// <clint>
				wrapperClass.declareStaticInit(method -> {
					// vtblLayout
					method.set(vtblLayoutField, () -> method.invokeStatic($MemoryLayout$structLayout, () -> {
						method.newArray($MemoryLayout, () -> method.ldc(comMethods.size()));

						for (int i = 0; i < comMethods.size(); i++) {
							java.lang.reflect.Method comMethod = comMethods.get(i);
							int index = i;
							method.dup().aastore(() -> method.ldc(index), () -> {
								method.push($ValueLayout$ADDRESS);
								if (comMethod != null) method.invokeInterface($AddressLayout$withName, () -> method.ldc(comMethod.getName()));
							});
						}
					}));

					// Native function descriptors
					for (java.lang.reflect.Method comMethod : comMethods) {
						Var descriptorField = wrapperClass.staticField("f%s".formatted(comMethod.getName()), $FunctionDescriptor);
						method.set(descriptorField, () -> {
							if (comMethod.getReturnType() != void.class) {
								pushMemoryLayout(method, comMethod.getReturnType());
							}

							method.newArray($MemoryLayout, () -> method.ldc(comMethod.getParameterTypes().length + 1));

							// Self
							method.dup().aastore(() -> method.ldc(0), () -> method.push($ValueLayout$ADDRESS));

							// Parameters
							Class<?>[] paramClasses = comMethod.getParameterTypes();
							for (int i = 0; i < paramClasses.length; i++) {
								Class<?> paramClass = paramClasses[i];
								int index = i + 1;
								method.dup().aastore(() -> method.ldc(index), () -> pushMemoryLayout(method, paramClass));
							}

							method.invokeStatic(comMethod.getReturnType() == void.class
								? $FunctionDescriptor$ofVoid
								: $FunctionDescriptor$of);
						});
					}

					method.returnInsn();
				});
			});

		return classWriter.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public static <T extends IUnknown> ComWrapperInfo<T> create(Class<T> interfaceType) throws Throwable {
		// Collect information about annotated methods
		List<java.lang.reflect.Method> comMethods = new ArrayList<>();
		Class<?> current = interfaceType;

		while (IUnknown.class.isAssignableFrom(current)) {
			for (java.lang.reflect.Method method : current.getDeclaredMethods()) {
				ComMethod annotation = method.getAnnotation(ComMethod.class);
				if (annotation == null) continue;
				while (comMethods.size() <= annotation.index()) comMethods.add(null);
				comMethods.set(annotation.index(), method);
			}

			current = current.getSuperclass();
		}

		byte[] classBytes = generateWrapperClass(interfaceType, comMethods);
		MethodHandles.Lookup lookup = MethodHandles.lookup().defineHiddenClass(classBytes, true);
		Class<? extends T> wrapperType = (Class<? extends T>) lookup.lookupClass();
		MethodType ctorType = methodType(void.class, MemorySegment.class, Linker.class);
		MethodHandle ctor = lookup.findConstructor(wrapperType, ctorType);
		return new ComWrapperInfo<>(interfaceType, wrapperType, ctor);
	}

	public T wrap(MemorySegment memory, Linker linker) throws Throwable {
		T result = (T) constructor.invoke(memory.reinterpret(ValueLayout.ADDRESS.byteSize()), linker);
		return result;
	}
}

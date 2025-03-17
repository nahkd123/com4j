package io.github.nahkd123.com4j.impl.wrapper;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Function;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.conversion.ForeignConverter;
import io.github.nahkd123.com4j.itf.IUnknown;

public record Java2ComFunc(String name, MethodHandle sourceMethod, MemorySegment targetFunction, FunctionDescriptor targetDescriptor) {
	/**
	 * <p>
	 * </p>
	 * 
	 * @param name          Name of the function. For use in {@link PathElement}.
	 * @param source        The source instance method handle on Java class.
	 * @param factoryArena  The arena that is used by {@link ComFactory}. For
	 *                      linking to new {@link MemorySegment}.
	 * @param linker        The linker.
	 * @param javaObjLookup The lookup table that lookups Java object from pointer
	 *                      of COM object.
	 * @return A new vtbl function description.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Java2ComFunc mapArgs(String name, MethodHandle source, Arena factoryArena, Linker linker, Function<MemorySegment, IUnknown> javaObjLookup) {
		MethodType sourceType = source.type();
		ForeignConverter<?> ret = sourceType.returnType() != void.class
			? ForeignConverter.of(sourceType.returnType())
			: null;
		ForeignConverter<?>[] params = new ForeignConverter[sourceType.parameterCount()];

		// Create target foreign function descriptor & method type
		MemoryLayout targetReturnLayout = ret != null ? ret.layout() : null;
		MemoryLayout[] targetParamLayouts = new MemoryLayout[sourceType.parameterCount()];
		targetParamLayouts[0] = ValueLayout.ADDRESS; // The first one will always be `this`

		Class<?> targetReturnType = ret != null ? ret.targetType() : void.class;
		Class<?>[] targetParamTypes = new Class[sourceType.parameterCount()];
		targetParamTypes[0] = MemorySegment.class;

		for (int i = 1; i < targetParamLayouts.length; i++) {
			params[i] = ForeignConverter.of(sourceType.parameterType(i));
			targetParamLayouts[i] = params[i].layout();
			targetParamTypes[i] = params[i].targetType();
		}

		FunctionDescriptor targetDesc = targetReturnLayout != null
			? FunctionDescriptor.of(targetReturnLayout, targetParamLayouts)
			: FunctionDescriptor.ofVoid(targetParamLayouts);
		MethodType targetHandleType = MethodType.methodType(targetReturnType, targetParamTypes);

		// Map source MethodHandle to new target
		VarargsFunction mapper = (foreignSelf, foreignArgs) -> {
			Object[] javaArgs = new Object[sourceType.parameterCount()];
			javaArgs[0] = javaObjLookup.apply(foreignSelf);
			for (int i = 1; i < sourceType.parameterCount(); i++)
				javaArgs[i] = params[i].convertToJava(foreignArgs[i - 1]);

			try (Arena localArena = Arena.ofConfined()) {
				Object javaResult = source.invokeWithArguments(List.of(javaArgs));
				return ret != null ? ((ForeignConverter) ret).convertToForeign(javaResult, localArena) : null;
			} catch (Throwable e) {
				throw new RuntimeException("Invocation failed for %s".formatted(name), e);
			}
		};

		VarargsConsumer consumer = targetReturnLayout == null ? (self, args) -> mapper.apply(self, args) : null;
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodHandle target;

		try {
			target = consumer == null
				? lookup.findVirtual(
					VarargsFunction.class,
					"apply",
					MethodType.methodType(Object.class, MemorySegment.class, Object[].class))
				: lookup.findVirtual(
					VarargsConsumer.class,
					"apply",
					MethodType.methodType(void.class, MemorySegment.class, Object[].class));
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Unable to get apply() (JVM bug?)", e);
		}

		target = target
			.bindTo(consumer != null ? consumer : mapper)
			.asVarargsCollector(Object[].class)
			.asType(targetHandleType);
		MemorySegment upcallPointer = linker.upcallStub(target, targetDesc, factoryArena);
		return new Java2ComFunc(name, source, upcallPointer, targetDesc);
	}

	@Override
	public final String toString() {
		return "%s%s -> %s".formatted(name, sourceMethod.type(), targetDescriptor);
	}

	@FunctionalInterface
	private interface VarargsFunction {
		Object apply(MemorySegment self, Object... args);
	}

	@FunctionalInterface
	private interface VarargsConsumer {
		void apply(MemorySegment self, Object... args);
	}
}

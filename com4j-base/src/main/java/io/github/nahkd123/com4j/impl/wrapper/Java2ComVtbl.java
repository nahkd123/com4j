package io.github.nahkd123.com4j.impl.wrapper;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.itf.IUnknown;

/**
 * <p>
 * Represent a virtual table for wrapping Java object to COM.
 * </p>
 */
public record Java2ComVtbl(List<Java2ComFunc> functions, MemoryLayout layout, MemorySegment struct) {
	public static Java2ComVtbl create(List<Java2ComFunc> functions, Arena arena) {
		MemoryLayout[] layoutElements = new MemoryLayout[functions.size()];

		for (int i = 0; i < functions.size(); i++) {
			Java2ComFunc function = functions.get(i);
			layoutElements[i] = function != null
				? ValueLayout.ADDRESS.withName(function.name())
				: ValueLayout.ADDRESS;
		}

		MemoryLayout layout = MemoryLayout.structLayout(layoutElements);
		MemorySegment struct = arena.allocate(layout);

		for (int i = 0; i < layoutElements.length; i++) {
			Java2ComFunc function = functions.get(i);
			VarHandle vh = layout.varHandle(PathElement.groupElement(i));
			vh.set(struct, 0L, function != null ? function.targetFunction() : MemorySegment.NULL);
		}

		return new Java2ComVtbl(functions, layout, struct);
	}

	public static <T extends IUnknown> Java2ComVtbl create(Class<T> classType, Arena arena, Linker linker, Function<MemorySegment, IUnknown> javaObjLookup) {
		List<Java2ComFunc> functions = new ArrayList<>();
		Set<String> includedFunctionNames = new HashSet<>();
		Class<?> clazz = classType;
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		while (IUnknown.class.isAssignableFrom(clazz)) {
			Map<Method, ComMethod> declared = Stream.of(clazz.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(ComMethod.class))
				.collect(Collectors.toMap(
					Function.identity(),
					method -> method.getDeclaredAnnotation(ComMethod.class)));

			for (Map.Entry<Method, ComMethod> entry : declared.entrySet()) {
				Method reflectedMethod = entry.getKey();
				ComMethod comInfo = entry.getValue();
				if (includedFunctionNames.contains(reflectedMethod.getName())) continue;
				includedFunctionNames.add(reflectedMethod.getName());

				try {
					MethodHandle sourceMethod = lookup.unreflect(reflectedMethod);
					Java2ComFunc func = Java2ComFunc.mapArgs(
						reflectedMethod.getName(), sourceMethod,
						arena, linker, javaObjLookup);
					while (functions.size() <= comInfo.index()) functions.add(null);
					functions.set(comInfo.index(), func);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("%s is not visible".formatted(reflectedMethod), e);
				}
			}

			clazz = clazz.getSuperclass();
		}

		return create(functions, arena);
	}
}

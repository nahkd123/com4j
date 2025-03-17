package io.github.nahkd123.com4j.conversion;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.itf.IUnknown;

public record ForeignConverter<T>(Class<T> javaType, Class<?> targetType, MemoryLayout layout, MethodHandle fromForeign, MethodHandle toForeign, boolean useArena) {

	private static final Map<Class<?>, ForeignConverter<?>> CONVERTERS = new HashMap<>();
	private static final Map<Class<?>, ValueLayout> PRIMITIVE_LAYOUTS = Map.of(
		byte.class, ValueLayout.JAVA_BYTE,
		short.class, ValueLayout.JAVA_SHORT,
		int.class, ValueLayout.JAVA_INT,
		long.class, ValueLayout.JAVA_LONG,
		float.class, ValueLayout.JAVA_FLOAT,
		double.class, ValueLayout.JAVA_DOUBLE,
		boolean.class, ValueLayout.JAVA_BOOLEAN,
		MemorySegment.class, ValueLayout.ADDRESS);

	static {
		for (Map.Entry<Class<?>, ValueLayout> entry : PRIMITIVE_LAYOUTS.entrySet()) {
			Class<?> clazz = entry.getKey();
			ValueLayout layout = entry.getValue();
			CONVERTERS.put(
				clazz,
				new ForeignConverter<>(clazz, clazz, layout, MethodHandles.identity(clazz), MethodHandles
					.identity(clazz), false));
		}
	}

	/**
	 * <p>
	 * Get (or create if not exists) the converter for converting between Java class
	 * and foreign value.
	 * </p>
	 * 
	 * @param <T>      The type of Java object.
	 * @param javaType The Java class that annotated with
	 *                 {@link ForeignConvertible}.
	 * @return The converter for converting between Java type and foreign type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ForeignConverter<T> of(Class<T> javaType) {
		return (ForeignConverter<T>) CONVERTERS.computeIfAbsent(javaType, ForeignConverter::createConverter);
	}

	/**
	 * <p>
	 * Create a new converter derived from annotations in Java class. This does not
	 * cache the converter!
	 * </p>
	 * 
	 * @param javaType The Java class that annotated with
	 *                 {@link ForeignConvertible}.
	 * @return The converter for converting between Java type and foreign type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ForeignConverter<T> createConverter(Class<T> javaType) {
		if (IUnknown.class.isAssignableFrom(javaType)) {
			Class<?> targetType = MemorySegment.class;
			MemoryLayout layout = ValueLayout.ADDRESS.withTargetLayout(ValueLayout.ADDRESS);
			ComFactory factory = ComFactory.instance();
			Function<MemorySegment, T> fromForeign = comPtr -> (T) factory
				.wrap(comPtr, (Class<? extends IUnknown>) javaType);
			Function<T, MemorySegment> toForeign = javaObj -> ((IUnknown) javaObj).getComPointer();
			MethodHandles.Lookup lookup = MethodHandles.lookup();

			try {
				MethodHandle funcHandle = lookup.findVirtual(
					Function.class,
					"apply",
					MethodType.genericMethodType(1));
				MethodHandle fromForeignHandle = funcHandle.bindTo(fromForeign);
				MethodHandle toForeignHandle = funcHandle.bindTo(toForeign);
				return new ForeignConverter<T>(javaType, targetType, layout, fromForeignHandle, toForeignHandle, false);
			} catch (ReflectiveOperationException t) {
				throw new IllegalArgumentException("Unable to derive IUnknown foreign converter for %s"
					.formatted(javaType));
			}
		}

		try {
			ForeignConvertible convertibleInfo = javaType.getDeclaredAnnotation(ForeignConvertible.class);
			if (convertibleInfo == null) throw new IllegalArgumentException("%s is not annotated with %s".formatted(
				javaType, ForeignConvertible.class));
			Class<?> targetType = convertibleInfo.value();

			MethodHandles.Lookup lookup = MethodHandles.lookup();
			MemoryLayout layout;

			if (targetType == MemorySegment.class) {
				// Struct type
				Field layoutField = Stream.of(javaType.getDeclaredFields())
					.filter(field -> field.isAnnotationPresent(ForeignConvertibleLayout.class))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("%s does not have field annotated with %s"
						.formatted(javaType, ForeignConvertibleLayout.class)));
				layout = (MemoryLayout) lookup
					.findStaticGetter(javaType, layoutField.getName(), MemoryLayout.class)
					.invoke();
			} else {
				layout = PRIMITIVE_LAYOUTS.get(targetType);
				if (layout == null) throw new IllegalArgumentException("Cannot convert %s to corresponding MemoryLayout"
					.formatted(targetType));
			}

			MethodType fromForeignType = MethodType.methodType(javaType, targetType);
			MethodHandle fromForeign = Optional.<MethodHandle>empty()
				.or(() -> Stream.of(javaType.getDeclaredConstructors())
					.filter(ctor -> ctor.isAnnotationPresent(ConvertFromForeign.class))
					.findFirst()
					.map(ctor -> {
						try {
							return lookup.unreflectConstructor(ctor).asType(fromForeignType);
						} catch (ReflectiveOperationException e) {
							throw new RuntimeException(e);
						}
					}))
				.or(() -> Stream.of(javaType.getDeclaredMethods())
					.filter(method -> Modifier.isStatic(method.getModifiers()))
					.filter(method -> method.isAnnotationPresent(ConvertFromForeign.class))
					.findFirst()
					.map(method -> {
						try {
							return lookup.unreflect(method).asType(fromForeignType);
						} catch (ReflectiveOperationException e) {
							throw new RuntimeException(e);
						}
					}))
				.orElseThrow(
					() -> new IllegalArgumentException("%s does not have method/constructor annotated with %s"
						.formatted(javaType, ConvertFromForeign.class)));

			MethodType toForeignType = MethodType.methodType(targetType, javaType);
			MethodType toForeignTypeWithArena = MethodType.methodType(targetType, javaType, Arena.class);
			MethodHandle toForeign = Stream.of(javaType.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(ConvertToForeign.class))
				.findFirst()
				.map(method -> {
					try {
						MethodHandle handle = lookup.unreflect(method);
						if (handle.type().equals(toForeignType))
							return handle.asType(toForeignType);
						else if (handle.type().equals(toForeignTypeWithArena))
							return handle.asType(toForeignTypeWithArena);
						else throw new IllegalArgumentException("%s is not (%s[, Arena])%s"
							.formatted(method, javaType.getSimpleName(), targetType.getSimpleName()));
					} catch (ReflectiveOperationException e) {
						throw new RuntimeException(e);
					}
				})
				.orElseThrow(() -> new IllegalArgumentException("%s does not have method annotated with %s".formatted(
					javaType, ConvertFromForeign.class)));
			boolean useArena = toForeign.type().parameterCount() == 2
				&& toForeign.type().parameterType(1) == Arena.class;

			return new ForeignConverter<>(javaType, targetType, layout, fromForeign, toForeign, useArena);
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public T convertToJava(Object foreign) {
		try {
			if (foreign instanceof MemorySegment segment && layout != null)
				foreign = segment.reinterpret(layout.byteSize());
			return (T) fromForeign.invoke(foreign);
		} catch (Throwable e) {
			throw new RuntimeException("Failed to convert %s (foreign)".formatted(foreign), e);
		}
	}

	public Object convertToForeign(T java, Arena arena) {
		try {
			Object result = useArena
				? toForeign.invoke(java, arena)
				: toForeign.invoke(java);
			if (result instanceof MemorySegment segment && layout != null)
				result = segment.reinterpret(layout.byteSize());
			return result;
		} catch (Throwable e) {
			throw new RuntimeException("Failed to convert %s (Java)".formatted(java), e);
		}
	}
}

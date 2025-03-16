package io.github.nahkd123.com4j;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;

import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.impl.ComFactoryImpl;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.win32.Guid;
import io.github.nahkd123.com4j.wrapper.JavaToComFactory;

public interface ComFactory {
	/**
	 * <p>
	 * Wrap an existing COM object as Java object. This method does not increase
	 * reference counter.
	 * </p>
	 * <p>
	 * Internally this will generate a wrapper class based on provided interface
	 * class, then wrap the pointer with that class.
	 * </p>
	 * 
	 * @param <T>           The type of Java object.
	 * @param comPtr        The pointer to COM object.
	 * @param interfaceType The class that contains interface definition.
	 * @return A Java object that is wrapping COM object.
	 */
	<T extends IUnknown> T wrap(MemorySegment comPtr, Class<T> interfaceType);

	/**
	 * <p>
	 * Create a new, unwrapped COM object for passing to foreign receiver
	 * immediately. You can optionally wrap it as {@link IUnknown} with
	 * {@link #wrap(MemorySegment, Class))}. The newly created COM object will have
	 * reference counter value of 1.
	 * </p>
	 * <p>
	 * Internally this will call {@code CoCreateInstance} with IID and CLSID, using
	 * {@code CLSCTX_ALL} class context.
	 * </p>
	 * 
	 * @param iid   The interface GUID.
	 * @param clsid The class GUID.
	 * @return The pointer to COM object.
	 */
	MemorySegment createFromClsid(Guid iid, Guid clsid);

	/**
	 * <p>
	 * Create a new COM object and wrap it inside Java object. This method basically
	 * calls {@link #createFromClsid(Guid, Guid)} with interface GUID derived from
	 * {@link Class} and {@link #wrap(MemorySegment, Class)}.
	 * </p>
	 * 
	 * @param <T>           The type of Java object.
	 * @param interfaceType The class that contains interface definition.
	 * @param clsid         The class GUID.
	 * @return A Java object that is wrapping COM object.
	 */
	default <T extends IUnknown> T createFromClsid(Class<T> interfaceType, Guid clsid) {
		Guid iid = ComUtils.iidOf(interfaceType);
		return wrap(createFromClsid(iid, clsid), interfaceType);
	}

	/**
	 * <p>
	 * Create a new Java object, then wrap it as COM object. To pass the pointer of
	 * COM wrapper to foreign receiver, use {@link IUnknown#getComPointer()}. The
	 * newly created Java object will have reference counter value of 1.
	 * </p>
	 * <p>
	 * In the future, there will be a new method that make use of scoped values.
	 * When that happens, this method will be marked for removal. Trying to
	 * instantiate object without passing through
	 * {@code createJava(Class, Runnable)} will throw exception.
	 * </p>
	 * 
	 * @param <T>           The type of Java object.
	 * @param interfaceType The class that contains interface definition.
	 * @param ctor          The constructor of Java object.
	 * @return A Java object with COM wrapper object.
	 */
	<T extends IUnknown> T createJava(Class<T> interfaceType, JavaToComFactory<T> ctor);

	/**
	 * <p>
	 * A reflective version of {@link #createJava(Class)}, using reflection to find
	 * constructor that matches
	 * {@code <init>(Ljava/lang/foreign/MemorySegment;Ljava/lang/Runnable;)V}
	 * descriptor.
	 * </p>
	 * <p>
	 * Interface GUID is obtained from the first superclass with
	 * {@link ComInterface} annotation, starting from the provided class's
	 * superclass.
	 * </p>
	 * 
	 * @param <T>       The type of Java object.
	 * @param classType The class that extending {@link IUnknown}, either directly
	 *                  or indirectly.
	 * @return A Java object with COM wrapper object.
	 */
	<T extends IUnknown> T createJava(Class<T> classType);

	/**
	 * <p>
	 * Get the main instance of {@link ComFactory}. The instance will only be
	 * initialized when this method being called for the first time. The instance
	 * will use {@link Arena#ofAuto()} to manage memory and
	 * {@link Linker#nativeLinker()} as linker.
	 * </p>
	 * 
	 * @return The instance.
	 */
	static ComFactory instance() {
		return ComFactoryImpl.getInstance();
	}
}

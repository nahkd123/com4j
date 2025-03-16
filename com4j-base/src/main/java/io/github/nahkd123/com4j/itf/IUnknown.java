package io.github.nahkd123.com4j.itf;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.ComUtils;
import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.win32.Guid;
import io.github.nahkd123.com4j.win32.HResult;

/**
 * <p>
 * This is the base interface for any COM objects. To declare a new COM
 * interface, make a new Java <em>abstract</em> class extending {@link IUnknown}
 * either directly or indirectly, as well as annotate your abstract class with
 * {@link ComInterface} containing the IID. To declare a new COM class, make a
 * regular Java class extending {@link IUnknown}, either directly or indirectly.
 * </p>
 * <p>
 * <b>Reference counter</b>: COM uses reference counting mechanism for resources
 * management. This {@link IUnknown} internally have an atomic counter that
 * increase 1 when {@link #AddRef()} is called and decrease 1 when
 * {@link #Release()} is called. Upon reaching 0, the Java object will
 * unreference {@link MemorySegment} that is holding COM object, as well as
 * invoking destroy callback (passed from {@link ComFactory}), allowing garbage
 * collector to free the memory. Garbage collector may not free the
 * {@link MemorySegment} immediately after releasing.
 * </p>
 */
@ComInterface("00000000-0000-0000-C000-000000000046")
public abstract class IUnknown {
	private MemorySegment comPtr;
	private AtomicInteger references = new AtomicInteger(1);
	private Runnable destroyCallback;

	public IUnknown(MemorySegment comPtr, Runnable destroyCallback) {
		this.comPtr = comPtr;
		this.destroyCallback = destroyCallback;
	}

	/**
	 * <p>
	 * Get the pointer to COM object. This will not increase the reference count.
	 * The COM object is considered to be destroyed if the returned value is
	 * {@code null}.
	 * </p>
	 * 
	 * @return The pointer to COM object.
	 */
	public MemorySegment getComPointer() { return comPtr; }

	private void ensureNotDestroyed() {
		if (comPtr == null) throw new IllegalStateException("already destroyed");
	}

	@ComMethod(index = 0)
	public HResult QueryInterface(Guid iid, MemorySegment ppvObject) {
		ensureNotDestroyed();

		if (ComUtils.isAssignableTo(iid, this.getClass())) {
			ppvObject.set(ValueLayout.ADDRESS, 0L, getComPointer());
			AddRef();
			return HResult.SUCCEED;
		}

		ppvObject.set(ValueLayout.ADDRESS, 0L, MemorySegment.NULL);
		return HResult.E_NOINTERFACE;
	}

	@ComMethod(index = 1)
	public int AddRef() {
		ensureNotDestroyed();
		return references.incrementAndGet();
	}

	@ComMethod(index = 2)
	public int Release() {
		ensureNotDestroyed();
		int refCount = references.decrementAndGet();

		if (refCount <= 0) {
			if (destroyCallback != null) destroyCallback.run();
			comPtr = null;
		}

		return refCount;
	}
}

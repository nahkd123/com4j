package io.github.nahkd123.com4j;

import java.lang.foreign.MemorySegment;

import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.itf.IUnknown;

public abstract class IMyComObject extends IUnknown {
	public IMyComObject(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}

	@ComMethod(index = 3)
	public abstract void testV();

	@ComMethod(index = 4)
	public abstract int testI();

	@ComMethod(index = 5)
	public abstract void testVI(int i);
}
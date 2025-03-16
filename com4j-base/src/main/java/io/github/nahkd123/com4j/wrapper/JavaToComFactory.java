package io.github.nahkd123.com4j.wrapper;

import java.lang.foreign.MemorySegment;

import io.github.nahkd123.com4j.itf.IUnknown;

@FunctionalInterface
public interface JavaToComFactory<T extends IUnknown> {
	T create(MemorySegment comPtr, Runnable destroyCallback);
}

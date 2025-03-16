package io.github.nahkd123.com4j.itf.realtimestylus;

import java.lang.foreign.MemorySegment;

import io.github.nahkd123.com4j.annotation.ComInterface;

@ComInterface("A7CCA85A-31BC-4cd2-AADC-3289A3AF11C8")
public abstract class IStylusAsyncPlugin extends IStylusPlugin {
	public IStylusAsyncPlugin(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}
}

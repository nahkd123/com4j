package io.github.nahkd123.com4j.itf.realtimestylus;

import java.lang.foreign.MemorySegment;

import io.github.nahkd123.com4j.annotation.ComInterface;

@ComInterface("A157B174-482F-4d71-A3F6-3A41DDD11BE9")
public abstract class IStylusSyncPlugin extends IStylusPlugin {
	public IStylusSyncPlugin(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}
}

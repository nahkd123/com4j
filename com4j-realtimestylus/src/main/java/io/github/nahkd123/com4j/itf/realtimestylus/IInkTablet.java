package io.github.nahkd123.com4j.itf.realtimestylus;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.itf.automation.IDispatch;
import io.github.nahkd123.com4j.win32.HResult;

@ComInterface("2DE25EAA-6EF8-42d5-AEE9-185BC81B912D")
public abstract class IInkTablet extends IDispatch {
	public IInkTablet(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}

	@ComMethod(index = 7)
	public abstract HResult get_Name(MemorySegment name);

	@ComMethod(index = 8)
	public abstract HResult get_PlugAndPlayId(MemorySegment name);

	public String getName() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment bstr = arena.allocate(ValueLayout.ADDRESS);
			get_Name(bstr).throwIfFail();
			return ComFactory.instance().bstr().getAndFree(bstr.get(ValueLayout.ADDRESS, 0L));
		}
	}

	public String getPlugAndPlayId() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment bstr = arena.allocate(ValueLayout.ADDRESS);
			get_PlugAndPlayId(bstr).throwIfFail();
			return ComFactory.instance().bstr().getAndFree(bstr.get(ValueLayout.ADDRESS, 0L));
		}
	}
}

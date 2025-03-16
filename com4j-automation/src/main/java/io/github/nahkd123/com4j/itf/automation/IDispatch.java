package io.github.nahkd123.com4j.itf.automation;

import java.lang.foreign.MemorySegment;

import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.win32.Guid;
import io.github.nahkd123.com4j.win32.HResult;

@ComInterface("00020400-0000-0000-C000-000000000046")
public abstract class IDispatch extends IUnknown {
	public IDispatch(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}

	@ComMethod(index = 3)
	public abstract HResult GetTypeInfoCount(MemorySegment pctinfo);

	@ComMethod(index = 4)
	public abstract HResult GetTypeInfo(int iTInfo, int lcid, MemorySegment ppTInfo);

	@ComMethod(index = 5)
	public abstract HResult GetIDsOfNames(Guid riid, MemorySegment rgszNames, int cNames, int lcid, MemorySegment rgDispId);

	@ComMethod(index = 6)
	public abstract HResult Invoke(int dispIdMember, Guid riid, int lcid, short wFlags, MemorySegment pDispParams, MemorySegment pVarResult, MemorySegment pExcepInfo, MemorySegment puArgErr);
}

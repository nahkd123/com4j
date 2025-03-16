package io.github.nahkd123.com4j.impl.wrapper;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

public interface ComWrapper {
	MemorySegment getVtbl();

	MemoryLayout getVtblLayout();

	Linker getLinker();

	default MethodHandle computeMethod(MethodHandle existing, String name, FunctionDescriptor descriptor) {
		if (existing != null) return existing;
		MemorySegment vtblFunc = (MemorySegment) getVtblLayout()
			.varHandle(PathElement.groupElement(name))
			.get(getVtbl(), 0L);
		return getLinker().downcallHandle(vtblFunc, descriptor);
	}
}

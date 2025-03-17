package io.github.nahkd123.com4j.impl;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import io.github.nahkd123.com4j.Bstr;

// BSTR appears to be belong to Automation
public class BstrImpl implements Bstr {
	private MethodHandle SysAllocString;
	private MethodHandle SysFreeString;

	public BstrImpl(Arena arena, Linker linker, SymbolLookup ole32) {
		SysAllocString = linker.downcallHandle(ole32.findOrThrow("PropSysAllocString"), FunctionDescriptor.of(
			ValueLayout.ADDRESS,
			ValueLayout.ADDRESS));
		SysFreeString = linker.downcallHandle(ole32.findOrThrow("PropSysFreeString"), FunctionDescriptor.ofVoid(
			ValueLayout.ADDRESS));
	}

	@Override
	public String get(MemorySegment bstr) {
		// BSTR is a pointer to the first character of the string, with 32-bit integer
		// before it.
		// https://learn.microsoft.com/en-us/previous-versions/windows/desktop/automat/bstr
		int bytes = MemorySegment
			.ofAddress(bstr.address() - 4)
			.reinterpret(ValueLayout.JAVA_INT.byteSize())
			.get(ValueLayout.JAVA_INT, 0L);
		byte[] data = bstr.reinterpret(bytes).toArray(ValueLayout.JAVA_BYTE);
		return new String(data, StandardCharsets.UTF_16LE);
	}

	@Override
	public void free(MemorySegment bstr) {
		try {
			SysFreeString.invoke(bstr);
		} catch (Throwable t) {
			throw new RuntimeException("Failed to free string", t);
		}
	}

	@Override
	public MemorySegment allocate(String content) {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment wchars = arena.allocateFrom(content, StandardCharsets.UTF_16LE);
			MemorySegment bstr = (MemorySegment) SysAllocString.invoke(wchars);
			if (bstr.equals(MemorySegment.NULL))
				throw new RuntimeException("Failed to allocate string: NULL returned");
			return bstr;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to allocate string", t);
		}
	}
}

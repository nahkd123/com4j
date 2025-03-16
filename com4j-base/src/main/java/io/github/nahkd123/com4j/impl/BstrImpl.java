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
		String s = "";
		char ch;
		do {
			ch = bstr
				.reinterpret(ValueLayout.JAVA_CHAR.scale(0L, s.length() + 1))
				.get(ValueLayout.JAVA_CHAR, s.length() * 2);
			if (ch == '\0') break;
			s += ch;
		} while (ch != '\0');
		return s;
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

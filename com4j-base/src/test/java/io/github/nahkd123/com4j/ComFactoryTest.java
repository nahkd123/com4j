package io.github.nahkd123.com4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.github.nahkd123.com4j.impl.ComFactoryImpl;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.win32.Guid;

class ComFactoryTest {
	@Test
	void testWrappingToCom() throws Throwable {
		AtomicInteger count = new AtomicInteger(0);
		ComFactory com = new ComFactoryImpl(Arena.ofAuto(), Linker.nativeLinker());
		IMyComObject java = com.createJava(
			IMyComObject.class,
			(comPtr, destroyCallback) -> new MyComObject(comPtr, destroyCallback, () -> count.incrementAndGet()));
		IMyComObject wrapped = com.wrap(java.getComPointer(), IMyComObject.class);
		wrapped.testV();
		wrapped.testVI(wrapped.testI());
		java.Release();
		assertEquals(1, count.intValue());
	}

	@Test
	void testWrappingToJava() throws Throwable {
		ComFactory com = new ComFactoryImpl(Arena.ofAuto(), Linker.nativeLinker());
		IUnknown rts = com.createFromClsid(IUnknown.class, Guid.of("E26B366D-F998-43ce-836F-CB6D904432B0"));
		rts.Release();
	}
}

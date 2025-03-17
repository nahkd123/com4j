package io.github.nahkd123.com4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.MemorySegment;

public class MyComObject extends IMyComObject {
	private Runnable cbTestV;

	public MyComObject(MemorySegment comPtr, Runnable destroyCallback, Runnable cbTestV) {
		super(comPtr, destroyCallback);
		this.cbTestV = cbTestV;
	}

	@Override
	public void testV() {
		cbTestV.run();
	}

	@Override
	public int testI() {
		return 42;
	}

	@Override
	public void testVI(int i) {
		assertEquals(42, i);
	}
}
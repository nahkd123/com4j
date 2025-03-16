package io.github.nahkd123.com4j.types.windef;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import io.github.nahkd123.com4j.conversion.ConvertFromForeign;
import io.github.nahkd123.com4j.conversion.ConvertToForeign;
import io.github.nahkd123.com4j.conversion.ForeignConvertible;
import io.github.nahkd123.com4j.conversion.ForeignConvertibleLayout;

@ForeignConvertible(MemorySegment.class)
public record Rect(int left, int top, int right, int bottom) {

	@ForeignConvertibleLayout
	public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
		ValueLayout.JAVA_INT.withName("left"),
		ValueLayout.JAVA_INT.withName("top"),
		ValueLayout.JAVA_INT.withName("right"),
		ValueLayout.JAVA_INT.withName("bottom"));

	private static final VarHandle VH_LEFT = LAYOUT.varHandle(PathElement.groupElement("left"));
	private static final VarHandle VH_TOP = LAYOUT.varHandle(PathElement.groupElement("top"));
	private static final VarHandle VH_RIGHT = LAYOUT.varHandle(PathElement.groupElement("right"));
	private static final VarHandle VH_BOTTOM = LAYOUT.varHandle(PathElement.groupElement("bottom"));

	@ConvertFromForeign
	public static Rect of(MemorySegment memory) {
		int left = (int) VH_LEFT.get(memory, 0L);
		int top = (int) VH_TOP.get(memory, 0L);
		int right = (int) VH_RIGHT.get(memory, 0L);
		int bottom = (int) VH_BOTTOM.get(memory, 0L);
		return new Rect(left, top, right, bottom);
	}

	@ConvertToForeign
	public MemorySegment asMemorySegment(Arena arena) {
		MemorySegment memory = arena.allocate(LAYOUT);
		VH_LEFT.set(memory, 0L, left());
		VH_TOP.set(memory, 0L, top());
		VH_RIGHT.set(memory, 0L, right());
		VH_BOTTOM.set(memory, 0L, bottom());
		return memory;
	}
}

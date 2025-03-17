package io.github.nahkd123.com4j.types.realtimestylus;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import io.github.nahkd123.com4j.conversion.ConvertFromForeign;
import io.github.nahkd123.com4j.conversion.ConvertToForeign;
import io.github.nahkd123.com4j.conversion.ForeignConvertible;
import io.github.nahkd123.com4j.conversion.ForeignConvertibleLayout;

@ForeignConvertible(MemorySegment.class)
public record StylusInfo(int tcid, int stylusId, boolean isInverted) {

	@ForeignConvertibleLayout
	public static final StructLayout LAYOUT = MemoryLayout.structLayout(
		ValueLayout.JAVA_INT.withName("tcid"),
		ValueLayout.JAVA_INT.withName("sid"),
		ValueLayout.JAVA_INT.withName("bIsInvertedCursor"));

	private static final VarHandle VH_TCID = LAYOUT.varHandle(PathElement.groupElement("tcid"));
	private static final VarHandle VH_SID = LAYOUT.varHandle(PathElement.groupElement("sid"));
	private static final VarHandle VH_INVERT = LAYOUT.varHandle(PathElement.groupElement("bIsInvertedCursor"));

	@ConvertFromForeign
	public static StylusInfo of(MemorySegment memory) {
		int tcid = (int) VH_TCID.get(memory, 0L);
		int stylusId = (int) VH_SID.get(memory, 0L);
		int isInverted = (int) VH_INVERT.get(memory, 0L);
		return new StylusInfo(tcid, stylusId, isInverted != 0);
	}

	@ConvertToForeign
	public MemorySegment asMemorySegment(Arena arena) {
		MemorySegment memory = arena.allocate(LAYOUT);
		VH_TCID.set(memory, 0L, tcid);
		VH_SID.set(memory, 0L, stylusId);
		VH_INVERT.set(memory, 0L, isInverted ? 1 : 0);
		return memory;
	}
}

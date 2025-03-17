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

/**
 * <p>
 * A property metrics of specific property. This contains logical range
 * (<em>inclusively</em>), its unit and resolution.
 * </p>
 * <p>
 * Resolution defines the division of physical unit mapped to logical range. For
 * example, if the lines per inch is 5080, the unit is
 * {@link PropertyUnit#CENTIMETERS} then the resolution is 20,000.
 * </p>
 */
@ForeignConvertible(MemorySegment.class)
public record PropertyMetrics(int logicalMin, int logicalMax, PropertyUnit unit, float resolution) {

	@ForeignConvertibleLayout
	public static final StructLayout LAYOUT = MemoryLayout.structLayout(
		ValueLayout.JAVA_INT.withName("logicalMin"),
		ValueLayout.JAVA_INT.withName("logicalMax"),
		ValueLayout.JAVA_INT.withName("unit"),
		ValueLayout.JAVA_FLOAT.withName("resolution"));

	private static final VarHandle VH_LMIN = LAYOUT.varHandle(PathElement.groupElement("logicalMin"));
	private static final VarHandle VH_LMAX = LAYOUT.varHandle(PathElement.groupElement("logicalMax"));
	private static final VarHandle VH_UNIT = LAYOUT.varHandle(PathElement.groupElement("unit"));
	private static final VarHandle VH_RES = LAYOUT.varHandle(PathElement.groupElement("resolution"));

	@ConvertFromForeign
	public static PropertyMetrics of(MemorySegment memory) {
		int logicalMin = (int) VH_LMIN.get(memory, 0L);
		int logicalMax = (int) VH_LMAX.get(memory, 0L);
		PropertyUnit unit = PropertyUnit.fromId((int) VH_UNIT.get(memory, 0L));
		float resolution = (float) VH_RES.get(memory, 0L);
		return new PropertyMetrics(logicalMin, logicalMax, unit, resolution);
	}

	@ConvertToForeign
	public MemorySegment asMemorySegment(Arena arena) {
		MemorySegment memory = arena.allocate(LAYOUT);
		setToMemorySegment(memory);
		return memory;
	}

	public void setToMemorySegment(MemorySegment memory) {
		VH_LMIN.set(memory, 0L, logicalMin);
		VH_LMAX.set(memory, 0L, logicalMax);
		VH_UNIT.set(memory, 0L, unit.getId());
		VH_RES.set(memory, 0L, resolution);
	}

	@Override
	public final String toString() {
		if (resolution == 0f) return "(%d -> %d)".formatted(logicalMin, logicalMax);
		else return "(%.2f%s -> %.2f%s)".formatted(
			logicalMin / resolution, unit.getShorthand(),
			logicalMax / resolution, unit.getShorthand());
	}
}

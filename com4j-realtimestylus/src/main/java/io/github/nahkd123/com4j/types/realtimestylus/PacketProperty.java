package io.github.nahkd123.com4j.types.realtimestylus;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;

import io.github.nahkd123.com4j.conversion.ConvertFromForeign;
import io.github.nahkd123.com4j.conversion.ConvertToForeign;
import io.github.nahkd123.com4j.conversion.ForeignConvertible;
import io.github.nahkd123.com4j.conversion.ForeignConvertibleLayout;
import io.github.nahkd123.com4j.win32.Guid;

@ForeignConvertible(MemorySegment.class)
public record PacketProperty(Guid guid, PropertyMetrics metrics) {
	@ForeignConvertibleLayout
	public static final StructLayout LAYOUT = MemoryLayout.structLayout(
		Guid.LAYOUT.withName("guid"),
		PropertyMetrics.LAYOUT.withName("metrics"));

	@ConvertFromForeign
	public static PacketProperty of(MemorySegment memory) {
		Guid guid = Guid.of(memory.asSlice(
			LAYOUT.byteOffset(PathElement.groupElement("guid")),
			Guid.LAYOUT));
		PropertyMetrics metrics = PropertyMetrics.of(memory.asSlice(
			LAYOUT.byteOffset(PathElement.groupElement("metrics")),
			PropertyMetrics.LAYOUT));
		return new PacketProperty(guid, metrics);
	}

	@ConvertToForeign
	public MemorySegment asMemorySegment(Arena arena) {
		MemorySegment memory = arena.allocate(LAYOUT);
		guid.setToMemorySegment(memory.asSlice(
			LAYOUT.byteOffset(PathElement.groupElement("guid")),
			Guid.LAYOUT));
		metrics.setToMemorySegment(memory.asSlice(
			LAYOUT.byteOffset(PathElement.groupElement("metrics")),
			PropertyMetrics.LAYOUT));
		return memory;
	}

	public PacketField field() {
		return PacketField.ofGuid(guid);
	}

	@Override
	public final String toString() {
		return "%s: %s".formatted(field() != null ? field() : guid, metrics);
	}
}

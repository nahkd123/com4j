package io.github.nahkd123.com4j.types.realtimestylus;

import java.util.List;

import io.github.nahkd123.com4j.itf.realtimestylus.IStylusPlugin;

/**
 * <p>
 * The ordering of properties list in this record reflects the ordering of
 * values when reading from
 * {@link IStylusPlugin#Packets(io.github.nahkd123.com4j.itf.realtimestylus.IRealTimeStylus, java.lang.foreign.MemorySegment, int, int, java.lang.foreign.MemorySegment, java.lang.foreign.MemorySegment, java.lang.foreign.MemorySegment)}.
 * </p>
 */
public record PacketDescription(int tcid, float scaleX, float scaleY, List<PacketProperty> properties) {
}

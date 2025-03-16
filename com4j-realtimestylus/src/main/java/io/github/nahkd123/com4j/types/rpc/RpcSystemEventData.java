package io.github.nahkd123.com4j.types.rpc;

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
public record RpcSystemEventData(byte bModifier, char wKey, int xPos, int yPos, byte bCursorMode, int dwButtonState) {

	@ForeignConvertibleLayout
	public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
		ValueLayout.JAVA_BYTE.withName("bModifier"),
		MemoryLayout.paddingLayout(1),
		ValueLayout.JAVA_CHAR.withName("wKey"),
		ValueLayout.JAVA_INT.withName("xPos"),
		ValueLayout.JAVA_INT.withName("yPos"),
		ValueLayout.JAVA_BYTE.withName("bCursorMode"),
		MemoryLayout.paddingLayout(3),
		ValueLayout.JAVA_INT.withName("dwButtonState"));

	private static final VarHandle VH_MODIFIER = LAYOUT.varHandle(PathElement.groupElement("bModifier"));
	private static final VarHandle VH_KEY = LAYOUT.varHandle(PathElement.groupElement("wKey"));
	private static final VarHandle VH_XPOS = LAYOUT.varHandle(PathElement.groupElement("xPos"));
	private static final VarHandle VH_YPOS = LAYOUT.varHandle(PathElement.groupElement("yPos"));
	private static final VarHandle VH_MODE = LAYOUT.varHandle(PathElement.groupElement("bCursorMode"));
	private static final VarHandle VH_STATE = LAYOUT.varHandle(PathElement.groupElement("dwButtonState"));

	@ConvertFromForeign
	public static RpcSystemEventData of(MemorySegment memory) {
		if (memory.byteSize() < LAYOUT.byteSize()) memory = memory.reinterpret(LAYOUT.byteSize());
		byte bModifier = (byte) VH_MODIFIER.get(memory, 0L);
		char wKey = (char) VH_KEY.get(memory, 0L);
		int xPos = (int) VH_XPOS.get(memory, 0L);
		int yPos = (int) VH_YPOS.get(memory, 0L);
		byte bCursorMode = (byte) VH_MODE.get(memory, 0L);
		int dwButtonState = (int) VH_STATE.get(memory, 0L);
		return new RpcSystemEventData(bModifier, wKey, xPos, yPos, bCursorMode, dwButtonState);
	}

	@ConvertToForeign
	public MemorySegment asMemorySegment(Arena arena) {
		MemorySegment memory = arena.allocate(LAYOUT);
		VH_MODIFIER.set(memory, 0L, bModifier);
		VH_KEY.set(memory, 0L, wKey);
		VH_XPOS.set(memory, 0L, xPos);
		VH_YPOS.set(memory, 0L, yPos);
		VH_MODE.set(memory, 0L, bCursorMode);
		VH_STATE.set(memory, 0L, dwButtonState);
		return memory;
	}
}

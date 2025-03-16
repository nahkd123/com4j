package io.github.nahkd123.com4j.win32;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.random.RandomGenerator;

import io.github.nahkd123.com4j.conversion.ConvertFromForeign;
import io.github.nahkd123.com4j.conversion.ConvertToForeign;
import io.github.nahkd123.com4j.conversion.ForeignConvertible;
import io.github.nahkd123.com4j.conversion.ForeignConvertibleLayout;

@ForeignConvertible(MemorySegment.class)
public record Guid(int data1, short data2, short data3, byte data40, byte data41, byte data42, byte data43, byte data44, byte data45, byte data46, byte data47) {

	@ForeignConvertibleLayout
	public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
		ValueLayout.JAVA_INT.withName("data1"),
		ValueLayout.JAVA_SHORT.withName("data2"),
		ValueLayout.JAVA_SHORT.withName("data3"),
		MemoryLayout.sequenceLayout(8L, ValueLayout.JAVA_BYTE).withName("data4"));

	private static final VarHandle VH_DATA1 = LAYOUT.varHandle(PathElement.groupElement("data1"));
	private static final VarHandle VH_DATA2 = LAYOUT.varHandle(PathElement.groupElement("data2"));
	private static final VarHandle VH_DATA3 = LAYOUT.varHandle(PathElement.groupElement("data3"));
	private static final VarHandle VH_DATA4 = LAYOUT.varHandle(
		PathElement.groupElement("data4"),
		PathElement.sequenceElement(0L, 1L));

	public static Guid of(int data1, short data2, short data3, byte[] data4) {
		if (data4.length < 8) throw new IllegalArgumentException("data4 length is less than 8");
		return new Guid(data1, data2, data3, data4[0], data4[1], data4[2], data4[3], data4[4], data4[5], data4[6], data4[7]);
	}

	@ConvertFromForeign
	public static Guid of(MemorySegment memory) {
		int data1 = (int) VH_DATA1.get(memory, 0L);
		short data2 = (short) VH_DATA2.get(memory, 0L);
		short data3 = (short) VH_DATA3.get(memory, 0L);
		byte[] data4 = new byte[8];
		for (int i = 0; i < 8; i++) data4[i] = (byte) VH_DATA4.get(memory, 0L, i);
		return of(data1, data2, data3, data4);
	}

	/**
	 * <p>
	 * Parse GUID from string. The following formats are supported:
	 * <ul>
	 * <li>4-dash format (00000000-0000-0000-0000-000000000000)</li>
	 * <li>4-dash format with curly brackets
	 * (&lbrace;00000000-0000-0000-0000-000000000000&rbrace;)</li>
	 * <li>No dash format (00000000000000000000000000000000)</li>
	 * <li>No dash format with curly bracket
	 * (&lbrace;00000000000000000000000000000000&rbrace;)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param guid String representation of GUID.
	 * @return The GUID, or {@code null} if parsing failed.
	 */
	public static Guid of(String guid) {
		if (guid.length() == 38 && guid.charAt(0) == '{' && guid.charAt(37) == '}') {
			return of(guid.substring(1, 37));
		}

		if (guid.length() == 34 && guid.charAt(0) == '{' && guid.charAt(33) == '}') {
			return of(guid.substring(1, 33));
		}

		if (guid.length() == 36) {
			char[] cs = guid.toCharArray();
			if (cs[8] == '-' && cs[13] == '-' && cs[18] == '-' && cs[23] == '-') {
				int data1 = hexToInt(cs, 0, 8);
				short data2 = (short) hexToInt(cs, 9, 4);
				short data3 = (short) hexToInt(cs, 14, 4);
				byte[] data4 = {
					(byte) hexToInt(cs, 19, 2),
					(byte) hexToInt(cs, 21, 2),
					(byte) hexToInt(cs, 24, 2),
					(byte) hexToInt(cs, 26, 2),
					(byte) hexToInt(cs, 28, 2),
					(byte) hexToInt(cs, 30, 2),
					(byte) hexToInt(cs, 32, 2),
					(byte) hexToInt(cs, 34, 2)
				};
				return of(data1, data2, data3, data4);
			}
		}

		if (guid.length() == 32) {
			char[] cs = guid.toCharArray();
			int data1 = hexToInt(cs, 0, 8);
			short data2 = (short) hexToInt(cs, 8, 4);
			short data3 = (short) hexToInt(cs, 12, 4);
			byte[] data4 = {
				(byte) hexToInt(cs, 16, 2),
				(byte) hexToInt(cs, 18, 2),
				(byte) hexToInt(cs, 20, 2),
				(byte) hexToInt(cs, 22, 2),
				(byte) hexToInt(cs, 24, 2),
				(byte) hexToInt(cs, 26, 2),
				(byte) hexToInt(cs, 28, 2),
				(byte) hexToInt(cs, 30, 2)
			};
			return of(data1, data2, data3, data4);
		}

		return null;
	}

	public static Guid createRandom(RandomGenerator rng) {
		int data1 = rng.nextInt();
		short data2 = (short) rng.nextInt(-32768, 32768);
		short data3 = (short) rng.nextInt(-32768, 32768);
		byte[] data4 = new byte[8];
		rng.nextBytes(data4);
		return of(data1, data2, data3, data4);
	}

	private static int hexToInt(char ch, int idx) {
		if (ch >= '0' && ch <= '9') return ch - '0';
		if (ch >= 'A' && ch <= 'F') return ch - 'A' + 10;
		if (ch >= 'a' && ch <= 'f') return ch - 'a' + 10;
		throw new IllegalArgumentException("Invalid hex character at index %d: %s".formatted(idx, ch));
	}

	private static int hexToInt(char[] cs, int off, int len) {
		int v = 0, baseShift;

		for (int i = 0; i < len; i += 2) {
			baseShift = (len - i - 2) * 4;
			v |= hexToInt(cs[off + i + 0], i + 0) << (baseShift + 4);
			v |= hexToInt(cs[off + i + 1], i + 1) << baseShift;
		}

		return v;
	}

	public byte[] data4(byte[] buf, int off) {
		if (off + 8 > buf.length) throw new IllegalArgumentException("off + 8 > buf.length");
		buf[0] = data40;
		buf[1] = data41;
		buf[2] = data42;
		buf[3] = data43;
		buf[4] = data44;
		buf[5] = data45;
		buf[6] = data46;
		buf[7] = data47;
		return buf;
	}

	public byte[] data4() {
		return data4(new byte[8], 0);
	}

	@ConvertToForeign
	public MemorySegment asMemorySegment(Arena arena) {
		MemorySegment memory = arena.allocate(LAYOUT);
		byte[] data4 = data4();
		VH_DATA1.set(memory, 0L, data1);
		VH_DATA2.set(memory, 0L, data2);
		VH_DATA3.set(memory, 0L, data3);
		for (int i = 0; i < 8; i++) VH_DATA4.set(memory, 0L, i, data4[i]);
		return memory;
	}

	@Override
	public final String toString() {
		return "%08x-%04x-%04x-%02x%02x-%02x%02x%02x%02x%02x%02x".formatted(
			data1, data2, data3,
			data40, data41, data42, data43, data44, data45, data46, data47);
	}
}

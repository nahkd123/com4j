package io.github.nahkd123.com4j.win32;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import io.github.nahkd123.com4j.conversion.ConvertFromForeign;
import io.github.nahkd123.com4j.conversion.ConvertToForeign;
import io.github.nahkd123.com4j.conversion.ForeignConvertible;

/**
 * <p>
 * Utility record for parsing HRESULT.
 * </p>
 * 
 * @see <a href=
 *      "https://learn.microsoft.com/en-us/windows/win32/com/structure-of-com-error-codes">Structure
 *      of COM Error Codes</a>
 */
@ForeignConvertible(int.class)
public class HResult {
	public static final HResult SUCCEED = new HResult(0);
	public static final HResult E_UNEXPECTED = new HResult(0x8000FFFF);
	public static final HResult E_NOTIMPL = new HResult(0x80004001);
	public static final HResult E_OUTOFMEMORY = new HResult(0x8007000E);
	public static final HResult E_INVALIDARG = new HResult(0x80070057);
	public static final HResult E_NOINTERFACE = new HResult(0x80004002);
	public static final HResult E_POINTER = new HResult(0x80004003);
	public static final HResult E_HANDLE = new HResult(0x80070006);
	public static final HResult E_ABORT = new HResult(0x80004004);
	public static final HResult E_FAIL = new HResult(0x80004005);
	public static final HResult E_ACCESSDENIED = new HResult(0x80070005);

	public static final ValueLayout.OfInt LAYOUT = ValueLayout.JAVA_INT.withName("HResult");
	public static final int S_CODE = 0x8_000_0000;
	public static final int R_CODE = 0x4_000_0000;
	public static final int C_CODE = 0x2_000_0000;
	public static final int N_CODE = 0x1_000_0000;
	public static final int r_CODE = 0x0_800_0000;
	public static final int FACILITY_MASK = 0x0_7FF_0000;
	public static final int CODE_MASK = 0x0_000_FFFF;

	private int value;
	private String message = null;

	@ConvertFromForeign
	public HResult(int value) {
		this.value = value;
	}

	public HResult(boolean failed, boolean R, boolean C, boolean N, boolean r, int facilityCode, int code) {
		this((failed ? S_CODE : 0) | (R ? R_CODE : 0) | (C ? C_CODE : 0) | (N ? N_CODE : 0) | (r ? r_CODE : 0) |
			((facilityCode << 16) & FACILITY_MASK) |
			(code & CODE_MASK));
	}

	public HResult(boolean failed, Facility facility, int code) {
		this(failed, false, false, false, false, facility.getCode(), code);
	}

	public static HResult ofSucceed() {
		return SUCCEED;
	}

	public static HResult ofFailed(Facility facility, int code) {
		return new HResult(true, facility, code);
	}

	@ConvertToForeign
	public int value() {
		return value;
	}

	public boolean succeed() {
		return (value & S_CODE) == 0;
	}

	@Deprecated
	public boolean R() {
		return (value & R_CODE) != 0;
	}

	@Deprecated
	public boolean C() {
		return (value & C_CODE) != 0;
	}

	@Deprecated
	public boolean N() {
		return (value & N_CODE) != 0;
	}

	@Deprecated
	public boolean r() {
		return (value & r_CODE) != 0;
	}

	public int facilityCode() {
		return (value & FACILITY_MASK) >> 16;
	}

	public Facility facility() {
		return Facility.valueOf(facilityCode());
	}

	public int code() {
		return value & CODE_MASK;
	}

	public void throwIfFail() {
		if (!succeed()) throw new Win32Exception(this);
	}

	private static final int FORMAT_MESSAGE_ALLOCATE_BUFFER = 0x00000100;
	private static final int FORMAT_MESSAGE_FROM_SYSTEM = 0x00001000;
	private static final int FORMAT_MESSAGE_IGNORE_INSERTS = 0x00000200;

	public String getMessage() {
		if (message != null) return message;

		try (Arena arena = Arena.ofConfined()) {
			Linker linker = Linker.nativeLinker();
			SymbolLookup kernel32 = SymbolLookup.libraryLookup("kernel32.dll", arena);

			// FormatMessageW
			MethodHandle formatMessage = linker.downcallHandle(
				kernel32.findOrThrow("FormatMessageW"),
				FunctionDescriptor.of(
					ValueLayout.JAVA_INT,
					ValueLayout.JAVA_INT.withName("dwFlags"),
					ValueLayout.ADDRESS.withName("lpSource"),
					ValueLayout.JAVA_INT.withName("dwMessageId"),
					ValueLayout.JAVA_INT.withName("dwLanguageId"),
					ValueLayout.ADDRESS.withName("lppBuffer"),
					ValueLayout.JAVA_INT.withName("nSize"),
					ValueLayout.ADDRESS.withName("arguments")));
			MemorySegment lppBuffer = arena.allocate(ValueLayout.ADDRESS);
			int stringSize = (int) formatMessage.invoke(
				FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
				MemorySegment.NULL,
				value,
				0x00 | (0x01 << 10), // 0x00: Neutral; 0x01: Default
				lppBuffer,
				0,
				MemorySegment.NULL);
			if (stringSize == 0) return null;

			// Extract string
			MemorySegment lpBuffer = lppBuffer.get(ValueLayout.ADDRESS, 0L).reinterpret((stringSize + 1) * 2);
			message = lpBuffer.getString(0L, StandardCharsets.UTF_16LE);

			// LocalFree
			MethodHandle localFree = linker.downcallHandle(
				kernel32.findOrThrow("LocalFree"),
				FunctionDescriptor.of(
					ValueLayout.ADDRESS,
					ValueLayout.ADDRESS));
			localFree.invoke(lpBuffer);
			return message;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public final String toString() {
		return succeed()
			? "Succeed"
			: "Failed(Facility = %s, Code = 0x%04x)".formatted(
				facility() != null
					? facility()
					: "0x%04x".formatted(facilityCode()),
				code());
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		HResult other = (HResult) obj;
		return value == other.value;
	}

	public static enum Facility {
		NULL(0),
		RPC(1),
		DISPATCH(2),
		STORAGE(3),
		ITF(4),
		WIN32(7),
		WINDOWS(8);

		private int code;

		private Facility(int code) {
			this.code = code;
		}

		public int getCode() { return code; }

		public static Facility valueOf(int code) {
			return switch (code) {
			case 0 -> NULL;
			case 1 -> RPC;
			case 2 -> DISPATCH;
			case 3 -> STORAGE;
			case 4 -> ITF;
			case 7 -> WIN32;
			case 8 -> WINDOWS;
			default -> null;
			};
		}
	}
}

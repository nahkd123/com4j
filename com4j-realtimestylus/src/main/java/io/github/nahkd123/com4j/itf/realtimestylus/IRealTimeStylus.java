package io.github.nahkd123.com4j.itf.realtimestylus;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.win32.HResult;

@ComInterface("A8BB5D22-3144-4a7b-93CD-F34A16BE513A")
public abstract class IRealTimeStylus extends IUnknown implements RealTimeStylus {
	public IRealTimeStylus(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}

	@ComMethod(index = 3)
	public abstract HResult get_Enabled(MemorySegment pfEnable);

	@ComMethod(index = 4)
	public abstract HResult put_Enabled(int fEnable);

	@ComMethod(index = 5)
	public abstract HResult get_HWND(MemorySegment phwnd);

	@ComMethod(index = 6)
	public abstract HResult put_HWND(MemorySegment hwnd);

	@ComMethod(index = 7)
	public abstract HResult get_WindowInputRectangle(MemorySegment prcWndInputRect);

	@ComMethod(index = 8)
	public abstract HResult put_WindowInputRectangle(MemorySegment prcWndInputRect);

	@ComMethod(index = 9)
	public abstract HResult AddStylusSyncPlugin(int iIndex, MemorySegment piPlugin);

	@ComMethod(index = 10)
	public abstract HResult RemoveStylusSyncPlugin(int iIndex, MemorySegment ppiPlugin);

	@ComMethod(index = 11)
	public abstract HResult RemoveAllStylusSyncPlugins();

	@ComMethod(index = 12)
	public abstract HResult GetStylusSyncPlugin(int iIndex, MemorySegment ppiPlugin);

	@ComMethod(index = 13)
	public abstract HResult GetStylusSyncPluginCount(MemorySegment pcPlugins);

	@ComMethod(index = 14)
	public abstract HResult AddStylusAsyncPlugin(int iIndex, MemorySegment piPlugin);

	@ComMethod(index = 15)
	public abstract HResult RemoveStylusAsyncPlugin(int iIndex, MemorySegment ppiPlugin);

	@ComMethod(index = 16)
	public abstract HResult RemoveAllStylusAsyncPlugins();

	@ComMethod(index = 17)
	public abstract HResult GetStylusAsyncPlugin(int iIndex, MemorySegment ppiPlugin);

	@ComMethod(index = 18)
	public abstract HResult GetStylusAsyncPluginCount(MemorySegment pcPlugins);

	@ComMethod(index = 19)
	public abstract HResult get_ChildRealTimeStylusPlugin(MemorySegment ppiRTS);

	@ComMethod(index = 20)
	public abstract HResult putref_ChildRealTimeStylusPlugin(MemorySegment piRTS);

	public boolean isEnabled() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment pfEnable = arena.allocate(ValueLayout.JAVA_INT);
			get_Enabled(pfEnable).throwIfFail();
			return pfEnable.get(ValueLayout.JAVA_INT, 0L) != 0;
		}
	}

	public void setEnable(boolean enable) {
		put_Enabled(enable ? 1 : 0).throwIfFail();
	}

	public long getHwnd() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment phwnd = arena.allocate(ValueLayout.ADDRESS);
			get_HWND(phwnd).throwIfFail();
			return phwnd.get(ValueLayout.ADDRESS, 0L).address();
		}
	}

	/**
	 * <p>
	 * Set target HWND of this {@link IRealTimeStylus}. The current process (a.k.a
	 * currently running JVM) must own the HWND of target window (usually by
	 * creating window).
	 * </p>
	 * 
	 * @param hwnd The HWND of window.
	 */
	public void setHwnd(long hwnd) {
		put_HWND(MemorySegment.ofAddress(hwnd)).throwIfFail();
	}

	public void addPlugin(int index, IStylusSyncPlugin plugin) {
		AddStylusSyncPlugin(index, plugin.getComPointer()).throwIfFail();
	}

	public void addPlugin(int index, IStylusAsyncPlugin plugin) {
		AddStylusAsyncPlugin(index, plugin.getComPointer()).throwIfFail();
	}
}

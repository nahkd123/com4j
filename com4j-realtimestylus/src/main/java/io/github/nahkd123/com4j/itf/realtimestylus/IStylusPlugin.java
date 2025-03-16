package io.github.nahkd123.com4j.itf.realtimestylus;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.win32.HResult;
import io.github.nahkd123.com4j.win32.Win32Exception;

@ComInterface("A81436D8-4757-4fd1-A185-133F97C6C545")
public abstract class IStylusPlugin extends IUnknown {
	public IStylusPlugin(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}

	@ComMethod(index = 3)
	public HResult RealTimeStylusEnabled(MemorySegment piRtsSrc, int cTcidCount, MemorySegment pTcids) {
		try {
			IRealTimeStylus rts = ComFactory.instance().wrap(piRtsSrc, IRealTimeStylus.class);
			pTcids = pTcids.reinterpret(ValueLayout.JAVA_INT.scale(0L, cTcidCount));
			int[] tcids = new int[cTcidCount];
			for (int i = 0; i < tcids.length; i++) tcids[i] = pTcids.getAtIndex(ValueLayout.JAVA_INT, i);
			onEnabled(rts, tcids);
			return HResult.SUCCEED;
		} catch (Win32Exception e) {
			return e.getHResult();
		} catch (Throwable t) {
			t.printStackTrace();
			return HResult.E_FAIL;
		}
	}

	@ComMethod(index = 4)
	public HResult RealTimeStylusDisabled(MemorySegment piRtsSrc, int cTcidCount, MemorySegment pTcids) {
		try {
			IRealTimeStylus rts = ComFactory.instance().wrap(piRtsSrc, IRealTimeStylus.class);
			pTcids = pTcids.reinterpret(ValueLayout.JAVA_INT.scale(0L, cTcidCount));
			int[] tcids = new int[cTcidCount];
			for (int i = 0; i < tcids.length; i++) tcids[i] = pTcids.getAtIndex(ValueLayout.JAVA_INT, i);
			onDisabled(rts, tcids);
			return HResult.SUCCEED;
		} catch (Win32Exception e) {
			return e.getHResult();
		} catch (Throwable t) {
			t.printStackTrace();
			return HResult.E_FAIL;
		}
	}

	@ComMethod(index = 5)
	public HResult StylusInRange(MemorySegment piRtsSrc, int tcid, int sid) {
		try {
			IRealTimeStylus rts = ComFactory.instance().wrap(piRtsSrc, IRealTimeStylus.class);
			onStylusInRange(rts, tcid, sid);
			return HResult.SUCCEED;
		} catch (Win32Exception e) {
			return e.getHResult();
		} catch (Throwable t) {
			t.printStackTrace();
			return HResult.E_FAIL;
		}
	}

	@ComMethod(index = 6)
	public HResult StylusOutOfRange(MemorySegment piRtsSrc, int tcid, int sid) {
		try {
			IRealTimeStylus rts = ComFactory.instance().wrap(piRtsSrc, IRealTimeStylus.class);
			onStylusOutOfRange(rts, tcid, sid);
			return HResult.SUCCEED;
		} catch (Win32Exception e) {
			return e.getHResult();
		} catch (Throwable t) {
			t.printStackTrace();
			return HResult.E_FAIL;
		}
	}

	public abstract void onEnabled(IRealTimeStylus rts, int[] tcids);

	public abstract void onDisabled(IRealTimeStylus rts, int[] tcids);

	public abstract void onStylusInRange(IRealTimeStylus rts, int tcid, int sid);

	public abstract void onStylusOutOfRange(IRealTimeStylus rts, int tcid, int sid);
}

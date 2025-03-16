package io.github.nahkd123.com4j.itf.realtimestylus;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Collection;
import java.util.Set;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.types.realtimestylus.RtsEvent;
import io.github.nahkd123.com4j.types.rpc.RpcSystemEventData;
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
			onRtsEnabled(rts, tcids);
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
			onRtsDisabled(rts, tcids);
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

	@ComMethod(index = 7)
	public HResult StylusDown(MemorySegment piRtsSrc, MemorySegment pStylusInfo, int cPropCountPerPkt, MemorySegment pPacket, MemorySegment ppInOutPkt) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 8)
	public HResult StylusUp(MemorySegment piRtsSrc, MemorySegment pStylusInfo, int cPropCountPerPkt, MemorySegment pPacket, MemorySegment ppInOutPkt) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 9)
	public HResult StylusButtonDown(MemorySegment piRtsSrc, int sid, MemorySegment pGuidStylusButton, MemorySegment pStylusPos) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 10)
	public HResult StylusButtonUp(MemorySegment piRtsSrc, int sid, MemorySegment pGuidStylusButton, MemorySegment pStylusPos) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 11)
	public HResult InAirPackets(MemorySegment piRtsSrc, MemorySegment pStylusInfo, int cPktCount, int cPktBuffLength, MemorySegment pPackets, MemorySegment pcInOutPkts, MemorySegment ppInOutPkts) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 12)
	public HResult Packets(MemorySegment piRtsSrc, MemorySegment pStylusInfo, int cPktCount, int cPktBuffLength, MemorySegment pPackets, MemorySegment pcInOutPkts, MemorySegment ppInOutPkts) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 13)
	public HResult CustomStylusDataAdded(MemorySegment piRtsSrc, MemorySegment pGuidId, int cbData, MemorySegment pbData) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 14)
	public HResult SystemEvent(MemorySegment piRtsSrc, int tcid, int sid, short event, RpcSystemEventData eventdata) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 15)
	public HResult TabletAdded(MemorySegment piRtsSrc, MemorySegment piTablet) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 16)
	public HResult TabletRemoved(MemorySegment piRtsSrc, long iTabletIndex) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 17)
	public HResult Error(MemorySegment piRtsSrc, MemorySegment piPlugin, int dataInterest, HResult hrErrorCode, MemorySegment lptrKey) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 19)
	public HResult DataInterest(MemorySegment pDataInterest) {
		try {
			pDataInterest
				.reinterpret(ValueLayout.JAVA_INT.byteSize())
				.set(ValueLayout.JAVA_INT, 0L, RtsEvent.toBitfield(getDataInterest()));
			return HResult.SUCCEED;
		} catch (Win32Exception e) {
			return e.getHResult();
		} catch (Throwable t) {
			t.printStackTrace();
			return HResult.E_FAIL;
		}
	}

	/**
	 * <p>
	 * Invoked when RealTimeStylus is enabled. Requires {@link RtsEvent#RtsEnabled}
	 * in {@link #getDataInterest()}.
	 * </p>
	 * 
	 * @param rts   The {@link IRealTimeStylus} instance.
	 * @param tcids A list of tablet context IDs.
	 */
	public void onRtsEnabled(IRealTimeStylus rts, int[] tcids) {}

	public void onRtsDisabled(IRealTimeStylus rts, int[] tcids) {}

	public void onStylusInRange(IRealTimeStylus rts, int tcid, int sid) {}

	public void onStylusOutOfRange(IRealTimeStylus rts, int tcid, int sid) {}

	/**
	 * <p>
	 * Get a collection of events that this stylus plugin is interested in
	 * collecting. You must override this with your own set of events if you want to
	 * listen (or not to listen) to specific event.
	 * </p>
	 * 
	 * @return A collection of events.
	 */
	public Collection<RtsEvent> getDataInterest() {
		return Set.of(
			RtsEvent.RtsEnabled,
			RtsEvent.RtsDisabled,
			RtsEvent.StylusDown,
			RtsEvent.Packets,
			RtsEvent.StylusUp,
			RtsEvent.SystemEvent,
			RtsEvent.CustomStylusDataAdded);
	}
}

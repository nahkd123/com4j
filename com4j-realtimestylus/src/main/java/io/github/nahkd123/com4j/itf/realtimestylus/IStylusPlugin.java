package io.github.nahkd123.com4j.itf.realtimestylus;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.util.Collection;
import java.util.Set;

import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.types.realtimestylus.Packet;
import io.github.nahkd123.com4j.types.realtimestylus.PacketsIO;
import io.github.nahkd123.com4j.types.realtimestylus.RtsEvent;
import io.github.nahkd123.com4j.types.realtimestylus.StylusInfo;
import io.github.nahkd123.com4j.types.rpc.RpcSystemEventData;
import io.github.nahkd123.com4j.win32.HResult;
import io.github.nahkd123.com4j.win32.Win32Exception;

@ComInterface("A81436D8-4757-4fd1-A185-133F97C6C545")
public abstract class IStylusPlugin extends IUnknown {
	public IStylusPlugin(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}

	@ComMethod(index = 3)
	public HResult RealTimeStylusEnabled(IRealTimeStylus rts, int cTcidCount, MemorySegment pTcids) {
		try {
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
	public HResult RealTimeStylusDisabled(IRealTimeStylus rts, int cTcidCount, MemorySegment pTcids) {
		try {
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
	public HResult StylusInRange(IRealTimeStylus rts, int tcid, int sid) {
		try {
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
	public HResult StylusOutOfRange(IRealTimeStylus rts, int tcid, int sid) {
		try {
			onStylusOutOfRange(rts, tcid, sid);
			return HResult.SUCCEED;
		} catch (Win32Exception e) {
			return e.getHResult();
		} catch (Throwable t) {
			t.printStackTrace();
			return HResult.E_FAIL;
		}
	}

	private HResult applyPacketEvents(IRealTimeStylus rts, MemorySegment pStylusInfo, int cPktCount, int cPktBuffLength, MemorySegment pPackets, MemorySegment pcInOutPkts, MemorySegment ppInOutPkts, PacketEventCallback callback) {
		try {
			StylusInfo stylus = StylusInfo.of(pStylusInfo.reinterpret(StylusInfo.LAYOUT.byteSize()));
			PacketsIO io = new PacketsIOImpl(cPktCount, cPktBuffLength, pPackets);
			callback.apply(rts, stylus, io);
			// TODO output
			pPackets
				.reinterpret(ValueLayout.JAVA_INT.byteSize())
				.set(ValueLayout.JAVA_INT, 0L, 0);
			ppInOutPkts
				.reinterpret(ValueLayout.ADDRESS.byteSize())
				.set(ValueLayout.ADDRESS, 0L, MemorySegment.NULL);
			return HResult.SUCCEED;
		} catch (Throwable t) {
			t.printStackTrace();
			return HResult.E_FAIL;
		}
	}

	@ComMethod(index = 7)
	public HResult StylusDown(IRealTimeStylus rts, MemorySegment pStylusInfo, int cPropCountPerPkt, MemorySegment pPacket, MemorySegment ppInOutPkt) {
		return applyPacketEvents(
			rts, pStylusInfo,
			1, cPropCountPerPkt, pPacket,
			null, ppInOutPkt,
			this::onStylusUpPacket);
	}

	@ComMethod(index = 8)
	public HResult StylusUp(IRealTimeStylus rts, MemorySegment pStylusInfo, int cPropCountPerPkt, MemorySegment pPacket, MemorySegment ppInOutPkt) {
		return applyPacketEvents(
			rts, pStylusInfo,
			1, cPropCountPerPkt, pPacket,
			null, ppInOutPkt,
			this::onStylusDownPacket);
	}

	@ComMethod(index = 9)
	public HResult StylusButtonDown(IRealTimeStylus rts, int sid, MemorySegment pGuidStylusButton, MemorySegment pStylusPos) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 10)
	public HResult StylusButtonUp(IRealTimeStylus rts, int sid, MemorySegment pGuidStylusButton, MemorySegment pStylusPos) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 11)
	public HResult InAirPackets(IRealTimeStylus rts, MemorySegment pStylusInfo, int cPktCount, int cPktBuffLength, MemorySegment pPackets, MemorySegment pcInOutPkts, MemorySegment ppInOutPkts) {
		return applyPacketEvents(
			rts, pStylusInfo,
			cPktCount, cPktBuffLength, pPackets,
			pcInOutPkts, ppInOutPkts,
			this::onAirPackets);
	}

	@ComMethod(index = 12)
	public HResult Packets(IRealTimeStylus rts, MemorySegment pStylusInfo, int cPktCount, int cPktBuffLength, MemorySegment pPackets, MemorySegment pcInOutPkts, MemorySegment ppInOutPkts) {
		return applyPacketEvents(
			rts, pStylusInfo,
			cPktCount, cPktBuffLength, pPackets,
			pcInOutPkts, ppInOutPkts,
			this::onPackets);
	}

	@ComMethod(index = 13)
	public HResult CustomStylusDataAdded(IRealTimeStylus rts, MemorySegment pGuidId, int cbData, MemorySegment pbData) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 14)
	public HResult SystemEvent(IRealTimeStylus rts, int tcid, int sid, short event, RpcSystemEventData eventdata) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 15)
	public HResult TabletAdded(IRealTimeStylus rts, IInkTablet tablet) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 16)
	public HResult TabletRemoved(IRealTimeStylus rts, long iTabletIndex) {
		// TODO
		return HResult.SUCCEED;
	}

	@ComMethod(index = 17)
	public HResult Error(IRealTimeStylus rts, MemorySegment piPlugin, int dataInterest, HResult hrErrorCode, MemorySegment lptrKey) {
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

	public void onStylusUpPacket(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {}

	public void onStylusDownPacket(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {}

	public void onPackets(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {}

	public void onAirPackets(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {}

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

	@FunctionalInterface
	private static interface PacketEventCallback {
		void apply(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io);
	}

	private static class PacketsIOImpl implements PacketsIO {
		private int inputs;
		private int sizePerPacket;
		private MemorySegment inputBuf;
		private SequenceLayout layout;

		public PacketsIOImpl(int inputs, int bufLen, MemorySegment inputBuf) {
			this.inputs = inputs;
			this.inputBuf = inputBuf.reinterpret(MemoryLayout
				.sequenceLayout(bufLen, ValueLayout.JAVA_INT)
				.byteSize());
			this.sizePerPacket = bufLen / inputs;
			this.layout = MemoryLayout.sequenceLayout(sizePerPacket, ValueLayout.JAVA_INT);
		}

		@Override
		public int getInputCount() { return inputs; }

		@Override
		public Packet getInput(int index) {
			if (index < 0 || index >= inputs) throw new IndexOutOfBoundsException();
			MemorySegment packetBuf = inputBuf.asSlice(layout.byteSize() * index, layout);
			return new MappedPacketImpl(packetBuf, sizePerPacket);
		}
	}

	private static class MappedPacketImpl implements Packet {
		private MemorySegment memory;
		private int size;

		public MappedPacketImpl(MemorySegment memory, int size) {
			this.memory = memory.reinterpret(MemoryLayout.sequenceLayout(size, ValueLayout.JAVA_INT).byteSize());
			this.size = size;
		}

		@Override
		public int get(int index) {
			if (index < 0 || index >= size) throw new IndexOutOfBoundsException();
			return memory.get(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT.byteSize() * index);
		}

		@Override
		public int size() {
			return size;
		}
	}
}

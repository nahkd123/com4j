package io.github.nahkd123.com4j.types.realtimestylus;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum RtsEvent {
	// @formatter:off
	Error						(0b00000000000000001),
	RtsEnabled					(0b00000000000000010),
	RtsDisabled					(0b00000000000000100),
	StylusNew					(0b00000000000001000),
	StylusInRange				(0b00000000000010000),
	InAirPackets				(0b00000000000100000),
	StylusOutOfRange			(0b00000000001000000),
	StylusDown					(0b00000000010000000),
	Packets						(0b00000000100000000),
	StylusUp					(0b00000001000000000),
	StylusButtonUp				(0b00000010000000000),
	StylusButtonDown			(0b00000100000000000),
	SystemEvent				(0b00001000000000000),
	TabletAdded					(0b00010000000000000),
	TabletRemoved				(0b00100000000000000),
	CustomStylusDataAdded		(0b01000000000000000),
	UpdateMapping				(0b10000000000000000);
	// @formatter:on

	private int bitflag;

	private RtsEvent(int bitflag) {
		this.bitflag = bitflag;
	}

	public static Set<RtsEvent> fromBitfield(int bitfield) {
		Set<RtsEvent> set = new HashSet<>();
		for (RtsEvent rts : values())
			if ((bitfield & rts.bitflag) != 0) set.add(rts);
		return set;
	}

	public static int toBitfield(Collection<RtsEvent> coll) {
		int v = 0;
		for (RtsEvent event : coll) v |= event.bitflag;
		return v;
	}
}

package io.github.nahkd123.com4j.types.realtimestylus;

public interface PacketOut extends Packet {
	/**
	 * <p>
	 * Set logical value of property in this packet.
	 * </p>
	 * 
	 * @param index The index of property.
	 * @param value The logical value.
	 */
	void set(int index, int value);
}

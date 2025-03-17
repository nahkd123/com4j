package io.github.nahkd123.com4j.types.realtimestylus;

public interface Packet {
	/**
	 * <p>
	 * Get logical value at specific index from this packet. The index corresponding
	 * to property from {@link PacketDescription}.
	 * </p>
	 * 
	 * @param index The property index.
	 * @return The logical value.
	 */
	int get(int index);

	/**
	 * <p>
	 * Get the number of property values stored in this packet.
	 * </p>
	 * 
	 * @return Number of values.
	 */
	int size();
}

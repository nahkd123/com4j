package io.github.nahkd123.com4j.types.realtimestylus;

/**
 * <p>
 * An interface for reading and writing packets.
 * </p>
 */
public interface PacketsIO {
	/**
	 * <p>
	 * Get number of input packets coming from this IO.
	 * </p>
	 * 
	 * @return The number of input packets.
	 */
	int getInputCount();

	Packet getInput(int index);
}

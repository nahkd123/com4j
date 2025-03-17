package io.github.nahkd123.com4j.types.realtimestylus;

public enum StylusQueue {
	SYNC(1),
	ASYNC_IMMEDIATE(2),
	ASYNC(3);

	private int id;

	private StylusQueue(int id) {
		this.id = id;
	}

	public int getId() { return id; }
}

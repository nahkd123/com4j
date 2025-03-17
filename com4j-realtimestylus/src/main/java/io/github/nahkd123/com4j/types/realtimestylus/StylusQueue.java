package io.github.nahkd123.com4j.types.realtimestylus;

import io.github.nahkd123.com4j.conversion.ConvertFromForeign;
import io.github.nahkd123.com4j.conversion.ConvertToForeign;
import io.github.nahkd123.com4j.conversion.ForeignConvertible;

@ForeignConvertible(int.class)
public enum StylusQueue {
	SYNC(1),
	ASYNC_IMMEDIATE(2),
	ASYNC(3);

	private int id;

	private StylusQueue(int id) {
		this.id = id;
	}

	@ConvertToForeign
	public int getId() { return id; }

	@ConvertFromForeign
	public static StylusQueue fromId(int id) {
		return switch (id) {
		case 1 -> SYNC;
		case 2 -> ASYNC_IMMEDIATE;
		case 3 -> ASYNC;
		default -> throw new IllegalArgumentException("Invalid StylusQueue enum ID: %d".formatted(id));
		};
	}
}

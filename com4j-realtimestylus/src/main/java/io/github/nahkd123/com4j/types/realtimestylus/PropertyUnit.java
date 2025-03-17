package io.github.nahkd123.com4j.types.realtimestylus;

import io.github.nahkd123.com4j.conversion.ConvertFromForeign;
import io.github.nahkd123.com4j.conversion.ConvertToForeign;
import io.github.nahkd123.com4j.conversion.ForeignConvertible;

@ForeignConvertible(int.class)
public enum PropertyUnit {
	DEFAULT(0, ""),
	INCHES(1, "in"),
	CENTIMETERS(2, "cm"),
	DEGREES(3, "deg"),
	RADIANS(4, "rad"),
	SECONDS(5, "s"),
	POUNDS(6, "lb"),
	GRAMS(7, "g"),
	SI_LINEAR(8, " si linear"),
	SI_ROTATION(9, " si rotation"),
	ENG_LINEAR(10, " eng linear"),
	ENG_ROTATION(11, " eng rotation"),
	SLUGS(12, " slugs"),
	KELVIN(13, "K"),
	FAHRENHEIT(14, "F"),
	AMPERE(15, "A");

	private int id;
	private String shorthand;

	private PropertyUnit(int id, String shorthand) {
		this.id = id;
		this.shorthand = shorthand;
	}

	@ConvertToForeign
	public int getId() { return id; }

	public String getShorthand() { return shorthand; }

	@ConvertFromForeign
	public static PropertyUnit fromId(int id) {
		return switch (id) {
		case 0 -> DEFAULT;
		case 1 -> INCHES;
		case 2 -> CENTIMETERS;
		case 3 -> DEGREES;
		case 4 -> RADIANS;
		case 5 -> SECONDS;
		case 6 -> POUNDS;
		case 7 -> GRAMS;
		case 8 -> SI_LINEAR;
		case 9 -> SI_ROTATION;
		case 10 -> ENG_LINEAR;
		case 11 -> ENG_ROTATION;
		case 12 -> SLUGS;
		case 13 -> KELVIN;
		case 14 -> FAHRENHEIT;
		case 15 -> AMPERE;
		default -> throw new IllegalArgumentException("Invalid PropertyUnit enum ID: %d".formatted(id));
		};
	}
}

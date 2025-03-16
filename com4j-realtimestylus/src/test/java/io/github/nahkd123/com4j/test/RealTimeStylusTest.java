package io.github.nahkd123.com4j.test;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.itf.realtimestylus.IRealTimeStylus;
import io.github.nahkd123.com4j.itf.realtimestylus.RealTimeStylus;

public class RealTimeStylusTest {
	public static void main(String[] args) {
		ComFactory com = ComFactory.instance();
		IRealTimeStylus rts = com.createFromClsid(IRealTimeStylus.class, RealTimeStylus.CLSID);
		rts.setEnable(false);
		rts.Release();
	}
}

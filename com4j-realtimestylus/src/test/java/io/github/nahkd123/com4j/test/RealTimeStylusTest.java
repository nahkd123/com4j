package io.github.nahkd123.com4j.test;

import java.lang.foreign.MemorySegment;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.itf.realtimestylus.IInkTablet;
import io.github.nahkd123.com4j.itf.realtimestylus.IRealTimeStylus;
import io.github.nahkd123.com4j.itf.realtimestylus.IStylusAsyncPlugin;
import io.github.nahkd123.com4j.itf.realtimestylus.RealTimeStylus;
import io.github.nahkd123.com4j.types.realtimestylus.Packet;
import io.github.nahkd123.com4j.types.realtimestylus.PacketDescription;
import io.github.nahkd123.com4j.types.realtimestylus.PacketField;
import io.github.nahkd123.com4j.types.realtimestylus.PacketProperty;
import io.github.nahkd123.com4j.types.realtimestylus.PacketsIO;
import io.github.nahkd123.com4j.types.realtimestylus.RtsEvent;
import io.github.nahkd123.com4j.types.realtimestylus.StylusInfo;

public class RealTimeStylusTest {
	public static void main(String[] args) {
		long window = glfwInit();
		long hwnd = GLFWNativeWin32.glfwGetWin32Window(window);
		System.out.println("HWND of GLFW window is 0x%016x".formatted(hwnd));

		System.out.println("Creating RealTimeStylus...");
		ComFactory com = ComFactory.instance();
		IRealTimeStylus rts = com.createFromClsid(IRealTimeStylus.class, RealTimeStylus.CLSID);

		rts.configureMultipleTablets(false);
		rts.setDesiredFields(Set.of(
			PacketField.X,
			PacketField.Y,
			PacketField.PACKET_STATUS,
			PacketField.NORMAL_PRESSURE,
			PacketField.X_TILT_ORIENTATION,
			PacketField.Y_TILT_ORIENTATION));
		rts.setHwnd(hwnd);

		System.out.println("  Creating stylus plugin...");
		MyStylusPlugin plugin = (MyStylusPlugin) com.createJava(IStylusAsyncPlugin.class, MyStylusPlugin::new);
		rts.addAsyncPlugin(0, plugin);
		plugin.Release();

		System.out.println("  Enabling RealTimeStylus...");
		rts.setEnable(true);

		GL.createCapabilities();
		GL20.glClearColor(0f, 0f, 0f, 1f);

		double[] xCur = { 0 };
		double[] yCur = { 0 };
		int[] width = { 0 };
		int[] height = { 0 };
		float[] lastDataPoint = { 0f, 0f, 0f };

		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwGetWindowSize(window, width, height);
			GL20.glMatrixMode(GL20.GL_PROJECTION);
			GL20.glLoadIdentity();
			GL20.glOrtho(0, width[0], height[0], 0, 0, 1);

			GL20.glEnable(GL20.GL_BLEND);
			GL20.glDisable(GL20.GL_DEPTH_TEST);
			GL20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			GL20.glColor4f(0f, 0f, 0f, 0.1f);
			GL20.glBegin(GL20.GL_QUADS);
			GL20.glVertex2i(0, 0);
			GL20.glVertex2i(width[0], 0);
			GL20.glVertex2i(width[0], height[0]);
			GL20.glVertex2i(0, height[0]);
			GL20.glEnd();

			GL20.glColor3f(1f, 0f, 0f);
			GL20.glBegin(GL20.GL_LINES);
			GL20.glVertex2d(xCur[0], yCur[0]);
			GLFW.glfwGetCursorPos(window, xCur, yCur);
			GL20.glVertex2d(xCur[0], yCur[0]);
			GL20.glEnd();

			while (!plugin.dataPoints.isEmpty()) {
				float[] next = plugin.dataPoints.poll();

				GL20.glColor3f(1f, 1f, 1f);
				GL20.glBegin(GL20.GL_LINES);
				GL20.glVertex2f(lastDataPoint[0], lastDataPoint[1]);
				GL20.glVertex2f(next[0], next[1]);
				GL20.glEnd();

				GL20.glBegin(GL20.GL_QUADS);
				GL20.glVertex2f(next[0] - 5, next[1] - 5);
				GL20.glVertex2f(next[0] + 5, next[1] - 5);
				GL20.glVertex2f(next[0] + 5, next[1] + 5);
				GL20.glVertex2f(next[0] - 5, next[1] + 5);
				GL20.glEnd();

				lastDataPoint = next;
			}

			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}

		System.out.println("Releasing RealTimeStylus...");
		rts.setEnable(false);
		rts.Release();
		glfwDestroy(window);
	}

	private static class MyStylusPlugin extends IStylusAsyncPlugin {
		private Queue<float[]> dataPoints = new ConcurrentLinkedQueue<float[]>();

		public MyStylusPlugin(MemorySegment comPtr, Runnable destroyCallback) {
			super(comPtr, destroyCallback);
		}

		@Override
		public Collection<RtsEvent> getDataInterest() {
			return Set.of(
				RtsEvent.RtsEnabled,
				RtsEvent.RtsDisabled,
				RtsEvent.StylusDown,
				RtsEvent.StylusUp,
				RtsEvent.Packets,
				RtsEvent.InAirPackets);
		}

		@Override
		public void onRtsEnabled(IRealTimeStylus rts, int[] tcids) {
			System.out.println("RealTimeStylus enabled with %d tablet contexts!".formatted(tcids.length));

			for (int i = 0; i < tcids.length; i++) {
				int tcid = tcids[i];
				IInkTablet tablet = rts.getTabletFromContextId(tcid);
				System.out.println("  Tablet #%d".formatted(i + 1));
				System.out.println("    Name: %s (PnP: %s)".formatted(
					tablet.getName(), tablet.getPlugAndPlayId()));

				PacketDescription desc = rts.getPacketDescription(tcid);
				System.out.println("    Properties: %s".formatted(desc.properties()));

				tablet.Release();
			}
		}

		@Override
		public void onRtsDisabled(IRealTimeStylus rts, int[] tcids) {
			System.out.println("RealTimeStylus disabled!");
		}

		@Override
		public void onStylusDownPacket(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {
			System.out.println("Received %d pen down packets".formatted(io.getInputCount()));
			printPackets(rts, stylus, io);
		}

		@Override
		public void onStylusUpPacket(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {
			System.out.println("Received %d pen up packets".formatted(io.getInputCount()));
			printPackets(rts, stylus, io);
		}

		@Override
		public void onPackets(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {
			System.out.println("Received %d packets".formatted(io.getInputCount()));
			printPackets(rts, stylus, io);
		}

		@Override
		public void onAirPackets(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {
			System.out.println("Received %d hover packets".formatted(io.getInputCount()));
			printPackets(rts, stylus, io);
		}

		private void printPackets(IRealTimeStylus rts, StylusInfo stylus, PacketsIO io) {
			PacketDescription desc = rts.getPacketDescription(stylus.tcid());

			for (int i = 0; i < io.getInputCount(); i++) {
				Packet packet = io.getInput(i);
				float[] dataPoint = { 0f, 0f, 0f };
				float scale = 144f / 2540f;
				System.out.println("  Packet #%d:".formatted(i + 1));

				for (int j = 0; j < packet.size(); j++) {
					PacketProperty prop = desc.properties().get(j);
					System.out.println("    #%d. %s = %d".formatted(j + 1, prop.field(), packet.get(j)));
					if (prop.field() == PacketField.X) dataPoint[0] = packet.get(j) * scale;
					if (prop.field() == PacketField.Y) dataPoint[1] = packet.get(j) * scale;
				}

				dataPoints.add(dataPoint);
			}
		}
	}

	private static long glfwInit() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) throw new IllegalStateException("Failed to init GLFW");

		GLFW.glfwDefaultWindowHints();

		long window = GLFW.glfwCreateWindow(
			1000, 1000, "COM4J RealTimeStylus Demo",
			MemoryUtil.NULL,
			MemoryUtil.NULL);
		if (window == MemoryUtil.NULL) throw new RuntimeException("Failed to create GLFW window");

		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
		GLFW.glfwShowWindow(window);
		return window;
	}

	private static void glfwDestroy(long window) {
		Callbacks.glfwFreeCallbacks(window);
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}
}

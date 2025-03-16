package io.github.nahkd123.com4j.test;

import java.lang.foreign.MemorySegment;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.MemoryUtil;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.itf.realtimestylus.IRealTimeStylus;
import io.github.nahkd123.com4j.itf.realtimestylus.IStylusAsyncPlugin;
import io.github.nahkd123.com4j.itf.realtimestylus.RealTimeStylus;

public class RealTimeStylusTest {
	public static void main(String[] args) {
		long window = glfwInit();
		long hwnd = GLFWNativeWin32.glfwGetWin32Window(window);
		System.out.println("HWND of GLFW window is 0x%016x".formatted(hwnd));

		System.out.println("Creating RealTimeStylus...");
		ComFactory com = ComFactory.instance();
		IRealTimeStylus rts = com.createFromClsid(IRealTimeStylus.class, RealTimeStylus.CLSID);
		rts.setHwnd(hwnd);

		System.out.println("  Creating stylus plugin...");
		IStylusAsyncPlugin plugin = com.createJava(IStylusAsyncPlugin.class, MyStylusPlugin::new);
		rts.addPlugin(0, plugin);
		plugin.Release();

		System.out.println("  Enabling RealTimeStylus...");
		rts.setEnable(true);

		while (!GLFW.glfwWindowShouldClose(window)) {
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}

		System.out.println("Releasing RealTimeStylus...");
		rts.setEnable(false);
		rts.Release();
		glfwDestroy(window);
	}

	private static class MyStylusPlugin extends IStylusAsyncPlugin {
		public MyStylusPlugin(MemorySegment comPtr, Runnable destroyCallback) {
			super(comPtr, destroyCallback);
		}

		@Override
		public void onRtsEnabled(IRealTimeStylus rts, int[] tcids) {
			System.out.println("RealTimeStylus enabled with %d tablet contexts!".formatted(tcids.length));
		}

		@Override
		public void onRtsDisabled(IRealTimeStylus rts, int[] tcids) {
			System.out.println("RealTimeStylus disabled!");
		}
	}

	private static long glfwInit() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) throw new IllegalStateException("Failed to init GLFW");

		GLFW.glfwDefaultWindowHints();

		long window = GLFW.glfwCreateWindow(
			500, 500, "COM4J RealTimeStylus Demo",
			MemoryUtil.NULL,
			MemoryUtil.NULL);
		if (window == MemoryUtil.NULL) throw new RuntimeException("Failed to create GLFW window");

		GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
			// TODO handle keys
		});

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

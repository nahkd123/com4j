package io.github.nahkd123.com4j.impl;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.nahkd123.com4j.Bstr;
import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.ComUtils;
import io.github.nahkd123.com4j.impl.wrapper.ComWrapperInfo;
import io.github.nahkd123.com4j.impl.wrapper.Java2ComVtbl;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.win32.Guid;
import io.github.nahkd123.com4j.win32.HResult;
import io.github.nahkd123.com4j.wrapper.JavaToComFactory;

public class ComFactoryImpl implements ComFactory {
	private static final int CLSCTX_INPROC_SERVER = 0x1;
	private static final int CLSCTX_INPROC_HANDLER = 0x2;
	private static final int CLSCTX_LOCAL_SERVER = 0x4;
	private static final int CLSCTX_REMOTE_SERVER = 0x10;
	private static final int CLSCTX_ALL = 0
		| CLSCTX_INPROC_SERVER
		| CLSCTX_INPROC_HANDLER
		| CLSCTX_LOCAL_SERVER
		| CLSCTX_REMOTE_SERVER;

	private static ComFactoryImpl instance = null;

	private Arena factoryArena;
	private Linker linker;
	private SymbolLookup ole32;
	private BstrImpl bstr;

	private MethodHandle CoInitialize;
	private MethodHandle CoCreateInstance;

	// Wrapping
	private Map<Class<?>, Java2ComVtbl> java2ComVtbls = new ConcurrentHashMap<>();
	private Map<MemorySegment, IUnknown> java2ComPtr2Obj = new ConcurrentHashMap<>();
	private Map<Class<?>, MethodHandle> java2ComCtor = new ConcurrentHashMap<>();
	private Map<Class<? extends IUnknown>, ComWrapperInfo<?>> com2JavaWrapperInfo = new ConcurrentHashMap<>();

	public ComFactoryImpl(Arena factoryArena, Linker linker) throws Throwable {
		this.factoryArena = factoryArena;
		this.linker = linker;
		// kernel32 = SymbolLookup.libraryLookup("kernel32.dll", factoryArena);
		ole32 = SymbolLookup.libraryLookup("ole32.dll", factoryArena);
		bstr = new BstrImpl(factoryArena, linker, ole32);

		CoInitialize = linker.downcallHandle(ole32.findOrThrow("CoInitialize"), FunctionDescriptor.of(
			HResult.LAYOUT,
			ValueLayout.ADDRESS.withName("reserved")));
		new HResult((int) CoInitialize.invoke(MemorySegment.NULL)).throwIfFail();

		CoCreateInstance = linker.downcallHandle(ole32.findOrThrow("CoCreateInstance"), FunctionDescriptor.of(
			HResult.LAYOUT,
			Guid.LAYOUT.withName("rclsid"),
			ValueLayout.ADDRESS.withName("pUnkOuter"),
			ValueLayout.JAVA_INT.withName("dwClsContext"),
			Guid.LAYOUT.withName("riid"),
			ValueLayout.ADDRESS.withName("ppV")));
	}

	public static ComFactoryImpl getInstance() {
		if (instance == null) {
			try {
				instance = new ComFactoryImpl(Arena.ofAuto(), Linker.nativeLinker());
			} catch (Throwable e) {
				throw new RuntimeException("Unable to create ComFactory", e);
			}
		}

		return instance;
	}

	@Override
	public Bstr bstr() {
		return bstr;
	}

	@Override
	public MemorySegment createFromClsid(Guid iid, Guid clsid) {
		try (Arena localArena = Arena.ofConfined()) {
			MemorySegment ppV = localArena.allocate(ValueLayout.ADDRESS);
			new HResult((int) CoCreateInstance.invoke(
				clsid.asMemorySegment(localArena),
				MemorySegment.NULL,
				CLSCTX_ALL,
				iid.asMemorySegment(localArena),
				ppV)).throwIfFail();
			return ppV.get(ValueLayout.ADDRESS, 0L).reinterpret(ValueLayout.ADDRESS.byteSize());
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IUnknown> T wrap(MemorySegment comPtr, Class<T> interfaceType) {
		ComWrapperInfo<T> wrapperInfo = (ComWrapperInfo<T>) com2JavaWrapperInfo.computeIfAbsent(interfaceType, _ -> {
			try {
				return ComWrapperInfo.create(interfaceType);
			} catch (Throwable e) {
				throw new RuntimeException("Unable to create wrapper info for %s".formatted(interfaceType), e);
			}
		});
		try {
			return wrapperInfo.wrap(comPtr, linker);
		} catch (Throwable e) {
			throw new RuntimeException("Unable to wrap %s".formatted(interfaceType), e);
		}
	}

	private IUnknown lookupWrapped(MemorySegment comPtr) {
		IUnknown java = java2ComPtr2Obj.get(comPtr);
		if (java == null) throw new RuntimeException("%s is no longer mapped to any Java object".formatted(comPtr));
		return java;
	}

	@Override
	public <T extends IUnknown> T createJava(Class<T> interfaceType, JavaToComFactory<T> ctor) {
		Java2ComVtbl vtblInfo = java2ComVtbls.computeIfAbsent(interfaceType, _ -> Java2ComVtbl.create(
			interfaceType,
			factoryArena,
			linker,
			this::lookupWrapped));
		MemoryLayout comLayout = MemoryLayout.structLayout(
			ValueLayout.ADDRESS.withName("vtbl").withTargetLayout(vtblInfo.layout()));
		MemorySegment com = factoryArena.allocate(comLayout);
		comLayout.varHandle(PathElement.groupElement("vtbl")).set(com, 0L, vtblInfo.struct());
		T obj = ctor.create(com, () -> java2ComPtr2Obj.remove(com));
		java2ComPtr2Obj.put(com, obj);
		return obj;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T extends IUnknown> T createJava(Class<T> classType) {
		try {
			MethodHandle ctor = java2ComCtor.computeIfAbsent(classType, clazz -> {
				try {
					return MethodHandles.lookup().findConstructor(
						clazz,
						MethodType.methodType(void.class, MemorySegment.class, Runnable.class));
				} catch (NoSuchMethodException e) {
					throw new IllegalArgumentException("%s does not have <init>(MemorySegment,Runnable)%s"
						.formatted(clazz.getName(), clazz.getSimpleName()), e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("%s: <init>(MemorySegment,Runnable)%s is possibly not visible"
						.formatted(clazz.getName(), clazz.getSimpleName()), e);
				}
			});
			return (T) createJava(
				(Class) ComUtils.findComInterface(classType),
				(comPtr, destroyCallback) -> {
					try {
						return (T) ctor.invoke(comPtr, destroyCallback);
					} catch (Throwable e) {
						throw new RuntimeException("Failed to create");
					}
				});
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}

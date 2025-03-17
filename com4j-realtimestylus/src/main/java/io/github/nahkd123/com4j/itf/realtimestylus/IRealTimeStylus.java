package io.github.nahkd123.com4j.itf.realtimestylus;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.annotation.ComInterface;
import io.github.nahkd123.com4j.annotation.ComMethod;
import io.github.nahkd123.com4j.itf.IUnknown;
import io.github.nahkd123.com4j.types.realtimestylus.PacketDescription;
import io.github.nahkd123.com4j.types.realtimestylus.PacketProperty;
import io.github.nahkd123.com4j.types.realtimestylus.StylusQueue;
import io.github.nahkd123.com4j.win32.Guid;
import io.github.nahkd123.com4j.win32.HResult;

@ComInterface("A8BB5D22-3144-4a7b-93CD-F34A16BE513A")
public abstract class IRealTimeStylus extends IUnknown implements RealTimeStylus {
	public IRealTimeStylus(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}

	@ComMethod(index = 3)
	public abstract HResult get_Enabled(MemorySegment pfEnable);

	@ComMethod(index = 4)
	public abstract HResult put_Enabled(int fEnable);

	@ComMethod(index = 5)
	public abstract HResult get_HWND(MemorySegment phwnd);

	@ComMethod(index = 6)
	public abstract HResult put_HWND(MemorySegment hwnd);

	@ComMethod(index = 7)
	public abstract HResult get_WindowInputRectangle(MemorySegment prcWndInputRect);

	@ComMethod(index = 8)
	public abstract HResult put_WindowInputRectangle(MemorySegment prcWndInputRect);

	@ComMethod(index = 9)
	public abstract HResult AddStylusSyncPlugin(int iIndex, IStylusSyncPlugin plugin);

	@ComMethod(index = 10)
	public abstract HResult RemoveStylusSyncPlugin(int iIndex, MemorySegment ppiPlugin);

	@ComMethod(index = 11)
	public abstract HResult RemoveAllStylusSyncPlugins();

	@ComMethod(index = 12)
	public abstract HResult GetStylusSyncPlugin(int iIndex, MemorySegment ppiPlugin);

	@ComMethod(index = 13)
	public abstract HResult GetStylusSyncPluginCount(MemorySegment pcPlugins);

	@ComMethod(index = 14)
	public abstract HResult AddStylusAsyncPlugin(int iIndex, IStylusAsyncPlugin plugin);

	@ComMethod(index = 15)
	public abstract HResult RemoveStylusAsyncPlugin(int iIndex, MemorySegment ppiPlugin);

	@ComMethod(index = 16)
	public abstract HResult RemoveAllStylusAsyncPlugins();

	@ComMethod(index = 17)
	public abstract HResult GetStylusAsyncPlugin(int iIndex, MemorySegment ppiPlugin);

	@ComMethod(index = 18)
	public abstract HResult GetStylusAsyncPluginCount(MemorySegment pcPlugins);

	@ComMethod(index = 19)
	public abstract HResult get_ChildRealTimeStylusPlugin(MemorySegment ppiRTS);

	@ComMethod(index = 20)
	public abstract HResult putref_ChildRealTimeStylusPlugin(MemorySegment piRTS);

	@ComMethod(index = 21)
	public abstract HResult AddCustomStylusDataToQueue(StylusQueue sq, MemorySegment pGuidId, int cbData, MemorySegment pbData);

	@ComMethod(index = 22)
	public abstract HResult ClearStylusQueues();

	@ComMethod(index = 23)
	public abstract HResult SetAllTabletsMode(int fUseMouseForInput);

	@ComMethod(index = 24)
	public abstract HResult SetSingleTabletMode(IInkTablet piTablet);

	@ComMethod(index = 25)
	public abstract HResult GetTablet(MemorySegment ppiSingleTablet);

	@ComMethod(index = 26)
	public abstract HResult GetTabletContextIdFromTablet(MemorySegment piTablet, MemorySegment pTcid);

	@ComMethod(index = 27)
	public abstract HResult GetTabletFromTabletContextId(int tcid, MemorySegment ppiTablet);

	@ComMethod(index = 28)
	public abstract HResult GetAllTabletContextIds(MemorySegment pcTcidCount, MemorySegment ppTcids);

	@ComMethod(index = 29)
	public abstract HResult GetStyluses(MemorySegment ppiInkCursors);

	@ComMethod(index = 30)
	public abstract HResult GetStylusForId(int sid, MemorySegment ppiInkCursor);

	@ComMethod(index = 31)
	public abstract HResult SetDesiredPacketDescription(int cProperties, MemorySegment pPropertyGuids);

	@ComMethod(index = 32)
	public abstract HResult GetDesiredPacketDescription(MemorySegment pcProperties, MemorySegment ppPropertyGuids);

	@ComMethod(index = 33)
	public abstract HResult GetPacketDescriptionData(int tcid, MemorySegment pfInkToDeviceScaleX, MemorySegment pfInkToDeviceScaleY, MemorySegment pcPacketProperties, MemorySegment ppPacketProperties);

	@Override
	public boolean isEnabled() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment pfEnable = arena.allocate(ValueLayout.JAVA_INT);
			get_Enabled(pfEnable).throwIfFail();
			return pfEnable.get(ValueLayout.JAVA_INT, 0L) != 0;
		}
	}

	@Override
	public void setEnable(boolean enable) {
		put_Enabled(enable ? 1 : 0).throwIfFail();
	}

	@Override
	public long getHwnd() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment phwnd = arena.allocate(ValueLayout.ADDRESS);
			get_HWND(phwnd).throwIfFail();
			return phwnd.get(ValueLayout.ADDRESS, 0L).address();
		}
	}

	@Override
	public void setHwnd(long hwnd) {
		put_HWND(MemorySegment.ofAddress(hwnd)).throwIfFail();
	}

	@Override
	public void addSyncPlugin(int index, IStylusSyncPlugin plugin) {
		AddStylusSyncPlugin(index, plugin).throwIfFail();
	}

	@Override
	public void addAsyncPlugin(int index, IStylusAsyncPlugin plugin) {
		AddStylusAsyncPlugin(index, plugin).throwIfFail();
	}

	@Override
	public IStylusSyncPlugin removeSyncPlugin(int index) {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment ppiPlugin = arena.allocate(ValueLayout.ADDRESS);
			RemoveStylusSyncPlugin(index, ppiPlugin);
			MemorySegment piTablet = ppiPlugin.get(ValueLayout.ADDRESS, 0L);
			return ComFactory.instance().wrap(piTablet, IStylusSyncPlugin.class);
		}
	}

	@Override
	public IStylusAsyncPlugin removeAsyncPlugin(int index) {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment ppiPlugin = arena.allocate(ValueLayout.ADDRESS);
			RemoveStylusAsyncPlugin(index, ppiPlugin);
			MemorySegment piTablet = ppiPlugin.get(ValueLayout.ADDRESS, 0L);
			return ComFactory.instance().wrap(piTablet, IStylusAsyncPlugin.class);
		}
	}

	@Override
	public void removeAllSyncPlugins() {
		RemoveAllStylusSyncPlugins().throwIfFail();
	}

	@Override
	public void removeAllAsyncPlugins() {
		RemoveAllStylusAsyncPlugins().throwIfFail();
	}

	@Override
	public IStylusSyncPlugin getSyncPlugin(int index) {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment ppiPlugin = arena.allocate(ValueLayout.ADDRESS);
			GetStylusSyncPlugin(index, ppiPlugin);
			MemorySegment piTablet = ppiPlugin.get(ValueLayout.ADDRESS, 0L);
			return ComFactory.instance().wrap(piTablet, IStylusSyncPlugin.class);
		}
	}

	@Override
	public IStylusAsyncPlugin getAsyncPlugin(int index) {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment ppiPlugin = arena.allocate(ValueLayout.ADDRESS);
			GetStylusAsyncPlugin(index, ppiPlugin);
			MemorySegment piTablet = ppiPlugin.get(ValueLayout.ADDRESS, 0L);
			return ComFactory.instance().wrap(piTablet, IStylusAsyncPlugin.class);
		}
	}

	@Override
	public int syncPluginsCount() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment pcPlugins = arena.allocate(ValueLayout.JAVA_INT);
			GetStylusSyncPluginCount(pcPlugins).throwIfFail();
			return pcPlugins.get(ValueLayout.JAVA_INT, 0L);
		}
	}

	@Override
	public int asyncPluginsCount() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment pcPlugins = arena.allocate(ValueLayout.JAVA_INT);
			GetStylusAsyncPluginCount(pcPlugins).throwIfFail();
			return pcPlugins.get(ValueLayout.JAVA_INT, 0L);
		}
	}

	@Override
	public IInkTablet getTablet() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment ppiTablet = arena.allocate(ValueLayout.ADDRESS);
			GetTablet(ppiTablet).throwIfFail();
			MemorySegment piTablet = ppiTablet.get(ValueLayout.ADDRESS, 0L);
			return ComFactory.instance().wrap(piTablet, IInkTablet.class);
		}
	}

	@Override
	public IInkTablet getTabletFromContextId(int tcid) {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment ppiTablet = arena.allocate(ValueLayout.ADDRESS);
			GetTabletFromTabletContextId(tcid, ppiTablet).throwIfFail();
			MemorySegment piTablet = ppiTablet.get(ValueLayout.ADDRESS, 0L);
			return ComFactory.instance().wrap(piTablet, IInkTablet.class);
		}
	}

	@Override
	public void setDesiredPacketDescription(Collection<Guid> guids) {
		try (Arena arena = Arena.ofConfined()) {
			Guid[] array = guids.toArray(Guid[]::new);
			MemorySegment ptr = arena.allocate(MemoryLayout.sequenceLayout(array.length, Guid.LAYOUT));

			for (int i = 0; i < array.length; i++) {
				MemorySegment guidPtr = MemorySegment
					.ofAddress(ptr.address() + i * Guid.LAYOUT.byteSize())
					.reinterpret(Guid.LAYOUT.byteSize());
				array[i].setToMemorySegment(guidPtr);
			}

			SetDesiredPacketDescription(array.length, ptr);
		}
	}

	@Override
	public Set<Guid> getDesiredPacketDescription() {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment pcProps = arena.allocate(ValueLayout.JAVA_INT);
			MemorySegment ppGuids = arena.allocate(ValueLayout.ADDRESS);
			GetDesiredPacketDescription(pcProps, ppGuids).throwIfFail();
			int props = pcProps.get(ValueLayout.JAVA_INT, 0L);
			MemorySegment pGuids = ppGuids.get(
				ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(props, Guid.LAYOUT)),
				0L);
			Guid[] array = new Guid[props];

			for (int i = 0; i < props; i++) {
				MemorySegment guidPtr = MemorySegment
					.ofAddress(pGuids.address() + i * Guid.LAYOUT.byteSize())
					.reinterpret(Guid.LAYOUT.byteSize());
				array[i] = Guid.of(guidPtr);
			}

			ComFactory.instance().memFree(pGuids);
			return Set.of(array);
		}
	}

	@Override
	public PacketDescription getPacketDescription(int tcid) {
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment pScaleX = arena.allocate(ValueLayout.JAVA_FLOAT);
			MemorySegment pScaleY = arena.allocate(ValueLayout.JAVA_FLOAT);
			MemorySegment pcProps = arena.allocate(ValueLayout.JAVA_INT);
			MemorySegment ppProps = arena.allocate(ValueLayout.ADDRESS);
			GetPacketDescriptionData(tcid, pScaleX, pScaleY, pcProps, ppProps).throwIfFail();
			float scaleX = pScaleX.get(ValueLayout.JAVA_FLOAT, 0L);
			float scaleY = pScaleY.get(ValueLayout.JAVA_FLOAT, 0L);
			int props = pcProps.get(ValueLayout.JAVA_INT, 0L);
			MemorySegment pProps = ppProps.get(
				ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(props, PacketProperty.LAYOUT)),
				0L);
			PacketProperty[] array = new PacketProperty[props];

			for (int i = 0; i < props; i++) {
				MemorySegment guidPtr = pProps.asSlice(
					PacketProperty.LAYOUT.byteSize() * i,
					PacketProperty.LAYOUT);
				array[i] = PacketProperty.of(guidPtr);
			}

			ComFactory.instance().memFree(pProps);
			return new PacketDescription(tcid, scaleX, scaleY, List.of(array));
		}
	}

	@Override
	public void configureMultipleTablets(boolean allowMouse) {
		SetAllTabletsMode(allowMouse ? 1 : 0).throwIfFail();
	}

	@Override
	public void configureSingleTablet(IInkTablet tablet) {
		SetSingleTabletMode(tablet).throwIfFail();
	}
}

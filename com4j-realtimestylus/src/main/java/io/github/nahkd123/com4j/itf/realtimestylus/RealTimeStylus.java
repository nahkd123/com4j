package io.github.nahkd123.com4j.itf.realtimestylus;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.nahkd123.com4j.ComFactory;
import io.github.nahkd123.com4j.types.realtimestylus.PacketDescription;
import io.github.nahkd123.com4j.types.realtimestylus.PacketField;
import io.github.nahkd123.com4j.win32.Guid;
import io.github.nahkd123.com4j.win32.Win32Exception;

public interface RealTimeStylus {
	/**
	 * <p>
	 * The class GUID of {@code RealTimeStylus}. Use this with
	 * {@link ComFactory#createFromClsid(Class, Guid)} in order to create new
	 * {@link RealTimeStylus}.
	 * </p>
	 * {@snippet :
	 * ComFactory com = ComFactory.instance();
	 * RealTimeStylus rts = com.createFromClsid(IRealTimeStylus.class, RealTimeStylus.CLSID);
	 * rts.setHwnd(GLFWNativeWin32.glfwGetWin32Window(handle));
	 * rts.setEnable(true);
	 * }
	 */
	public static final Guid CLSID = Guid.of("E26B366D-F998-43ce-836F-CB6D904432B0");

	/**
	 * <p>
	 * Check whether this {@link RealTimeStylus} instance is enabled.
	 * </p>
	 * 
	 * @return Enable state of {@link RealTimeStylus}.
	 */
	boolean isEnabled();

	/**
	 * <p>
	 * Set enable state of this {@link RealTimeStylus} instance. This requires
	 * {@code HWND} in order to enable successfully, otherwise
	 * {@link Win32Exception} will be thrown.
	 * </p>
	 * 
	 * @param enable Enable state to change.
	 */
	void setEnable(boolean enable);

	long getHwnd();

	void setHwnd(long hwnd);

	void addSyncPlugin(int index, IStylusSyncPlugin plugin);

	void addAsyncPlugin(int index, IStylusAsyncPlugin plugin);

	IStylusSyncPlugin removeSyncPlugin(int index);

	IStylusAsyncPlugin removeAsyncPlugin(int index);

	void removeAllSyncPlugins();

	void removeAllAsyncPlugins();

	IStylusSyncPlugin getSyncPlugin(int index);

	IStylusAsyncPlugin getAsyncPlugin(int index);

	int syncPluginsCount();

	int asyncPluginsCount();

	IInkTablet getTablet();

	IInkTablet getTabletFromContextId(int tcid);

	void setDesiredPacketDescription(Collection<Guid> guids);

	Set<Guid> getDesiredPacketDescription();

	/**
	 * <p>
	 * Get the description for all packets coming from tablet with specific context
	 * ID.
	 * </p>
	 * 
	 * @param tcid Tablet context ID.
	 * @return The packet description.
	 */
	PacketDescription getPacketDescription(int tcid);

	default void setDesiredFields(Collection<PacketField> fields) {
		setDesiredPacketDescription(fields.stream().map(PacketField::getGuid).toList());
	}

	default Set<PacketField> getDesiredFields() {
		return getDesiredPacketDescription().stream()
			.map(PacketField::ofGuid)
			.filter(p -> p != null)
			.collect(Collectors.toSet());
	}

	void configureMultipleTablets(boolean allowMouse);

	void configureSingleTablet(IInkTablet tablet);
}

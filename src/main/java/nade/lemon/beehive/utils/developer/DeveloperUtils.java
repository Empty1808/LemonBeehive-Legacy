package nade.lemon.beehive.utils.developer;

import org.bukkit.entity.Player;

import nade.lemon.utils.spigot.Version;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeveloperUtils {

	private DeveloperUtils() {
	}

	private static Method sendPacket = null;
	private static Method a = null;

	public static Object getHandle(Object object) {
		try {
			return object.getClass().getMethod("getHandle").invoke(object);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void sendPacket(Player player, Object packet) {
		try {
			Object handle = getHandle(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			if (sendPacket == null) {
				sendPacket = playerConnection.getClass().getMethod("sendPacket", DeveloperUtils.getNMSClass("Packet"));
			}
			sendPacket.invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	public static Object run(Method method, Object object, Object... args) {
		try {
			return method.invoke(object, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static Constructor<?> getConstructor(Class<?> cls, Class<?>... type) {
		try {
			return cls.getConstructor(type);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("An error occurred while run the class " + cls.getName() +".", e);
		}
	}

	public static Object newInstance (Constructor<?> constructor, Object... type) {
		try {
			return constructor.newInstance(type);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException("An error occurred while run the constructor " + constructor.getName() +".", e);
		}
	}

	public static Method getMethod(Class<?> cls, String name, Class<?>... type) {
		try {
			return cls.getMethod(name, type);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("An error occurred while run the class " + cls.getName() +".", e);
		}
	}

	public static Method getDeclaredClasses(Class<?> cls, int array, String name, Class<?>... type) {
		try {
			return cls.getDeclaredClasses()[array].getMethod(name, type);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("An error occurred while run the class " + cls.getName() +".", e);
		}
	}

	public static void sendNewPacket(Player player, Object packet) {
		try {
			Object handle = getHandle(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			if (sendPacket == null) {
				sendPacket = playerConnection.getClass().getMethod("sendPacket", DeveloperUtils.getNMSClass("Packet"));
			}
			sendPacket.invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {}
	}

	public static void a(Player player, Object packet) {
		try {
			Object handle = getHandle(player);
			Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
			if (a == null) {
				a = playerConnection.getClass().getMethod("a", packet.getClass());
			}
			a.invoke(playerConnection, packet);
		} catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	/**
	 * net.minecraft.server
	 */
	public static Class<?> getNMSClass(String className) {
		try {
			return Class.forName("net.minecraft.server." + Version.version() + "." + className);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding NMS class.", ex);
		}
	}

	public static Class<?> getClass(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding NMS class.", ex);
		}
	}

	public static Class<?> getNetClass(String name, String pack) {
		try {
			if (Version.isNew()) {
				return Class.forName("net.minecraft." + pack + "." + name);
			}else {
				return Class.forName("net.minecraft.server." + Version.version() + "." + name);
			}
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding NMS class.", ex);
		}
	}

	public static Class<?> getNMClass(String className, String pack) {
		try {
			return Class.forName("net.minecraft." + pack + "." + className);
		}catch (ClassNotFoundException e) {
			throw new RuntimeException("An error occurred while finding NMS class.", e);
		}
	}

	/**
	 * org.bukkit.craftbukkit
	 */
	public static Class<?> getOBCClass(String className) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + Version.version() + "." + className);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding NMS class.", ex);
		}
	}

	public static Class<?> getPackOBCClass(String name, String pack) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + Version.version() + "." + pack + "." + name);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("An error occurred while finding NMS class.", ex);
		}
	}

	public static class IChatBaseComponentNew {

		private static final Logger logger = Logger.getLogger(IChatBaseComponentOld.class.getName());
		public static final Class<?> IChatBaseComponent = getNMClass("IChatBaseComponent", "network.chat");
		private static Method newIChatBaseComponent = null;

		static {
			try {
				newIChatBaseComponent = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class);
			} catch (NoSuchMethodException e) {
				logger.log(Level.SEVERE, "An error occurred while initializing IChatBaseComponent.");
			}
		}

		public static Object of(String string) throws InvocationTargetException, IllegalAccessException {
			return newIChatBaseComponent.invoke(null, "{\"text\": \"" + string + "\"}");
		}

	}

	public static class IChatBaseComponentOld {

		private static final Logger logger = Logger.getLogger(IChatBaseComponentOld.class.getName());
		public static final Class<?> IChatBaseComponent = getNMSClass("IChatBaseComponent");
		private static Method newIChatBaseComponent = null;

		static {
			try {
				newIChatBaseComponent = IChatBaseComponent.getDeclaredClasses()[0].getMethod("a", String.class);
			} catch (NoSuchMethodException e) {
				logger.log(Level.SEVERE, "An error occurred while initializing IChatBaseComponent.");
			}
		}

		public static Object of(String string) {
			try {
				return newIChatBaseComponent.invoke(null, "{\"text\": \"" + string + "\"}");
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("An error occurred while IChatBase.", e);
			}
		}

	}

}

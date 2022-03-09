package me.xdark.reflectionhooks.core;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jdk.internal.reflect.ConstructorAccessor;
import jdk.internal.reflect.FieldAccessor;
import jdk.internal.reflect.MethodAccessor;
import jdk.internal.reflect.ReflectionFactory;
import me.xdark.reflectionhooks.api.FieldGetController;
import me.xdark.reflectionhooks.api.FieldSetController;
import me.xdark.reflectionhooks.api.Hook;
import me.xdark.reflectionhooks.api.Invoker;

@SuppressWarnings("Duplicates")
final class JavaAccessOld implements JavaAccess {

	private final ReflectionFactory factory = ReflectionFactory.getReflectionFactory();

	@Override
	public void init() throws Throwable {
		Environment.LOOKUP.findStaticSetter(Class.class, "useCaches", boolean.class)
				.invokeExact(true);
		Environment.LOOKUP
				.findStatic(ReflectionFactory.class, "checkInitted", MethodType.methodType(void.class))
				.invokeExact();
	}

	@Override
	public <R> Object newMethodAccessor(Method method, Invoker<R> invoker, Hook hook) {
		MethodAccessor accessor = factory.newMethodAccessor(method);
		Invoker<R> parent = (parent1, handle, args) -> (R) accessor.invoke(handle, args);
		return (MethodAccessor) (handle, args) -> {
			try {
				if (!hook.isHooked()) {
					return parent.invoke(null, handle, args);
				}
				return invoker.invoke(parent, handle, args);
			} catch (Throwable t) {
				throw new InvocationTargetException(t);
			}
		};
	}

	@Override
	public Object newFieldAccessor(Field field, FieldGetController getController,
			FieldSetController setController, Hook hook) {
		FieldAccessor accessor = factory.newFieldAccessor(field, true);
		FieldGetController parentGet = (parent, handle) -> accessor.get(handle);
		FieldSetController parentSet = (parent, handle, value) -> {
			try {
				accessor.set(handle, value);
			} catch (IllegalAccessException ignored) {
			}
		};
		return new FieldAccessor() {
			@Override
			public Object get(Object o) throws IllegalArgumentException {
				return getController == null || !hook.isHooked() ? accessor.get(o)
						: getController.get(parentGet, o);
			}

			@Override
			public boolean getBoolean(Object o) throws IllegalArgumentException {
				return getController == null || !hook.isHooked() ? accessor.getBoolean(o)
						: (boolean) getController.get(parentGet, o);
			}

			@Override
			public byte getByte(Object o) throws IllegalArgumentException {
				return getController == null || !hook.isHooked() ? accessor.getByte(o)
						: (byte) getController.get(parentGet, o);
			}

			@Override
			public char getChar(Object o) throws IllegalArgumentException {
				return getController == null || !hook.isHooked() ? accessor.getChar(o)
						: (char) getController.get(parentGet, o);
			}

			@Override
			public short getShort(Object o) throws IllegalArgumentException {
				return getController == null || !hook.isHooked() ? accessor.getShort(o)
						: (short) getController.get(parentGet, o);
			}

			@Override
			public int getInt(Object o) throws IllegalArgumentException {
				return getController == null || !hook.isHooked() ? accessor.getInt(o)
						: (int) getController.get(parentGet, o);
			}

			@Override
			public long getLong(Object o) throws IllegalArgumentException {
				return getController == null || !hook.isHooked() ? accessor.getLong(o)
						: (long) getController.get(parentGet, o);
			}

			@Override
			public float getFloat(Object o) throws IllegalArgumentException {
				return getController == null || !hook.isHooked() ? accessor.getFloat(o)
						: (float) getController.get(parentGet, o);
			}

			@Override
			public double getDouble(Object o) throws IllegalArgumentException {
				return getController == null || !hook.isHooked() ? accessor.getDouble(o)
						: (double) getController.get(parentGet, o);
			}

			@Override
			public void set(Object o, Object o1)
					throws IllegalArgumentException, IllegalAccessException {
				if (setController == null || !hook.isHooked()) {
					accessor.set(o, o1);
				} else {
					setController.set(parentSet, o, o1);
				}
			}

			@Override
			public void setBoolean(Object o, boolean b)
					throws IllegalArgumentException, IllegalAccessException {
				if (setController == null || !hook.isHooked()) {
					accessor.setBoolean(o, b);
				} else {
					setController.set(parentSet, o, b);
				}
			}

			@Override
			public void setByte(Object o, byte b)
					throws IllegalArgumentException, IllegalAccessException {
				if (setController == null || !hook.isHooked()) {
					accessor.setByte(o, b);
				} else {
					setController.set(parentSet, o, b);
				}
			}

			@Override
			public void setChar(Object o, char c)
					throws IllegalArgumentException, IllegalAccessException {
				if (setController == null || !hook.isHooked()) {
					accessor.setChar(o, c);
				} else {
					setController.set(parentSet, o, c);
				}
			}

			@Override
			public void setShort(Object o, short i)
					throws IllegalArgumentException, IllegalAccessException {
				if (setController == null || !hook.isHooked()) {
					accessor.setShort(o, i);
				} else {
					setController.set(parentSet, o, i);
				}
			}

			@Override
			public void setInt(Object o, int i)
					throws IllegalArgumentException, IllegalAccessException {
				if (setController == null || !hook.isHooked()) {
					accessor.setInt(o, i);
				} else {
					setController.set(parentSet, o, i);
				}
			}

			@Override
			public void setLong(Object o, long l)
					throws IllegalArgumentException, IllegalAccessException {
				if (setController == null || !hook.isHooked()) {
					accessor.setLong(o, l);
				} else {
					setController.set(parentSet, o, l);
				}
			}

			@Override
			public void setFloat(Object o, float v)
					throws IllegalArgumentException, IllegalAccessException {
				if (setController == null || !hook.isHooked()) {
					accessor.setFloat(o, v);
				} else {
					setController.set(parentSet, o, v);
				}
			}

			@Override
			public void setDouble(Object o, double v)
					throws IllegalArgumentException, IllegalAccessException {
				if (setController == null || !hook.isHooked()) {
					accessor.setDouble(o, v);
				} else {
					setController.set(parentSet, o, v);
				}
			}
		};
	}

	@Override
	public <R> Object newConstructorAccessor(Constructor<R> constructor, Invoker<R> invoker,
			Hook hook) {
		ConstructorAccessor accessor = factory.newConstructorAccessor(constructor);
		Invoker<R> parent = (parent1, handle, args) -> (R) accessor.newInstance(args);
		return (ConstructorAccessor) args -> {
			try {
				if (!hook.isHooked()) {
					return parent.invoke(null, null, args);
				}
				return invoker.invoke(parent, null, args);
			} catch (Throwable t) {
				throw new InvocationTargetException(t);
			}
		};
	}

	@Override
	public Class<?> resolve(String target) {
		try {
			return Class.forName("sun.reflect." + target);
		} catch (ClassNotFoundException ex) {
			return Environment.sneakyThrow(ex);
		}
	}
}

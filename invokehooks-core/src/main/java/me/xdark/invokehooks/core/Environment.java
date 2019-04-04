package me.xdark.invokehooks.core;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import me.xdark.invokehooks.api.BaseHook;
import me.xdark.invokehooks.api.FieldGetController;
import me.xdark.invokehooks.api.FieldSetController;
import me.xdark.invokehooks.api.Hook;
import me.xdark.invokehooks.api.Invoker;
import sun.misc.Unsafe;
import sun.reflect.ConstructorAccessor;
import sun.reflect.FieldAccessor;
import sun.reflect.MethodAccessor;
import sun.reflect.ReflectionFactory;

final class Environment {

	private static final Lookup LOOKUP;
	private static final Unsafe UNSAFE;
	private static final ReflectionFactory REFLECTION_FACTORY;

	private static final MethodHandle MH_METHOD_COPY;
	private static final MethodHandle MH_METHOD_PARENT_GET;
	private static final MethodHandle MH_METHOD_PARENT_SET;
	private static final MethodHandle MH_METHOD_ACCESSOR_SET;

	private static final MethodHandle MH_FIELD_COPY;
	private static final MethodHandle MH_FIELD_PARENT_GET;
	private static final MethodHandle MH_FIELD_PARENT_SET;
	private static final MethodHandle MH_FIELD_ACCESSOR_SET1;
	private static final MethodHandle MH_FIELD_ACCESSOR_SET2;

	private static final MethodHandle MH_CONST_COPY;
	private static final MethodHandle MH_CONST_PARENT_GET;
	private static final MethodHandle MH_CONST_PARENT_SET;
	private static final MethodHandle MH_CONST_ACCESSOR_SET;

	private Environment() {
	}

	static void prepare() {
		// Prepare our own hooks
		try {
			// Enable reflection cache, init
			LOOKUP.findStaticSetter(Class.class, "useCaches", boolean.class)
					.invokeExact(true);
			LOOKUP.findStatic(ReflectionFactory.class, "checkInitted", MethodType.methodType(void.class))
					.invokeExact();
		} catch (Throwable t) {
			UNSAFE.throwException(t);
		}
	}

	static <R> Hook createMethodHook0(Method method, Invoker<R> hook) {
		assert method != null;
		try {
			// Where magic begins
			Method root = (Method) MH_METHOD_PARENT_GET.invokeExact(method);
			// Copy root method to allow to call original one
			Method copyRoot = (Method) MH_METHOD_COPY.invokeExact(root);
			wipeMethod(copyRoot);
			MethodAccessor accessor = REFLECTION_FACTORY.newMethodAccessor(copyRoot);
			Invoker<R> parent = (parent1, handle, args) -> (R) accessor.invoke(handle, args);
			Hook delegate = new BaseHook();
			MethodAccessor hooked = (handle, args) -> {
				try {
					if (!delegate.isHooked()) {
						return parent.invoke(null, handle, args);
					}
					return hook.invoke(parent, handle, args);
				} catch (Throwable t) {
					throw new InvocationTargetException(t);
				}
			};
			MH_METHOD_ACCESSOR_SET.invokeExact(root, hooked);
			wipeMethod(method);
			return delegate;
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	static Hook createFieldHook0(Field field, FieldGetController getController,
			FieldSetController setController) {
		assert field != null;
		try {
			// Where magic begins
			Field root = (Field) MH_FIELD_PARENT_GET.invokeExact(field);
			// Copy root field to allow to call original one
			Field copyRoot = (Field) MH_FIELD_COPY.invokeExact(root);
			wipeField(copyRoot);
			FieldAccessor accessor = REFLECTION_FACTORY.newFieldAccessor(copyRoot, true);
			FieldGetController parentGet = (parent, handle) -> accessor.get(handle);
			FieldSetController parentSet = (parent, handle, value) -> {
				try {
					accessor.set(handle, value);
				} catch (IllegalAccessException ignored) {
				}
			};

			Hook delegate = new BaseHook();
			FieldAccessor hooked = new FieldAccessor() {
				@Override
				public Object get(Object o) throws IllegalArgumentException {
					return getController == null || !delegate.isHooked() ? accessor.get(o)
							: getController.get(parentGet, o);
				}

				@Override
				public boolean getBoolean(Object o) throws IllegalArgumentException {
					return getController == null || !delegate.isHooked() ? accessor.getBoolean(o)
							: (boolean) getController.get(parentGet, o);
				}

				@Override
				public byte getByte(Object o) throws IllegalArgumentException {
					return getController == null || !delegate.isHooked() ? accessor.getByte(o)
							: (byte) getController.get(parentGet, o);
				}

				@Override
				public char getChar(Object o) throws IllegalArgumentException {
					return getController == null || !delegate.isHooked() ? accessor.getChar(o)
							: (char) getController.get(parentGet, o);
				}

				@Override
				public short getShort(Object o) throws IllegalArgumentException {
					return getController == null || !delegate.isHooked() ? accessor.getShort(o)
							: (short) getController.get(parentGet, o);
				}

				@Override
				public int getInt(Object o) throws IllegalArgumentException {
					return getController == null || !delegate.isHooked() ? accessor.getInt(o)
							: (int) getController.get(parentGet, o);
				}

				@Override
				public long getLong(Object o) throws IllegalArgumentException {
					return getController == null || !delegate.isHooked() ? accessor.getLong(o)
							: (long) getController.get(parentGet, o);
				}

				@Override
				public float getFloat(Object o) throws IllegalArgumentException {
					return getController == null || !delegate.isHooked() ? accessor.getFloat(o)
							: (float) getController.get(parentGet, o);
				}

				@Override
				public double getDouble(Object o) throws IllegalArgumentException {
					return getController == null || !delegate.isHooked() ? accessor.getDouble(o)
							: (double) getController.get(parentGet, o);
				}

				@Override
				public void set(Object o, Object o1)
						throws IllegalArgumentException, IllegalAccessException {
					if (setController == null || !delegate.isHooked()) {
						accessor.set(o, o1);
					} else {
						setController.set(parentSet, o, o1);
					}
				}

				@Override
				public void setBoolean(Object o, boolean b)
						throws IllegalArgumentException, IllegalAccessException {
					if (setController == null || !delegate.isHooked()) {
						accessor.setBoolean(o, b);
					} else {
						setController.set(parentSet, o, b);
					}
				}

				@Override
				public void setByte(Object o, byte b)
						throws IllegalArgumentException, IllegalAccessException {
					if (setController == null || !delegate.isHooked()) {
						accessor.setByte(o, b);
					} else {
						setController.set(parentSet, o, b);
					}
				}

				@Override
				public void setChar(Object o, char c)
						throws IllegalArgumentException, IllegalAccessException {
					if (setController == null || !delegate.isHooked()) {
						accessor.setChar(o, c);
					} else {
						setController.set(parentSet, o, c);
					}
				}

				@Override
				public void setShort(Object o, short i)
						throws IllegalArgumentException, IllegalAccessException {
					if (setController == null || !delegate.isHooked()) {
						accessor.setShort(o, i);
					} else {
						setController.set(parentSet, o, i);
					}
				}

				@Override
				public void setInt(Object o, int i)
						throws IllegalArgumentException, IllegalAccessException {
					if (setController == null || !delegate.isHooked()) {
						accessor.setInt(o, i);
					} else {
						setController.set(parentSet, o, i);
					}
				}

				@Override
				public void setLong(Object o, long l)
						throws IllegalArgumentException, IllegalAccessException {
					if (setController == null || !delegate.isHooked()) {
						accessor.setLong(o, l);
					} else {
						setController.set(parentSet, o, l);
					}
				}

				@Override
				public void setFloat(Object o, float v)
						throws IllegalArgumentException, IllegalAccessException {
					if (setController == null || !delegate.isHooked()) {
						accessor.setFloat(o, v);
					} else {
						setController.set(parentSet, o, v);
					}
				}

				@Override
				public void setDouble(Object o, double v)
						throws IllegalArgumentException, IllegalAccessException {
					if (setController == null || !delegate.isHooked()) {
						accessor.setDouble(o, v);
					} else {
						setController.set(parentSet, o, v);
					}
				}
			};
			MH_FIELD_ACCESSOR_SET1.invokeExact(root, hooked);
			MH_FIELD_ACCESSOR_SET2.invokeExact(root, hooked);
			wipeField(field);
			return delegate;
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	static <R> Hook createConstructorHook0(Constructor<R> constructor, Invoker<R> hook) {
		assert constructor != null;
		try {
			// Where magic begins
			Constructor<R> root = (Constructor<R>) MH_CONST_PARENT_GET.invokeExact(constructor);
			// Copy root method to allow to call original one
			Constructor<R> copyRoot = (Constructor<R>) MH_CONST_COPY.invokeExact(root);
			wipeConstructor(copyRoot);
			ConstructorAccessor accessor = REFLECTION_FACTORY.newConstructorAccessor(copyRoot);
			Invoker<R> parent = (parent1, handle, args) -> (R) accessor.newInstance(args);
			Hook delegate = new BaseHook();
			ConstructorAccessor hooked = args -> {
				try {
					if (!delegate.isHooked()) {
						return parent.invoke(null, args);
					}
					return hook.invoke(parent, null, args);
				} catch (Throwable t) {
					throw new InvocationTargetException(t);
				}
			};
			MH_CONST_ACCESSOR_SET.invokeExact(root, hooked);
			wipeConstructor(constructor);
			return delegate;
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static void wipeMethod(Method method) {
		try {
			MH_METHOD_PARENT_SET.invokeExact(method, (Method) null);
			MH_METHOD_ACCESSOR_SET.invokeExact(method, (MethodAccessor) null);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static void wipeField(Field field) {
		try {
			MH_FIELD_PARENT_SET.invokeExact(field, (Field) null);
			MH_FIELD_ACCESSOR_SET1.invokeExact(field, (FieldAccessor) null);
			MH_FIELD_ACCESSOR_SET2.invokeExact(field, (FieldAccessor) null);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static void wipeConstructor(Constructor<?> constructor) {
		try {
			MH_CONST_PARENT_SET.invokeExact(constructor, (Constructor<?>) null);
			MH_CONST_ACCESSOR_SET.invokeExact(constructor, (ConstructorAccessor) null);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static <T> T sneakyThrow(Throwable t) {
		UNSAFE.throwException(t);
		// We throw exception, but compiler does not know about it
		return null;
	}

	static {
		Object maybeUnsafe = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			try {
				Field field = Unsafe.class.getDeclaredField("theUnsafe");
				field.setAccessible(true);
				return field.get(null);
			} catch (Throwable t) {
				return t;
			}
		});
		if (maybeUnsafe instanceof Throwable) {
			throw new AssertionError("sun.misc.Unsafe is not available!", (Throwable) maybeUnsafe);
		} else {
			UNSAFE = (Unsafe) maybeUnsafe;
		}
		Object maybeLookup = AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			try {
				MethodHandles.publicLookup();
				Field implLookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");
				return UNSAFE.getObject(UNSAFE.staticFieldBase(implLookupField),
						UNSAFE.staticFieldOffset(implLookupField));
			} catch (Throwable t) {
				return t;
			}
		});
		if (maybeLookup instanceof Throwable) {
			throw new AssertionError("java.lang.invoke.Lookup#IMPL_LOOKUP not available!",
					(Throwable) maybeLookup);
		} else {
			LOOKUP = (Lookup) maybeLookup;
		}
		try {
			MH_METHOD_COPY = LOOKUP
					.findVirtual(Method.class, "copy", MethodType.methodType(Method.class));
			MH_METHOD_PARENT_GET = LOOKUP.findGetter(Method.class, "root", Method.class);
			MH_METHOD_PARENT_SET = LOOKUP.findSetter(Method.class, "root", Method.class);
			MH_METHOD_ACCESSOR_SET = LOOKUP
					.findSetter(Method.class, "methodAccessor", MethodAccessor.class);

			MH_FIELD_COPY = LOOKUP
					.findVirtual(Field.class, "copy", MethodType.methodType(Field.class));
			MH_FIELD_PARENT_GET = LOOKUP.findGetter(Field.class, "root", Field.class);
			MH_FIELD_PARENT_SET = LOOKUP.findSetter(Field.class, "root", Field.class);
			MH_FIELD_ACCESSOR_SET1 = LOOKUP
					.findSetter(Field.class, "overrideFieldAccessor", FieldAccessor.class);
			MH_FIELD_ACCESSOR_SET2 = LOOKUP
					.findSetter(Field.class, "fieldAccessor", FieldAccessor.class);

			MH_CONST_COPY = LOOKUP
					.findVirtual(Constructor.class, "copy", MethodType.methodType(Constructor.class));
			MH_CONST_PARENT_GET = LOOKUP.findGetter(Constructor.class, "root", Constructor.class);
			MH_CONST_PARENT_SET = LOOKUP.findSetter(Constructor.class, "root", Constructor.class);
			MH_CONST_ACCESSOR_SET = LOOKUP
					.findSetter(Constructor.class, "constructorAccessor", ConstructorAccessor.class);

			REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();
		} catch (Throwable t) {
			throw new AssertionError("Initial setup failed!", t);
		}
	}
}

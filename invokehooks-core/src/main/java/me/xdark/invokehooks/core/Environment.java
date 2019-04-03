package me.xdark.invokehooks.core;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.misc.Unsafe;

final class Environment {

	static final Lookup LOOKUP;
	static final Unsafe UNSAFE;
	private static final Class<?> C_REFLECTION_DATA;
	private static final MethodHandle MH_METHOD_SLOT;
	private static final MethodHandle MH_FIELD_SLOT;
	private static final MethodHandle MH_CONSTRUCTOR_SLOT;
	private static final MethodHandle MH_REFLECTION_DATA;
	private static final MethodHandle MH_DECLARED_METHODS;
	private static final MethodHandle MH_DECLARED_FIELDS;
	private static final MethodHandle MH_DECLARED_CONSTRUCTORS;

	private Environment() {
	}

	static void prepare() {
		// Prepare our own hooks
		try {
			// Step 1: Enable reflection cache, initialize reflection data for java.lang.Class
			LOOKUP.findStaticSetter(Class.class, "useCaches", boolean.class)
					.invokeExact(true);
			initializeReflectionData(Class.class);
			// Step 2: begin injection into reflection data
			// Obtain reflectionData, setup hooks
			Object reflectionData = getReflectionData(Class.class);
		} catch (Throwable t) {
			UNSAFE.throwException(t);
		}
	}

	private static Object getReflectionData(Class<?> clazz) {
		try {
			return ((SoftReference) MH_REFLECTION_DATA.invoke(clazz)).get();
		} catch (Throwable t) {
			// We throw exception, but compiler does not know about it
			UNSAFE.throwException(t);
			return null;
		}
	}

	private static Method[] getDeclaredMethods(Object reflectionData) {
		try {
			return (Method[]) MH_DECLARED_METHODS.invoke(reflectionData);
		} catch (Throwable t) {
			// We throw exception, but compiler does not know about it
			UNSAFE.throwException(t);
			return null;
		}
	}

	private static Field[] getDeclaredFields(Object reflectionData) {
		try {
			return (Field[]) MH_DECLARED_FIELDS.invoke(reflectionData);
		} catch (Throwable t) {
			// We throw exception, but compiler does not know about it
			UNSAFE.throwException(t);
			return null;
		}
	}

	private static Constructor[] getDeclaredConstructors(Object reflectionData) {
		try {
			return (Constructor[]) MH_DECLARED_CONSTRUCTORS.invoke(reflectionData);
		} catch (Throwable t) {
			// We throw exception, but compiler does not know about it
			UNSAFE.throwException(t);
			return null;
		}
	}

	private static void initializeReflectionData(Class<?> clazz) {
		clazz.getDeclaredMethods();
		clazz.getDeclaredFields();
		clazz.getDeclaredConstructors();
	}

	private static int findSlot(Method method) {
		try {
			return (int) MH_METHOD_SLOT.invokeExact(method);
		} catch (Throwable t) {
			// We throw exception, but compiler does not know about it
			UNSAFE.throwException(t);
			return 0;
		}
	}

	private static int findSlot(Field field) {
		try {
			return (int) MH_FIELD_SLOT.invokeExact(field);
		} catch (Throwable t) {
			// We throw exception, but compiler does not know about it
			UNSAFE.throwException(t);
			return 0;
		}
	}

	private static int findSlot(Constructor constructor) {
		try {
			return (int) MH_CONSTRUCTOR_SLOT.invokeExact(constructor);
		} catch (Throwable t) {
			// We throw exception, but compiler does not know about it
			UNSAFE.throwException(t);
			return 0;
		}
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
			MH_METHOD_SLOT = LOOKUP.findGetter(Method.class, "slot", int.class);
			MH_FIELD_SLOT = LOOKUP.findGetter(Field.class, "slot", int.class);
			MH_CONSTRUCTOR_SLOT = LOOKUP.findGetter(Constructor.class, "slot", int.class);
			MH_REFLECTION_DATA = LOOKUP.findGetter(Class.class, "reflectionData", SoftReference.class);

			C_REFLECTION_DATA = Class.forName("java.lang.Class$ReflectionData");
			MH_DECLARED_METHODS = LOOKUP
					.findGetter(C_REFLECTION_DATA, "declaredMethods",
							Method[].class);
			MH_DECLARED_FIELDS = LOOKUP
					.findGetter(C_REFLECTION_DATA, "declaredFields",
							Method[].class);
			MH_DECLARED_CONSTRUCTORS = LOOKUP
					.findGetter(C_REFLECTION_DATA, "declaredConstructors",
							Constructor[].class);
		} catch (Throwable t) {
			throw new AssertionError("Initial setup failed!", t);
		}
	}
}

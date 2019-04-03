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
import java.util.Objects;
import me.xdark.invokehooks.api.MethodHook;
import sun.misc.Unsafe;

final class Environment {

	private static final Lookup LOOKUP;
	private static final Unsafe UNSAFE;
	private static final Class<?> C_REFLECTION_DATA;

	private static final MethodHandle MH_GET_METHOD_SLOT;
	private static final MethodHandle MH_GET_FIELD_SLOT;
	private static final MethodHandle MH_GET_CONSTRUCTOR_SLOT;
	private static final MethodHandle MH_SET_METHOD_SLOT;
	private static final MethodHandle MH_SET_FIELD_SLOT;
	private static final MethodHandle MH_SET_CONSTRUCTOR_SLOT;

	private static final MethodHandle MH_DECLARING_CLASS_METHOD;
	private static final MethodHandle MH_DECLARING_CLASS_FIELD;
	private static final MethodHandle MH_DECLARING_CLASS_CONSTRUCTOR;

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
		} catch (Throwable t) {
			UNSAFE.throwException(t);
		}
	}

	static <R> MethodHook<R> createMethodHook0(Method method, Method hook) {
		assert method != null;
		Class<?>[] params = hook.getParameterTypes();
		if (params.length != 2 || params[1] != Object.class || params[2] != Object[].class) {
			throw new IllegalArgumentException(
					"Illegal hook descriptor (required: Object,Object[])");
		}
		// Obtain declaring class, initialize & get reflection data
		Class<?> declaringClass = method.getDeclaringClass();
		initializeReflectionData(declaringClass);
		Object reflectionData = getReflectionData(declaringClass);
		// getDeclaredMethod returns a copy of original method, so we need to find original
		Method original = null;
		for (Method other : getDeclaredMethods(reflectionData)) {
			if (other.equals(method)) {
				original = other;
				break;
			}
		}
		Objects.requireNonNull(original, "Original method was not found!");
		// Where magic begins
		return null;
	}

	private static Object getReflectionData(Class<?> clazz) {
		try {
			return ((SoftReference) MH_REFLECTION_DATA.invoke(clazz)).get();
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static Method[] getDeclaredMethods(Object reflectionData) {
		try {
			return (Method[]) MH_DECLARED_METHODS.invoke(reflectionData);
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static Field[] getDeclaredFields(Object reflectionData) {
		try {
			return (Field[]) MH_DECLARED_FIELDS.invoke(reflectionData);
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static Constructor[] getDeclaredConstructors(Object reflectionData) {
		try {
			return (Constructor[]) MH_DECLARED_CONSTRUCTORS.invoke(reflectionData);
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static void initializeReflectionData(Class<?> clazz) {
		clazz.getDeclaredMethods();
		clazz.getDeclaredFields();
		clazz.getDeclaredConstructors();
	}

	private static int findSlot(Method method) {
		try {
			return (int) MH_GET_METHOD_SLOT.invokeExact(method);
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static int findSlot(Field field) {
		try {
			return (int) MH_GET_FIELD_SLOT.invokeExact(field);
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static int findSlot(Constructor constructor) {
		try {
			return (int) MH_GET_CONSTRUCTOR_SLOT.invokeExact(constructor);
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static void setSlot(Method method, int newSlot) {
		try {
			MH_SET_METHOD_SLOT.invokeExact(method, newSlot);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static void setSlot(Field field, int newSlot) {
		try {
			MH_SET_FIELD_SLOT.invokeExact(field, newSlot);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static void setSlot(Constructor<?> constructor, int newSlot) {
		try {
			MH_SET_CONSTRUCTOR_SLOT.invokeExact(constructor, newSlot);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static void setClass(Method method, Class<?> newClass) {
		try {
			MH_DECLARING_CLASS_METHOD.invokeExact(method, newClass);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static void setClass(Field field, Class<?> newClass) {
		try {
			MH_DECLARING_CLASS_FIELD.invokeExact(field, newClass);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static void setClass(Constructor<?> constructor, Class<?> newClass) {
		try {
			MH_DECLARING_CLASS_CONSTRUCTOR.invokeExact(constructor, newClass);
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
			MH_GET_METHOD_SLOT = LOOKUP.findGetter(Method.class, "slot", int.class);
			MH_GET_FIELD_SLOT = LOOKUP.findGetter(Field.class, "slot", int.class);
			MH_GET_CONSTRUCTOR_SLOT = LOOKUP.findGetter(Constructor.class, "slot", int.class);

			MH_SET_METHOD_SLOT = LOOKUP.findSetter(Method.class, "slot", int.class);
			MH_SET_FIELD_SLOT = LOOKUP.findSetter(Field.class, "slot", int.class);
			MH_SET_CONSTRUCTOR_SLOT = LOOKUP.findSetter(Constructor.class, "slot", int.class);

			MH_DECLARING_CLASS_METHOD = LOOKUP.findSetter(Method.class, "clazz", Class.class);
			MH_DECLARING_CLASS_FIELD = LOOKUP.findSetter(Field.class, "clazz", Class.class);
			MH_DECLARING_CLASS_CONSTRUCTOR = LOOKUP.findSetter(Constructor.class, "clazz", Class.class);

			MH_REFLECTION_DATA = LOOKUP.findGetter(Class.class, "reflectionData", SoftReference.class);

			C_REFLECTION_DATA = Class.forName("java.lang.Class$ReflectionData");
			MH_DECLARED_METHODS = LOOKUP
					.findGetter(C_REFLECTION_DATA, "declaredMethods",
							Method[].class);
			MH_DECLARED_FIELDS = LOOKUP
					.findGetter(C_REFLECTION_DATA, "declaredFields",
							Field[].class);
			MH_DECLARED_CONSTRUCTORS = LOOKUP
					.findGetter(C_REFLECTION_DATA, "declaredConstructors",
							Constructor[].class);
		} catch (Throwable t) {
			throw new AssertionError("Initial setup failed!", t);
		}
	}
}

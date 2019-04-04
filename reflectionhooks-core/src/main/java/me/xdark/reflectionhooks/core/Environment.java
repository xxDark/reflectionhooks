package me.xdark.reflectionhooks.core;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import me.xdark.reflectionhooks.api.BaseHook;
import me.xdark.reflectionhooks.api.FieldGetController;
import me.xdark.reflectionhooks.api.FieldSetController;
import me.xdark.reflectionhooks.api.Hook;
import me.xdark.reflectionhooks.api.Invoker;
import sun.misc.Unsafe;

final class Environment {

	static final Lookup LOOKUP;
	private static final Unsafe UNSAFE;
	private static final JavaAccess JAVA_ACCESS;

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
		try {
			JAVA_ACCESS.init();
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	static <R> Hook createMethodHook0(Method method, Invoker<R> hook) {
		assert method != null;
		try {
			// Where magic begins
			Method root = (Method) MH_METHOD_PARENT_GET.invokeExact(method);
			// If it is already a root one, then, what the heck?
			if (root == null) {
				root = method;
			}
			// Copy root method to allow to call original one
			Method copyRoot = (Method) MH_METHOD_COPY.invokeExact(root);
			wipeMethod(copyRoot);
			Hook delegate = new BaseHook();
			Object hooked = JAVA_ACCESS.newMethodAccessor(copyRoot, hook, delegate);
			MH_METHOD_ACCESSOR_SET.invoke(root, hooked);
			wipeMethod(method);
			MH_METHOD_ACCESSOR_SET.invoke(method, hooked);
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
			// If it is already a root one, then, what the heck?
			if (root == null) {
				root = field;
			}
			// Copy root field to allow to call original one
			Field copyRoot = (Field) MH_FIELD_COPY.invokeExact(root);
			wipeField(copyRoot);
			Hook delegate = new BaseHook();
			Object hooked = JAVA_ACCESS
					.newFieldAccessor(copyRoot, getController, setController, delegate);
			MH_FIELD_ACCESSOR_SET1.invoke(root, hooked);
			MH_FIELD_ACCESSOR_SET2.invoke(root, hooked);
			wipeField(field);
			MH_FIELD_ACCESSOR_SET1.invoke(field, hooked);
			MH_FIELD_ACCESSOR_SET2.invoke(field, hooked);
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
			// If it is already a root one, then, what the heck?
			if (root == null) {
				root = constructor;
			}
			// Copy root method to allow to call original one
			Constructor<R> copyRoot = (Constructor<R>) MH_CONST_COPY.invokeExact(root);
			wipeConstructor(copyRoot);
			Hook delegate = new BaseHook();
			Object hooked = JAVA_ACCESS.newConstructorAccessor(constructor, hook, delegate);
			MH_CONST_ACCESSOR_SET.invoke(root, hooked);
			wipeConstructor(constructor);
			MH_CONST_ACCESSOR_SET.invoke(constructor, hooked);
			return delegate;
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static void wipeMethod(Method method) {
		try {
			MH_METHOD_PARENT_SET.invokeExact(method, (Method) null);
			MH_METHOD_ACCESSOR_SET.invoke(method, null);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static void wipeField(Field field) {
		try {
			MH_FIELD_PARENT_SET.invokeExact(field, (Field) null);
			MH_FIELD_ACCESSOR_SET1.invoke(field, null);
			MH_FIELD_ACCESSOR_SET2.invoke(field, null);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	private static void wipeConstructor(Constructor<?> constructor) {
		try {
			MH_CONST_PARENT_SET.invokeExact(constructor, (Constructor<?>) null);
			MH_CONST_ACCESSOR_SET.invoke(constructor, null);
		} catch (Throwable t) {
			sneakyThrow(t);
		}
	}

	static <T> T sneakyThrow(Throwable t) {
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

			// I hate Oracle, if they change classfile format, im gonna kill myself
			int version;
			try (InputStream in = ClassLoader.getSystemResourceAsStream("java/lang/ClassLoader.class")) {
				in.skip(6);
				version = (in.read() << 8) + in.read() - 44;
			}
			if (version <= 8) {
				JAVA_ACCESS = new JavaAccessOld();
			} else {
				JAVA_ACCESS = new JavaAccessNew();
			}

			MH_METHOD_COPY = LOOKUP
					.findVirtual(Method.class, "copy", MethodType.methodType(Method.class));
			MH_METHOD_PARENT_GET = LOOKUP.findGetter(Method.class, "root", Method.class);
			MH_METHOD_PARENT_SET = LOOKUP.findSetter(Method.class, "root", Method.class);
			MH_METHOD_ACCESSOR_SET = LOOKUP
					.findSetter(Method.class, "methodAccessor", JAVA_ACCESS.resolve("MethodAccessor"));

			MH_FIELD_COPY = LOOKUP
					.findVirtual(Field.class, "copy", MethodType.methodType(Field.class));
			MH_FIELD_PARENT_GET = LOOKUP.findGetter(Field.class, "root", Field.class);
			MH_FIELD_PARENT_SET = LOOKUP.findSetter(Field.class, "root", Field.class);
			MH_FIELD_ACCESSOR_SET1 = LOOKUP
					.findSetter(Field.class, "overrideFieldAccessor", JAVA_ACCESS.resolve("FieldAccessor"));
			MH_FIELD_ACCESSOR_SET2 = LOOKUP
					.findSetter(Field.class, "fieldAccessor", JAVA_ACCESS.resolve("FieldAccessor"));

			MH_CONST_COPY = LOOKUP
					.findVirtual(Constructor.class, "copy", MethodType.methodType(Constructor.class));
			MH_CONST_PARENT_GET = LOOKUP.findGetter(Constructor.class, "root", Constructor.class);
			MH_CONST_PARENT_SET = LOOKUP.findSetter(Constructor.class, "root", Constructor.class);
			MH_CONST_ACCESSOR_SET = LOOKUP
					.findSetter(Constructor.class, "constructorAccessor",
							JAVA_ACCESS.resolve("ConstructorAccessor"));
		} catch (Throwable t) {
			throw new AssertionError("Initial setup failed!", t);
		}
	}
}

package me.xdark.invokehooks.core;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import me.xdark.invokehooks.api.BaseHook;
import me.xdark.invokehooks.api.Hook;
import me.xdark.invokehooks.api.Invoker;
import sun.misc.Unsafe;
import sun.reflect.MethodAccessor;
import sun.reflect.ReflectionFactory;

final class Environment {

	private static final Lookup LOOKUP;
	private static final Unsafe UNSAFE;

	private static final MethodHandle MH_METHOD_COPY;
	private static final MethodHandle MH_METHOD_PARENT_GET;
	private static final MethodHandle MH_METHOD_PARENT_SET;
	private static final MethodHandle MH_METHOD_ACCESSOR_SET;

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

	static <R> Hook createMethodHook0(Class<R> rtype, Method method, Invoker<R> hook) {
		assert method != null;
		// Where magic begins
		Method root = getRootMethod(method);
		// Copy root method to allow to call original one
		Method copyRoot = copyMethod(root);
		wipeMethod(copyRoot);
		try {
			MethodAccessor accessor = ReflectionFactory.getReflectionFactory()
					.newMethodAccessor(copyRoot);
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


	private static Method copyMethod(Method method) {
		try {
			return (Method) MH_METHOD_COPY.invokeExact(method);
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static Method getRootMethod(Method method) {
		try {
			return (Method) MH_METHOD_PARENT_GET.invokeExact(method);
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
		} catch (Throwable t) {
			throw new AssertionError("Initial setup failed!", t);
		}
	}
}

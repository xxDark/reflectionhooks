package me.xdark.invokehooks.core;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import me.xdark.invokehooks.api.Invoker;
import me.xdark.invokehooks.api.MethodHook;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;
import sun.reflect.MethodAccessor;

final class Environment {

	private static final String HOOK_CLASS_NAME = "me/xdark/invokehooks/codegen/Hook";
	private static volatile int nextId = 0;

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
	private static final MethodHandle MH_METHOD_COPY;
	private static final MethodHandle MH_METHOD_PARENT;
	private static final MethodHandle MH_METHOD_ACCESSOR;

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

	static <R> MethodHook<R> createMethodHook0(Class<R> rtype, Method method, Invoker<R> hook) {
		assert method != null;
		// Obtain declaring class, initialize & get reflection data
		Class<?> declaringClass = method.getDeclaringClass();
		initializeReflectionData(declaringClass);
		Object reflectionData = getReflectionData(declaringClass);
		wipeMethod(method);

		// Original invoker
		Method copy = copyMethod(method);
		// Where magic begins

		// Prepare parent invoker, generate ASM class
		Invoker<R> parent = (parent1, handle, args) -> (R) copy.invoke(handle, args);
		ClassWriter cw = new ClassWriter(0);
		String className = HOOK_CLASS_NAME + nextId++;
		String signature = rtype.isArray() ? rtype.getName() : ('L' + rtype.getName() + ';');
		cw.visit(52, Opcodes.ACC_PUBLIC, className,
				"Lme/xdark/invokehooks/api/MethodHook<" + signature + ">;",
				"java/lang/Object", null);
		cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "invoker",
				"Lme/xdark/invokehooks/api/Invoker;", null, null);
		cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "original",
				"Lme/xdark/invokehooks/api/Invoker;", null, null);
		{
			MethodVisitor mv = cw
					.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
							"(Lme/xdark/invokehooks/api/Invoker;Lme/xdark/invokehooks/api/Invoker;)V", null,
							null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V", false);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitFieldInsn(Opcodes.PUTFIELD, className, "invoker", signature);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitFieldInsn(Opcodes.PUTFIELD, className, "original", signature);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_VARARGS, "invoke",
					"(Ljava/lang/Object;[Ljava/lang/Object;)" + signature,
					null, new String[]{"java/lang/Throwable"});
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "invoker",
					"Lme/xdark/invokehooks/api/Invoker;");
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "original",
					"Lme/xdark/invokehooks/api/Invoker;");
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "me/xdark/invokehooks/api/Invoker", "invoke",
					"(Lme/xdark/invokehooks/api/Invoker;Ljava/lang/Object;[Ljava/lang/Object;)" + signature,
					true);
			mv.visitTypeInsn(Opcodes.CHECKCAST, signature);
			mv.visitInsn(Opcodes.RETURN);
		}
		/*try {
			Files.write(Paths.get(".", "Generated.class"), cw.toByteArray(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] src = cw.toByteArray();
		Class<?> defined = UNSAFE
				.defineClass(className, src, 0, src.length, Environment.class.getClassLoader(), null);
		try {
			Object instance = defined.getDeclaredConstructors()[0].newInstance(hook, parent);
			return new
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			return sneakyThrow(e);
		}*/
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

	private static Method copyMethod(Method method) {
		try {
			return (Method) MH_METHOD_COPY.invokeExact(method);
		} catch (Throwable t) {
			return sneakyThrow(t);
		}
	}

	private static void wipeMethod(Method method) {
		try {
			MH_METHOD_PARENT.invokeExact(method, (Method) null);
			MH_METHOD_ACCESSOR.invokeExact(method, (MethodAccessor) null);
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

			MH_METHOD_COPY = LOOKUP
					.findVirtual(Method.class, "copy", MethodType.methodType(Method.class));
			MH_METHOD_PARENT = LOOKUP.findSetter(Method.class, "root", Method.class);
			MH_METHOD_ACCESSOR = LOOKUP.findSetter(Method.class, "methodAccessor", MethodAccessor.class);
		} catch (Throwable t) {
			throw new AssertionError("Initial setup failed!", t);
		}
	}
}

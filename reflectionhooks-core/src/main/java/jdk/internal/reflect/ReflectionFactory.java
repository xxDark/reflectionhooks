package jdk.internal.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// Dummy JDK class
public class ReflectionFactory {

	public static ReflectionFactory getReflectionFactory() {
		return null;
	}

	public MethodAccessor newMethodAccessor(Method method) {
		return null;
	}

	public ConstructorAccessor newConstructorAccessor(Constructor<?> c) {
		return null;
	}

	public FieldAccessor newFieldAccessor(Field field, boolean override) {
		return null;
	}

	private static void checkInitted() { }
}

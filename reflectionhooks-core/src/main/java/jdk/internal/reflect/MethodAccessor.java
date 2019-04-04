package jdk.internal.reflect;

import java.lang.reflect.InvocationTargetException;

// Dummy JDK class
public interface MethodAccessor {

	Object invoke(Object var1, Object[] var2)
			throws IllegalArgumentException, InvocationTargetException;
}
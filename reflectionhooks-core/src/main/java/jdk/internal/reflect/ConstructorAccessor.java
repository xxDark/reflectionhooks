package jdk.internal.reflect;

import java.lang.reflect.InvocationTargetException;

// Dummy JDK class
public interface ConstructorAccessor {

	Object newInstance(Object[] var1)
			throws InstantiationException, IllegalArgumentException, InvocationTargetException;
}

package me.xdark.reflectionhooks.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.xdark.reflectionhooks.api.FieldGetController;
import me.xdark.reflectionhooks.api.FieldSetController;
import me.xdark.reflectionhooks.api.Hook;
import me.xdark.reflectionhooks.api.Invoker;

interface JavaAccess {

	default void init() throws Throwable { }

	<R> Object newMethodAccessor(Method method, Invoker<R> invoker, Hook hook);

	Object newFieldAccessor(Field field, FieldGetController getController,
			FieldSetController setController, Hook hook);

	<R> Object newConstructorAccessor(Constructor<R> constructor, Invoker<R> invoker, Hook hook);

	Class<?> resolve(String target);
}

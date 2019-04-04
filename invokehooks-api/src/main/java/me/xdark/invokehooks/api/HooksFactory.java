package me.xdark.invokehooks.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface HooksFactory {

	/**
	 * Creates a hook for {@link java.lang.reflect.Method}
	 *
	 * @param rtype invocation return type
	 * @param method the target
	 * @param controller invocation controller
	 * @return hook instance
	 */
	<R> Hook createMethodHook(Class<R> rtype, Method method, Invoker<R> controller);

	/**
	 * Creates a hook for {@link java.lang.reflect.Field}
	 *
	 * @return hook instance
	 * @see FieldGetController
	 * @see FieldSetController
	 */
	Hook createFieldHook(Field field, FieldGetController getController,
			FieldSetController setController);

	/**
	 * Creates a hook for {@link java.lang.reflect.Constructor}
	 *
	 * @param rtype constructor return type
	 * @param constructor the target
	 * @param controller invocation controller
	 * @return hook instance
	 */
	<R> Hook createConstructorHook(Class<R> rtype, Constructor<R> constructor, Invoker<R> controller);
}

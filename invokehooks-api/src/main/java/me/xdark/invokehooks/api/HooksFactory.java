package me.xdark.invokehooks.api;

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
	<R> MethodHook<R> createMethodHook(Class<R> rtype, Method method, Invoker<R> controller);
}

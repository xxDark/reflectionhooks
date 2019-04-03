package me.xdark.invokehooks.api;

import java.lang.reflect.Method;

public interface HooksFactory {

	/**
	 * Creates a hook for {@link java.lang.reflect.Method}
	 *
	 * @param method the target
	 * @param controller invocation controller
	 * @return hook instance
	 */
	<R> MethodHook<R> createMethodHook(Method method, Invoker<R> controller);
}

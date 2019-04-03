package me.xdark.invokehooks.api;

public interface Invoker<R> {

	/**
	 * Inovkes the method
	 *
	 * @param parent parent invoker, may be {@code null}
	 * @param handle object instance
	 * @param args arguments of method
	 * @return invocation result
	 */
	R invoke(Invoker<R> parent, Object handle, Object... args);
}

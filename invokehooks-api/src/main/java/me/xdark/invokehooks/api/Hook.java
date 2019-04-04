package me.xdark.invokehooks.api;

public interface Hook {

	/**
	 * Hooks target {@link java.lang.reflect.Member}
	 */
	void hook();

	/**
	 * Returns {@code true} if hook is set
	 */
	boolean isHooked();

	/**
	 * Removes hook from target {@link java.lang.reflect.Member}
	 */
	void unhook();
}

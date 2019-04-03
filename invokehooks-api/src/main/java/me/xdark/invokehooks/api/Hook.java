package me.xdark.invokehooks.api;

public interface Hook {

	/**
	 * Hooks target {@link java.lang.reflect.Member}
	 *
	 * @throws IllegalStateException if target is already hooked
	 */
	void hook();

	/**
	 * Returns {@code true} if hook is set
	 */
	boolean isHooked();

	/**
	 * Removes hook from target {@link java.lang.reflect.Member}
	 *
	 * @throws IllegalStateException if hook not set
	 */
	void unhook();
}

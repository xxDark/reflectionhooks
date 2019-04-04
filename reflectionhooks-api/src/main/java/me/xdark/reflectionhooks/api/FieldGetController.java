package me.xdark.reflectionhooks.api;

@FunctionalInterface
public interface FieldGetController {

	/**
	 * Called when something tries to get field value
	 *
	 * @param parent parent controller, may be {@code null}
	 * @param handle the field holder
	 */
	Object get(FieldGetController parent, Object handle);
}

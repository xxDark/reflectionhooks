package me.xdark.invokehooks.api;

@FunctionalInterface
public interface FieldSetController {

	/**
	 * Sets a value of field
	 *
	 * @param parent parent controller, may be {@code null}
	 * @param handle the field holder
	 * @param value new value
	 */
	void set(FieldSetController parent, Object handle, Object value);
}

package me.xdark.reflectionhooks.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import me.xdark.reflectionhooks.api.Hook;
import me.xdark.reflectionhooks.api.HooksFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestField {

	private int value = 0;

	@Test
	public void testField() throws NoSuchFieldException, IllegalAccessException {
		HooksFactory factory = new DefaultHooksFactory();
		Field field = TestField.class.getDeclaredField("value");
		Hook hook = factory.createFieldHook(field, (parent, handle) -> 13,
				(parent, handle, value1) -> parent.set(null, handle, 5));
		hook.hook();
		field.set(this, 3);
		assertEquals(value, 5);
		int get = field.getInt(this);
		Assertions
				.assertEquals(get, 13);
		hook.unhook();
		field.set(this, 3);
		assertEquals(value, 3);
		get = field.getInt(this);
		assertEquals(get, 3);
	}
}

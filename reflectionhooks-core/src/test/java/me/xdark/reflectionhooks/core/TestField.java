package me.xdark.reflectionhooks.core;

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
		Assertions.assertEquals(value, 5, "Expected '5' for field value, but got: '" + value + '\'');
		int get = field.getInt(this);
		Assertions
				.assertEquals(get, 13, "Expected '13' as returned get value, but got: '" + get + '\'');
		hook.unhook();
		field.set(this, 3);
		Assertions.assertEquals(value, 3, "Expected '3' for field value, but got: '" + value + '\'');
		get = field.getInt(this);
		Assertions
				.assertEquals(get, 3, "Expected '3' as returned get value, but got: '" + get + '\'');
	}
}

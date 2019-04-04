package me.xdark.reflectionhooks.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import me.xdark.reflectionhooks.api.Hook;
import me.xdark.reflectionhooks.api.HooksFactory;
import org.junit.jupiter.api.Test;

public class TestMethod {

	@Test
	public void testReturn()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		HooksFactory factory = new DefaultHooksFactory();
		Method method = TestMethod.class.getDeclaredMethod("someMethod");
		Hook hook = factory.createMethodHook(String.class, method, (parent, handle, args) -> "World!");
		hook.hook();
		String returnment = (String) method.invoke(this);
		assertEquals(returnment, "World!");
		hook.unhook();
		returnment = (String) method.invoke(this);
		assertEquals(returnment, "Hello, ");
	}

	@Test
	public void testArguments()
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		HooksFactory factory = new DefaultHooksFactory();
		Method method = TestMethod.class.getDeclaredMethod("self", String.class);
		Hook hook = factory.createMethodHook(String.class, method, (parent, handle, args) -> {
			args[0] = "World!";
			return parent.invoke(null, handle, args);
		});
		hook.hook();
		String returnment = (String) method.invoke(this, "Hello, ");
		assertEquals("World!", returnment);
		hook.unhook();
		returnment = (String) method.invoke(this, "Hello, ");
		assertEquals("Hello, ", returnment);
	}

	public String someMethod() {
		return "Hello, ";
	}

	public String self(String str) {
		return str;
	}
}

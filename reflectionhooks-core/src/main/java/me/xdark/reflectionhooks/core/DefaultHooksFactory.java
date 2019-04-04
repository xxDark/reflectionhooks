package me.xdark.reflectionhooks.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.xdark.reflectionhooks.api.FieldGetController;
import me.xdark.reflectionhooks.api.FieldSetController;
import me.xdark.reflectionhooks.api.Hook;
import me.xdark.reflectionhooks.api.HooksFactory;
import me.xdark.reflectionhooks.api.Invoker;

public final class DefaultHooksFactory implements HooksFactory {

	@Override
	public <R> Hook createMethodHook(Class<R> rtype, Method method, Invoker<R> controller) {
		return Environment.createMethodHook0(method, controller);
	}

	@Override
	public Hook createFieldHook(Field field, FieldGetController getController,
			FieldSetController setController) {
		return Environment.createFieldHook0(field, getController, setController);
	}

	@Override
	public <R> Hook createConstructorHook(Class<R> rtype, Constructor<R> constructor,
			Invoker<R> controller) {
		return Environment.createConstructorHook0(constructor, controller);
	}

	static {
		Environment.prepare();
	}
}

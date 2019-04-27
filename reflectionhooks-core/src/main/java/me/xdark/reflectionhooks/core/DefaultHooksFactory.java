package me.xdark.reflectionhooks.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.xdark.reflectionhooks.api.*;

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

	@Override
	public void createMethodInvokeHook(InvokeMethodController controller) {
		Environment.INVOKE_METHOD_CONTROLLERS.add(controller);
	}

	@Override
	public void createFieldInvokeHook(InvokeFieldController controller) {
		Environment.INVOKE_FIELD_CONTROLLERS.add(controller);
	}

	static {
		Environment.prepare();
	}
}

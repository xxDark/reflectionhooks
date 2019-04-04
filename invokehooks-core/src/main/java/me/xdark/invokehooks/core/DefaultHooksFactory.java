package me.xdark.invokehooks.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import me.xdark.invokehooks.api.FieldGetController;
import me.xdark.invokehooks.api.FieldSetController;
import me.xdark.invokehooks.api.Hook;
import me.xdark.invokehooks.api.HooksFactory;
import me.xdark.invokehooks.api.Invoker;

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

	static {
		Environment.prepare();
	}
}

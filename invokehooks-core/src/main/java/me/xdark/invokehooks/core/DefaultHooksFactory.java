package me.xdark.invokehooks.core;

import java.lang.reflect.Method;
import me.xdark.invokehooks.api.HooksFactory;
import me.xdark.invokehooks.api.Invoker;
import me.xdark.invokehooks.api.MethodHook;

public final class DefaultHooksFactory implements HooksFactory {

	@Override
	public <R> MethodHook<R> createMethodHook(Method method, Invoker<R> controller) {
		return Environment.createMethodHook0(method, controller);
	}

	static {
		Environment.prepare();
	}
}
package me.xdark.invokehooks.core;

import java.lang.reflect.Method;
import me.xdark.invokehooks.api.Hook;
import me.xdark.invokehooks.api.HooksFactory;
import me.xdark.invokehooks.api.Invoker;

public final class DefaultHooksFactory implements HooksFactory {

	@Override
	public <R> Hook createMethodHook(Class<R> rtype, Method method, Invoker<R> controller) {
		return Environment.createMethodHook0(rtype, method, controller);
	}

	static {
		Environment.prepare();
	}
}

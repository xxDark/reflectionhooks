package me.xdark.invokehooks.api;

public class BaseHook implements Hook {

	protected boolean hooked;

	@Override
	public void hook() {
		hooked = true;
	}

	@Override
	public boolean isHooked() {
		return hooked;
	}

	@Override
	public void unhook() {
		hooked = false;
	}
}

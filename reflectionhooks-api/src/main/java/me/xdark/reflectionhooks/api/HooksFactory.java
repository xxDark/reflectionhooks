package me.xdark.reflectionhooks.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface HooksFactory {

    /**
     * Creates a hook for {@link java.lang.reflect.Method}
     *
     * @param rtype      invocation return type
     * @param method     the target
     * @param controller invocation controller
     * @return hook instance
     */
    <R> Hook createMethodHook(Class<R> rtype, Method method, Invoker<R> controller);

    /**
     * Creates a hook for {@link java.lang.reflect.Field}
     *
     * @return hook instance
     * @see FieldGetController
     * @see FieldSetController
     */
    Hook createFieldHook(Field field, FieldGetController getController,
                         FieldSetController setController);

    /**
     * Creates a hook for {@link java.lang.reflect.Constructor}
     *
     * @param rtype       constructor return type
     * @param constructor the target
     * @param controller  invocation controller
     * @return hook instance
     */
    <R> Hook createConstructorHook(Class<R> rtype, Constructor<R> constructor, Invoker<R> controller);

    /**
     * Creates a method hook for {@link java.lang.invoke.MethodHandles.Lookup}
     * WARNING: this types of hook CANNOT be uninstalled
     */
    void createMethodInvokeHook(InvokeMethodController controller);

    /**
     * Creates a method hook for {@link java.lang.invoke.MethodHandles.Lookup}
     * WARNING: this types of hook CANNOT be uninstalled
     */
    void createConstructorInvokeHook(InvokeMethodController controller);

    /**
     * Creates a field hook for {@link java.lang.invoke.MethodHandles.Lookup}
     * WARNING: this types of hook CANNOT be uninstalled
     */
    void createFieldInvokeHook(InvokeFieldController controller);
}

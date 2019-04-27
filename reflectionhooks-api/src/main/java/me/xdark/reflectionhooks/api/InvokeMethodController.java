package me.xdark.reflectionhooks.api;

import java.lang.invoke.MethodType;
import java.lang.ref.SoftReference;

@FunctionalInterface
public interface InvokeMethodController {

    /**
     * Fired when find** is invoked
     * Fired on: findVirtual, findStatic, findSpecial, findConstructor
     *
     * @param type     the type of call
     * @param classRef the class reference
     * @param nameRef  the name reference
     * @param typeRef  the descriptor reference
     */
    void onFindCalled(FindType type, SoftReference<Class<?>> classRef, SoftReference<String> nameRef, SoftReference<MethodType> typeRef);
}

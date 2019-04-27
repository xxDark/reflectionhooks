package me.xdark.reflectionhooks.api;

import java.lang.invoke.MethodType;

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
    void onFindCalled(FindType type, NonDirectReference<Class<?>> classRef, NonDirectReference<String> nameRef, NonDirectReference<MethodType> typeRef);
}

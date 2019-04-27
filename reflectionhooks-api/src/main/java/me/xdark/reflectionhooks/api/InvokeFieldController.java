package me.xdark.reflectionhooks.api;

import java.lang.ref.SoftReference;

@FunctionalInterface
public interface InvokeFieldController {

    /**
     * Fired when find** is invoked
     * Fired on: findGetter, findSetter, findStaticGetter, findStaticSetter
     *
     * @param type     the type of call
     * @param classRef the class reference
     * @param nameRef  the name reference
     * @param typeRef  the field type reference
     */
    void onFindCalled(FindType type, SoftReference<Class<?>> classRef, SoftReference<String> nameRef, SoftReference<Class<?>> typeRef);
}

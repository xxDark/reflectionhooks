# reflectionhooks
WARNING: java.lang.invoke hooks can only be used if jar is appended into bootstrap class path
```Java
public class Test {

	private static int val = 1;

	private Test(String str) {
		System.err.println(str);
	}

	public static void main(String[] a) throws Throwable {
		HooksFactory factory = new DefaultHooksFactory();
		Hook hook = factory
				.createMethodHook(Void.class, Test.class.getDeclaredMethod("hi", String.class),
						(parent, handle, args) -> {
							System.out
									.println("Hello, World!, arg0 is: "
											+ args[0] + " and my instance is " + handle);
							parent.invoke(null, handle, args);
							return null;
						});
		hook.hook();
		Test.class.getDeclaredMethod("hi", String.class)
				.invoke(null, "Hi!");

		Hook hook1 = factory.createFieldHook(Test.class.getDeclaredField("val"),
				(parent, handle) -> {
					System.out.println("get called from " + handle);
					return parent.get(null, handle);
				}, (parent, handle, value) -> {
					System.out.println("set called from " + handle + ", to " + value);
					parent.set(null, handle, value);
				});
		hook1.hook();
		Field field = Test.class.getDeclaredField("val");
		field.setInt(null, 5);

		Hook hook2 = factory
				.createConstructorHook(Test.class, Test.class.getDeclaredConstructor(String.class),
						(parent, handle, args) -> {
							System.err.println("Constructor called!");
							args[0] = "World!";
							return parent.invoke(null, handle, args);
						});
		hook2.hook();
		Test.class.getDeclaredConstructor(String.class).newInstance("Hello, ");
	}

	public static void hi(String str) {
		System.out.println(str);
	}
}
```

```Java
public class Test {

    public static void main(String[] args) throws Throwable {
        JavaInvokeInjector.inject();
        HooksFactory factory = new DefaultHooksFactory();
        factory.createMethodInvokeHook((type, classRef, nameRef, typeRef) -> {
            System.out.println("Call: " + classRef.get() + ' ' + nameRef.get() + ' ' + typeRef.get());
            nameRef.set("hooked");
        });
        MethodHandles.publicLookup().findStatic(Test.class, "first", MethodType.methodType(void.class))
                .invokeExact();
    }

    public static void first() {
        System.out.println("Hello, ");
    }

    public static void hooked() {
        System.out.println("World!");
    }
}
```

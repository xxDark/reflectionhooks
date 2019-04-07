# reflectionhooks
TODO: java.lang.invoke hooks
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

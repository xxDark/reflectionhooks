package me.xdark.reflectionhooks.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

public final class JavaInvokeInjector {

	// A bit explanation here:
	// We can't use lambdas there because it will trigger java.lang.invoke classes initialization
	public static void inject() throws NoSuchFieldException, IllegalAccessException, IOException {
		// Let's hope that no one access java.lang.invoke.* before us.
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		Unsafe unsafe = (Unsafe) field.get(null);
		{
			byte[] transformed = transform("java/lang/invoke/MethodHandles.class",
					new Consumer<ClassWriter>() {
						@Override
						public void accept(ClassWriter cw) {
							cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "java/lang/invoke/MethodHandles", null,
									"java/lang/Object", null);
						}
					});
			defineClass(unsafe, "java.lang.invoke.MethodHandles", transformed);
		}
		{
			byte[] transformed = transform("java/lang/invoke/MethodHandles$Lookup.class",
					new Consumer<ClassWriter>() {
						@Override
						public void accept(ClassWriter cw) {
							cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "java/lang/invoke/MethodHandles$Lookup", null,
									"java/lang/Object", null);
						}
					});
			defineClass(unsafe, "java.lang.invoke.MethodHandles$Lookup", transformed);
		}
	}

	private static void defineClass(Unsafe unsafe, String className, byte[] code) {
		unsafe.defineClass(className, code, 0, code.length, null, null);
	}

	private static byte[] transform(String resource, Consumer<ClassWriter> consumer)
			throws IOException {
		ClassReader cr;
		try (InputStream in = ClassLoader.getSystemResourceAsStream(resource)) {
			cr = new ClassReader(in);
		}
		ClassWriter cw = new ClassWriter(cr, 0);
		consumer.accept(cw);
		return cw.toByteArray();
	}
}

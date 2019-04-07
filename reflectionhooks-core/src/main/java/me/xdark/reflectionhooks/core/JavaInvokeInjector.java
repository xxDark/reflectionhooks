package me.xdark.reflectionhooks.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

public final class JavaInvokeInjector {

	public static void inject() throws NoSuchFieldException, IllegalAccessException, IOException {
		// Let's hope that no one access java.lang.invoke.* before us.
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		Unsafe unsafe = (Unsafe) field.get(null);
		ClassReader cr;
		try (InputStream in = ClassLoader
				.getSystemResourceAsStream("java/lang/invoke/MethodHandles.class")) {
			cr = new ClassReader(in);
		}
		ClassWriter cw = new ClassWriter(cr, 0);
		cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "java/lang/invoke/MethodHandles", null,
				"java/lang/Object", null);
		byte[] transformed = cw.toByteArray();
		unsafe.defineClass(null, transformed, 0, transformed.length, null, null);
	}
}

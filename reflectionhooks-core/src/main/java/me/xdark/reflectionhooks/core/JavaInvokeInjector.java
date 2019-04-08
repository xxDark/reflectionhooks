package me.xdark.reflectionhooks.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import sun.misc.Unsafe;

public final class JavaInvokeInjector {

	// A bit explanation here:
	// We can't use lambdas there because it will trigger java.lang.invoke classes initialization
	public static void inject() throws Throwable {
		// Let's hope that no one access java.lang.invoke.* before us.
		Field field = Unsafe.class.getDeclaredField("theUnsafe");
		field.setAccessible(true);
		Unsafe unsafe = (Unsafe) field.get(null);
		{
			byte[] transformed = transform("java/lang/invoke/MethodHandles.class",
					new Consumer<ClassNode>() {
						@Override
						public void accept(ClassNode cw) {
							cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "java/lang/invoke/MethodHandles", null,
									"java/lang/Object", null);
						}
					});
			defineClass(unsafe, "java.lang.invoke.MethodHandles", transformed, null);
		}
		{
			byte[] transformed = transform("java/lang/invoke/MethodHandles$Lookup.class",
					new Consumer<ClassNode>() {
						@Override
						public void accept(ClassNode cw) {
							cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
									"java/lang/invoke/MethodHandles$Lookup",
									null,
									"java/lang/Object", null);
						}
					});
			defineClass(unsafe, "java.lang.invoke.MethodHandles$Lookup", transformed,
					MethodHandles.class);
		}
	}

	private static void defineClass(Unsafe unsafe, String className, byte[] code, Class<?> root) {
		if (root == null) {
			unsafe.defineClass(className, code, 0, code.length, null, null);
		} else {
			unsafe.defineAnonymousClass(root, code, null);
		}
	}

	private static byte[] transform(String resource, Consumer<ClassNode> consumer)
			throws IOException {
		ClassReader cr;
		try (InputStream in = ClassLoader.getSystemResourceAsStream(resource)) {
			cr = new ClassReader(in);
		}
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		consumer.accept(cn);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cn.accept(cw);
		return cw.toByteArray();
	}
}

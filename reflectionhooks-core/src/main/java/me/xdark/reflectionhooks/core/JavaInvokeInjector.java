package me.xdark.reflectionhooks.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import sun.misc.Unsafe;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

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
                            // We can't clear final bit here, so we directly inject into
                            // findVirtual, findStatic & so on
                            injectMethod(findMethodByName(cw.methods, "findVirtual"));
                            injectMethod(findMethodByName(cw.methods, "findStatic"));
                        }
                    });
            defineClass(unsafe, "java.lang.invoke.MethodHandles$Lookup", transformed, null);
        }
    }

    private static MethodNode findMethodByName(List<MethodNode> nodes, String name) {
        for (MethodNode mn : nodes) {
            if (name.equals(mn.name)) {
                return mn;
            }
        }
        throw new RuntimeException("MethodNode " + name + " was not found!");
    }

    private static void injectMethod(MethodNode mn) {
        InsnList list = mn.instructions;
        AbstractInsnNode first = list.getFirst();
        createRef(list, first, 1, 4);
        createRef(list, first, 2, 5);
        createRef(list, first, 3, 6);
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 4));
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 5));
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 6));
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/xdark/reflectionhooks/core/Environment", "onMethodHook", "(Ljava/lang/ref/SoftReference;Ljava/lang/ref/SoftReference;Ljava/lang/ref/SoftReference;)V", false));
    }

    private static void createRef(InsnList list, AbstractInsnNode first, int aload, int astore) {
        list.insertBefore(first, new TypeInsnNode(Opcodes.NEW, "java/lang/ref/SoftReference"));
        list.insertBefore(first, new InsnNode(Opcodes.DUP));
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, aload));
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/ref/SoftReference", "<init>", "(Ljava/lang/Object;)V", false));
        list.insertBefore(first, new VarInsnNode(Opcodes.ASTORE, astore));
    }

    private static void defineClass(Unsafe unsafe, String className, byte[] code, Class<?> root) {
        if (root == null) {
            unsafe.defineClass(className, code, 0, code.length, null, null);
        } else {
            unsafe.defineAnonymousClass(root, code, null);
        }
    }

    private static int next = 0;

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

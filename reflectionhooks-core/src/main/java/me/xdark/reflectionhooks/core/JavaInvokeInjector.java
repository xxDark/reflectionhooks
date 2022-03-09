package me.xdark.reflectionhooks.core;

import jdk.internal.misc.Unsafe;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
                            injectUnreflectMethod(findMethodByNameAndDesc(cw.methods, "unreflect", "(Ljava/lang/reflect/Method;)Ljava/lang/invoke/MethodHandle;"));
                        }
                    });
            Files.write(Paths.get(".").resolve("Test.class"), transformed, StandardOpenOption.CREATE);
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

    private static MethodNode findMethodByNameAndDesc(List<MethodNode> nodes, String name, String desc) {
        for (MethodNode mn : nodes) {
            if (name.equals(mn.name) && desc.equals(mn.desc)) {
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
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/xdark/reflectionhooks/core/Environment", "onMethodHook", "(Lme/xdark/reflectionhooks/api/NonDirectReference;Lme/xdark/reflectionhooks/api/NonDirectReference;Lme/xdark/reflectionhooks/api/NonDirectReference;)V", false));
        getAndSet(list, first, 4, 1);
        getAndSet(list, first, 5, 2);
        getAndSet(list, first, 6, 3);
    }

    private static void injectUnreflectMethod(MethodNode mn) {
        InsnList list = mn.instructions;
        AbstractInsnNode first = list.getFirst();
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "getReturnType", "()Ljava/lang/Class;", false));
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "getParameterTypes", "()[Ljava/lang/Class;", false));
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType", "methodType", "(Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;", false));
        list.insertBefore(first, new VarInsnNode(Opcodes.ASTORE, 2));
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "getDeclaringClass", "()Ljava/lang/Class;", false));
        list.insertBefore(first, new VarInsnNode(Opcodes.ASTORE, 3));
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "getName", "()Ljava/lang/String;", false));
        list.insertBefore(first, new VarInsnNode(Opcodes.ASTORE, 4));
        // TODO replace
    }

    private static void getAndSet(InsnList list, AbstractInsnNode first, int aload, int astore) {
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, aload));
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/xdark/reflectionhooks/api/NonDirectReference", "get", "()Ljava/lang/Object;", false));
        list.insertBefore(first, new VarInsnNode(Opcodes.ASTORE, astore));
    }

    private static void createRef(InsnList list, AbstractInsnNode first, int aload, int astore) {
        list.insertBefore(first, new TypeInsnNode(Opcodes.NEW, "me/xdark/reflectionhooks/api/NonDirectReference"));
        list.insertBefore(first, new InsnNode(Opcodes.DUP));
        list.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, aload));
        list.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESPECIAL, "me/xdark/reflectionhooks/api/NonDirectReference", "<init>", "(Ljava/lang/Object;)V", false));
        list.insertBefore(first, new VarInsnNode(Opcodes.ASTORE, astore));
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

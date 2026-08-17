package me.shvaich.moremwe.asm.transformers;

import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.asm.IClassNodeTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public interface MyTransformer extends IClassNodeTransformer {
    default String mapMethodName(ClassNode classNode, MethodNode methodNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, methodNode.name, methodNode.desc);
    }

    default String mapMethodInsnName(AbstractInsnNode insnNode) {
        final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
    }

    default String mapClassName(String className) {
        return FMLDeobfuscatingRemapper.INSTANCE.map(className);
    }

    default String mapMethodDesc(MethodNode methodNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(methodNode.desc);
    }

    default String mapMethodInsnDesc(AbstractInsnNode insnNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(((MethodInsnNode) insnNode).desc);
    }

    default boolean checkMcMethod(ClassNode classNode, MethodNode methodNode, String name, String desc) {
        return mapMethodName(classNode, methodNode).equals(name) && mapMethodDesc(methodNode).equals(desc);
    }

    default boolean checkMcMethod(ClassNode classNode, MethodNode methodNode, String mcpName, String srgName, String desc) {
        return checkMcMethod(classNode, methodNode, MWEApi.Asm.isObfuscatedEnvironment() ? srgName : mcpName, desc);
    }

    default boolean checkMcMethodInsn(AbstractInsnNode insnNode, int opcode, String owner, String name, String desc) {
        return insnNode.getOpcode() == opcode
                && insnNode instanceof MethodInsnNode
                && mapClassName(((MethodInsnNode) insnNode).owner).equals(owner)
                && mapMethodInsnName(insnNode).equals(name)
                && mapMethodInsnDesc(insnNode).equals(desc);
    }

    default boolean checkMcMethodInsn(AbstractInsnNode insnNode, int opcode, String owner, String mcpName, String srgName, String desc) {
        return checkMcMethodInsn(insnNode, opcode, owner, MWEApi.Asm.isObfuscatedEnvironment() ? srgName : mcpName, desc);
    }

    default FieldInsnNode getMcFieldInsnNode(int opcode, String owner, String mcpName, String srgName, String desc) {
        return new FieldInsnNode(opcode, owner, MWEApi.Asm.isObfuscatedEnvironment() ? srgName : mcpName, desc);
    }

    default MethodInsnNode getMcMethodInsnNode(int opcode, String owner, String mcpName, String srgName, String desc, boolean isInterface) {
        return new MethodInsnNode(opcode, owner, MWEApi.Asm.isObfuscatedEnvironment() ? srgName : mcpName, desc, isInterface);
    }

    default MethodNode getMcOverrideMethod(int opcode, String mcpName, String srgName, String desc) {
        final MethodNode m = new MethodNode(
                opcode,
                MWEApi.Asm.isObfuscatedEnvironment() ? mcpName : srgName,
                desc,
                null,
                null
        );
        if (m.visibleAnnotations == null) {
            m.visibleAnnotations = new ArrayList<>(1);
        }
        m.visibleAnnotations.add(new AnnotationNode("Ljava/lang/Override;"));
        return m;
    }

    default InsnList getBooleanReturnInsn(boolean value) {
        final InsnList list = new InsnList();
        list.add(new InsnNode(value ? ICONST_1 : ICONST_0));
        list.add(new InsnNode(IRETURN));
        return list;
    }

    default String getHookClass(String name) {
        return "me/shvaich/moremwe/asm/hooks/" + name;
    }
}

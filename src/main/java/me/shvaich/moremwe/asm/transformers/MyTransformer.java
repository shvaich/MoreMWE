package me.shvaich.moremwe.asm.transformers;

import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.asm.IClassNodeTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.ListIterator;

public abstract class MyTransformer implements IClassNodeTransformer {

    /** Allows override */
    protected String getHookClass(String name) {
        return "me/shvaich/moremwe/asm/hooks/" + name;
    }

    protected static String mapMethodName(ClassNode classNode, MethodNode methodNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, methodNode.name, methodNode.desc);
    }

    protected static String mapFieldInsnName(AbstractInsnNode insnNode) {
        final FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
        return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(fieldInsnNode.owner, fieldInsnNode.name, fieldInsnNode.desc);
    }

    protected static String mapMethodInsnName(AbstractInsnNode insnNode) {
        final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
    }

    protected static String mapClassName(String className) {
        return FMLDeobfuscatingRemapper.INSTANCE.map(className);
    }

    protected static String mapMethodDesc(MethodNode methodNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(methodNode.desc);
    }

    protected static String mapFieldInsnDesc(AbstractInsnNode insnNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapDesc(((FieldInsnNode) insnNode).desc);
    }

    protected static String mapMethodInsnDesc(AbstractInsnNode insnNode) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(((MethodInsnNode) insnNode).desc);
    }

    protected static boolean checkMcMethod(ClassNode classNode, MethodNode methodNode, String name, String desc) {
        return mapMethodName(classNode, methodNode).equals(name) && mapMethodDesc(methodNode).equals(desc);
    }

    protected static boolean checkMcMethod(ClassNode classNode, MethodNode methodNode, String mcpName, String srgName, String desc) {
        return checkMcMethod(classNode, methodNode, getName(mcpName, srgName), desc);
    }

    protected static boolean checkMcFieldInsn(AbstractInsnNode insnNode, int opcode, String owner, String name, String desc) {
        return insnNode.getOpcode() == opcode
                && insnNode instanceof FieldInsnNode
                && mapClassName(((FieldInsnNode) insnNode).owner).equals(owner)
                && mapFieldInsnName(insnNode).equals(name)
                && mapFieldInsnDesc(insnNode).equals(desc);
    }

    protected static boolean checkMcFieldInsn(AbstractInsnNode insnNode, int opcode, String owner, String mcpName, String srgName, String desc) {
        return checkMcFieldInsn(insnNode, opcode, owner, getName(mcpName, srgName), desc);
    }

    protected static boolean checkMcMethodInsn(AbstractInsnNode insnNode, int opcode, String owner, String name, String desc) {
        return insnNode.getOpcode() == opcode
                && insnNode instanceof MethodInsnNode
                && mapClassName(((MethodInsnNode) insnNode).owner).equals(owner)
                && mapMethodInsnName(insnNode).equals(name)
                && mapMethodInsnDesc(insnNode).equals(desc);
    }

    protected static boolean checkMcMethodInsn(AbstractInsnNode insnNode, int opcode, String owner, String mcpName, String srgName, String desc) {
        return checkMcMethodInsn(insnNode, opcode, owner, getName(mcpName, srgName), desc);
    }

    protected static FieldInsnNode getMcFieldInsnNode(int opcode, String owner, String mcpName, String srgName, String desc) {
        return new FieldInsnNode(opcode, owner, getName(mcpName, srgName), desc);
    }

    protected static MethodInsnNode getMcMethodInsnNode(int opcode, String owner, String mcpName, String srgName, String desc, boolean isInterface) {
        return new MethodInsnNode(opcode, owner, getName(mcpName, srgName), desc, isInterface);
    }

    protected static MethodNode getMcOverrideMethod(int opcode, String mcpName, String srgName, String desc) {
        final MethodNode m = new MethodNode(
                opcode,
                getName(mcpName, srgName),
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

    protected static InsnList getBooleanReturnInsn(boolean value) {
        final InsnList list = new InsnList();
        list.add(new InsnNode(value ? ICONST_1 : ICONST_0));
        list.add(new InsnNode(IRETURN));
        return list;
    }

    private interface InstructionChecker {
        boolean check(AbstractInsnNode insnNode, int opcode, String owner, String name, String desc);
    }

    protected static MethodInsnNode findMcMethodInsnNode(MethodNode methodNode, int opcode, String owner, String name, String desc) {
        return findMcInsnNode(MyTransformer::checkMcMethodInsn, methodNode, opcode, owner, name, desc);
    }

    protected static MethodInsnNode findMcMethodInsnNode(MethodNode methodNode, int opcode, String owner, String mcpName, String srgName, String desc) {
        return findMcMethodInsnNode(methodNode, opcode, owner, getName(mcpName, srgName), desc);
    }

    protected static FieldInsnNode findMcFieldInsnNode(MethodNode methodNode, int opcode, String owner, String name, String desc) {
        return findMcInsnNode(MyTransformer::checkMcFieldInsn, methodNode, opcode, owner, name, desc);
    }

    protected static FieldInsnNode findMcFieldInsnNode(MethodNode methodNode, int opcode, String owner, String mcpName, String srgName, String desc) {
        return findMcFieldInsnNode(methodNode, opcode, owner, getName(mcpName, srgName), desc);
    }


    private static <T extends AbstractInsnNode> T findMcInsnNode(InstructionChecker checker, MethodNode methodNode, int opcode, String owner, String name, String desc) {
        final ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        while (iterator.hasNext()) {
            final AbstractInsnNode insnNode = iterator.next();
            if (checker.check(
                    insnNode,
                    opcode,
                    owner,
                    name,
                    desc
            )) {
                return (T) insnNode;
            }
        }
        return null;
    }

    private static String getName(String developmentName, String productionName) {
        return MWEApi.Asm.isObfuscatedEnvironment() ? productionName : developmentName;
    }
}

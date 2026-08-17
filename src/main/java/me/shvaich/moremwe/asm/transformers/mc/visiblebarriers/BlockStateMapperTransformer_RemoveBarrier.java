package me.shvaich.moremwe.asm.transformers.mc.visiblebarriers;

import fr.alexdoru.mwe.api.asm.InjectionCallback;
import me.shvaich.moremwe.asm.transformers.MyTransformer;
import org.objectweb.asm.tree.*;

public class BlockStateMapperTransformer_RemoveBarrier implements MyTransformer {
    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.renderer.block.statemap.BlockStateMapper"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionCallback status) {
        status.setInjectionPoints(1);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMcMethod(classNode, methodNode, "registerBuiltInBlocks", "func_178448_a", "([Lnet/minecraft/block/Block;)V")) {
                for (AbstractInsnNode insnNode = methodNode.instructions.getLast(); insnNode != null; insnNode = insnNode.getPrevious()) {
                    if (insnNode.getOpcode() == RETURN) {
                        final InsnList list = new InsnList();

                        list.add(new VarInsnNode(ALOAD, 0));

                        list.add(getMcFieldInsnNode(
                                GETFIELD,
                                "net/minecraft/client/renderer/block/statemap/BlockStateMapper",
                                "setBuiltInBlocks",
                                "field_178449_b",
                                "Ljava/util/Set;"
                        ));

                        list.add(getMcFieldInsnNode(
                                GETSTATIC,
                                "net/minecraft/init/Blocks",
                                "barrier",
                                "field_180401_cv",
                                "Lnet/minecraft/block/Block;"
                        ));

                        list.add(new MethodInsnNode(
                                INVOKEINTERFACE,
                                "java/util/Set",
                                "remove",
                                "(Ljava/lang/Object;)Z",
                                true
                        ));

                        list.add(new InsnNode(POP));

                        methodNode.instructions.insertBefore(insnNode, list);
                        status.addInjection();
                        break;
                    }
                }
            }
        }
    }

}
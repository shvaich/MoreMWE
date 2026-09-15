package me.shvaich.moremwe.asm.transformers.mc.visiblebarriers;

import fr.alexdoru.mwe.api.asm.InjectionCallback;
import me.shvaich.moremwe.asm.transformers.MyTransformer;
import org.objectweb.asm.tree.*;

public class BlockBarrierTransformer_VisibleBarrier extends MyTransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.block.BlockBarrier"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionCallback status) {
        status.setInjectionPoints(3);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMcMethod(classNode, methodNode, "getRenderType", "func_149645_b", "()I")) {
                final InsnList list = new InsnList();

                list.add(new MethodInsnNode(
                        INVOKESTATIC,
                        "me/shvaich/moremwe/features/VisibleBarriers",
                        "isActive",
                        "()Z",
                        false
                ));

                final LabelNode continueLabel = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, continueLabel));

                list.add(new InsnNode(ICONST_3));
                list.add(new InsnNode(IRETURN));

                list.add(continueLabel);

                methodNode.instructions.insert(list);
                status.addInjection();
            }
        }
        addGetBlockLayer(classNode, status);
        addShouldSideBeRendered(classNode, status);
    }

    private void addGetBlockLayer(ClassNode classNode, InjectionCallback status) {
        final MethodNode method = getMcOverrideMethod(
                ACC_PUBLIC,
                "getBlockLayer",
                "func_180664_k",
                "()Lnet/minecraft/util/EnumWorldBlockLayer;"
        );

        method.instructions.add(getMcFieldInsnNode(
                GETSTATIC,
                "net/minecraft/util/EnumWorldBlockLayer",
                "TRANSLUCENT",
                "TRANSLUCENT",
                "Lnet/minecraft/util/EnumWorldBlockLayer;"
        ));

        method.instructions.add(new InsnNode(ARETURN));
        classNode.methods.removeIf(m -> checkMcMethod(classNode, m, method.name, method.desc));
        classNode.methods.add(method);
        status.addInjection();
    }

    private void addShouldSideBeRendered(ClassNode classNode, InjectionCallback status) {
        final MethodNode method = getMcOverrideMethod(
                ACC_PUBLIC,
                "shouldSideBeRendered",
                "func_176225_a",
                "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"
        );

        method.instructions.add(new VarInsnNode(ALOAD, 1));
        method.instructions.add(new VarInsnNode(ALOAD, 2));

        method.instructions.add(getMcMethodInsnNode(
                INVOKEINTERFACE,
                "net/minecraft/world/IBlockAccess",
                "getBlockState",
                "func_180495_p",
                "(Lnet/minecraft/util/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
                true
        ));

        method.instructions.add(getMcMethodInsnNode(
                INVOKEINTERFACE,
                "net/minecraft/block/state/IBlockState",
                "getBlock",
                "func_177230_c",
                "()Lnet/minecraft/block/Block;",
                true
        ));

        method.instructions.add(new VarInsnNode(ALOAD, 0));

        final LabelNode sameBlock = new LabelNode();
        method.instructions.add(new JumpInsnNode(IF_ACMPEQ, sameBlock));
        method.instructions.add(getBooleanReturnInsn(true));
        method.instructions.add(sameBlock);
        method.instructions.add(getBooleanReturnInsn(false));

        classNode.methods.removeIf(m -> checkMcMethod(classNode, m, method.name, method.desc));
        classNode.methods.add(method);
        status.addInjection();
    }
}
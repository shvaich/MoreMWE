package me.shvaich.moremwe.asm.transformers.mc;

import fr.alexdoru.mwe.api.asm.InjectionCallback;
import me.shvaich.moremwe.asm.transformers.MyTransformer;
import org.objectweb.asm.tree.*;

public class MinecraftTransformer_RightClickGuards extends MyTransformer {
    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.Minecraft"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionCallback status) {
        status.setInjectionPoints(1);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMcMethod(classNode, methodNode, "rightClickMouse", "func_147121_ag", "()V")) {
                AbstractInsnNode insnNode = findMcMethodInsnNode(
                        methodNode,
                        INVOKEVIRTUAL,
                        "net/minecraft/entity/player/InventoryPlayer",
                        "getCurrentItem",
                        "func_70448_g",
                        "()Lnet/minecraft/item/ItemStack;"
                );
                if (insnNode != null && ((insnNode = insnNode.getNext()) instanceof VarInsnNode) && insnNode.getOpcode() == ASTORE) {
                    final InsnList list = new InsnList();

                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new VarInsnNode(ALOAD, ((VarInsnNode) insnNode).var));

                    list.add(new MethodInsnNode(
                            INVOKESTATIC,
                            getHookClass("MinecraftHook_RightClickGuards"),
                            "shouldCancelRightClick",
                            "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/item/ItemStack;)Z",
                            false
                    ));

                    final LabelNode notCanceled = new LabelNode();
                    list.add(new JumpInsnNode(IFEQ, notCanceled));
                    list.add(new InsnNode(RETURN));
                    list.add(notCanceled);
                    methodNode.instructions.insert(insnNode, list);
                    status.addInjection();
                }
            }
        }
    }
}
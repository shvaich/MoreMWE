package me.shvaich.moremwe.asm.transformers.mc;


import fr.alexdoru.mwe.api.asm.InjectionCallback;
import me.shvaich.moremwe.asm.transformers.MyTransformer;
import org.objectweb.asm.tree.*;

public class MinecraftTransformer_ShovelGuard implements MyTransformer {
    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.Minecraft"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionCallback injectionCallback) {
        int injectionPoints = 1;
        injectionCallback.setInjectionPoints(injectionPoints);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMcMethod(classNode, methodNode, "rightClickMouse", "func_147121_ag", "()V")) {
                for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
                    if (injectionPoints > 0 && checkMcMethodInsn(
                            insnNode,
                            INVOKEVIRTUAL,
                            "net/minecraft/client/multiplayer/PlayerControllerMP",
                            "isPlayerRightClickingOnEntity",
                            "func_178894_a",
                            "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/MovingObjectPosition;)Z"
                    )) {
                        final InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new MethodInsnNode(
                                INVOKESTATIC,
                                getHookClass("MinecraftHook_ShovelGuard"),
                                "shouldCancelRightClick",
                                "(Lnet/minecraft/client/Minecraft;)Z",
                                false
                        ));
                        final LabelNode notCanceled = new LabelNode();
                        list.add(new JumpInsnNode(IFEQ, notCanceled));
                        list.add(new InsnNode(RETURN));
                        list.add(notCanceled);
                        methodNode.instructions.insertBefore(insnNode, list);
                        injectionCallback.addInjection();
                        injectionPoints--;
                    }
                }
            }
        }
    }
}

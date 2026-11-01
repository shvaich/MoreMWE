package me.shvaich.moremwe.asm.transformers.mc;

import fr.alexdoru.mwe.api.asm.InjectionCallback;
import me.shvaich.moremwe.asm.transformers.MyTransformer;
import org.objectweb.asm.tree.*;

import java.util.HashMap;

public class MinecraftTransformer_DisableKeysUnpressedInScreensThatAllowPlayerKeyInput extends MyTransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.Minecraft"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionCallback status) {
        status.setInjectionPoints(2);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMcMethod(classNode, methodNode, "displayGuiScreen", "func_147108_a", "(Lnet/minecraft/client/gui/GuiScreen;)V")) {
                final FieldInsnNode setCurrentScreenInsnNode = findMcFieldInsnNode(
                        methodNode,
                        PUTFIELD,
                        "net/minecraft/client/Minecraft",
                        "currentScreen",
                        "field_71462_r",
                        "Lnet/minecraft/client/gui/GuiScreen;"
                );
                if (setCurrentScreenInsnNode != null) {
                    final InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new FieldInsnNode(GETFIELD, setCurrentScreenInsnNode.owner, setCurrentScreenInsnNode.name, setCurrentScreenInsnNode.desc));
                    list.add(setCurrentScreenInsnNode.getPrevious().clone(new HashMap<>()));
                    list.add(new MethodInsnNode(
                            INVOKESTATIC,
                            getHookClass("GuiScreenAllowsPlayerKeyInputHook"),
                            "unPressAllKeysIfNeeded",
                            "(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/gui/GuiScreen;)V",
                            false
                    ));
                    methodNode.instructions.insertBefore(setCurrentScreenInsnNode.getPrevious().getPrevious(), list);
                    status.addInjection();
                }
            }
            else if (checkMcMethod(classNode, methodNode, "setIngameNotInFocus", "func_71364_i", "()V")) {
                final AbstractInsnNode insnNode = findMcMethodInsnNode(
                        methodNode,
                        INVOKESTATIC,
                        "net/minecraft/client/settings/KeyBinding",
                        "unPressAllKeys",
                        "func_74506_a",
                        "()V"
                );
                if (insnNode != null) {
                    final InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(
                            INVOKESTATIC,
                            getHookClass("GuiScreenAllowsPlayerKeyInputHook"),
                            "shouldntUnpressAllKeys",
                            "(Lnet/minecraft/client/Minecraft;)Z",
                            false
                    ));
                    final LabelNode label = new LabelNode();
                    list.add(new JumpInsnNode(IFNE, label));
                    methodNode.instructions.insertBefore(insnNode, list);
                    methodNode.instructions.insert(insnNode, label);
                    status.addInjection();
                }
            }
        }
    }
}

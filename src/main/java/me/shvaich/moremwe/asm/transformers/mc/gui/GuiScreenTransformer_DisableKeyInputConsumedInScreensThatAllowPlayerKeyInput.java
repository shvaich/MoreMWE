package me.shvaich.moremwe.asm.transformers.mc.gui;

import fr.alexdoru.mwe.api.asm.InjectionCallback;
import me.shvaich.moremwe.asm.transformers.MyTransformer;
import org.objectweb.asm.tree.*;

public class GuiScreenTransformer_DisableKeyInputConsumedInScreensThatAllowPlayerKeyInput extends MyTransformer {


    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.gui.GuiScreen"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionCallback status) {
        status.setInjectionPoints(1);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMcMethod(classNode, methodNode, "handleInput", "func_146269_k", "()V")) {
                final AbstractInsnNode insnNode = findMcMethodInsnNode(methodNode, INVOKESTATIC, "org/lwjgl/input/Keyboard", "isCreated", "()Z");
                if (insnNode != null) {
                    final InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(
                            INVOKESTATIC,
                            getHookClass("GuiScreenAllowsPlayerKeyInputHook"),
                            "shouldConsumeKeyInput",
                            "(ZLnet/minecraft/client/gui/GuiScreen;)Z",
                            false
                    ));
                    methodNode.instructions.insert(insnNode, list);
                    status.addInjection();
                }
            }
        }
    }
}

package me.shvaich.moremwe.asm.transformers.mc.chat;

import fr.alexdoru.mwe.api.asm.InjectionCallback;
import me.shvaich.moremwe.asm.transformers.MyTransformer;
import org.objectweb.asm.tree.*;

public class GuiChatTransformer_MWCommands extends MyTransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.gui.GuiChat"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionCallback status) {
        status.setInjectionPoints(3);
        for (final MethodNode methodNode : classNode.methods) {
            if (handleKeyTyped(classNode, methodNode) || handleSendAutocompleteRequest(classNode, methodNode) || handleOnAutocompleteResponse(classNode, methodNode))
                status.addInjection();
        }
    }

    private boolean handleKeyTyped(ClassNode classNode, MethodNode methodNode) {
        if (checkMcMethod(classNode, methodNode, "keyTyped", "func_73869_a", "(CI)V")) {
            final AbstractInsnNode insnNode = findMcMethodInsnNode(
                    methodNode,
                    INVOKEVIRTUAL,
                    "net/minecraft/client/gui/GuiChat",
                    "sendChatMessage",
                    "func_175275_f",
                    "(Ljava/lang/String;)V"
            );
            if (insnNode != null) {
                final InsnList list = new InsnList();
                list.add(new MethodInsnNode(
                        INVOKESTATIC,
                        getHookClass("GuiChatHook_MWCommands"),
                        "isHandledInput",
                        "(Lnet/minecraft/client/gui/GuiScreen;Ljava/lang/String;)Z",
                        false
                ));

                final LabelNode skip = new LabelNode();

                list.add(new JumpInsnNode(IFNE, skip));
                list.add(new VarInsnNode(ALOAD, 0));
                final VarInsnNode inputNode = (VarInsnNode) insnNode.getPrevious();
                list.add(new VarInsnNode(inputNode.getOpcode(), inputNode.var));

                methodNode.instructions.insertBefore(insnNode, list);
                methodNode.instructions.insert(insnNode, skip);
                return true;
            }
        }
        return false;
    }

    private boolean handleSendAutocompleteRequest(ClassNode classNode, MethodNode methodNode) {
        if (checkMcMethod(classNode, methodNode, "sendAutocompleteRequest", "func_146405_a", "(Ljava/lang/String;Ljava/lang/String;)V")) {
            final InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 1));
            list.add(new VarInsnNode(ALOAD, 2));
            list.add(new MethodInsnNode(
                    INVOKESTATIC,
                    getHookClass("GuiChatHook_MWCommands"),
                    "autoComplete",
                    "(Ljava/lang/String;Ljava/lang/String;)V",
                    false
            ));
            methodNode.instructions.insert(list);
            return true;
        }
        return false;
    }

    private boolean handleOnAutocompleteResponse(ClassNode classNode, MethodNode methodNode) {
        if (checkMcMethod(classNode, methodNode, "onAutocompleteResponse", "func_146406_a", "([Ljava/lang/String;)V")) {
            final AbstractInsnNode insnNode = findMcFieldInsnNode(
                    methodNode,
                    GETSTATIC,
                    "net/minecraftforge/client/ClientCommandHandler",
                    "instance",
                    "Lnet/minecraftforge/client/ClientCommandHandler;"
            );
            if (insnNode != null) {
                final InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(
                        INVOKESTATIC,
                        getHookClass("GuiChatHook_MWCommands"),
                        "getLatestAutoComplete",
                        "([Ljava/lang/String;)[Ljava/lang/String;",
                        false
                ));
                list.add(new VarInsnNode(ASTORE, 1));
                methodNode.instructions.insertBefore(insnNode, list);
                return true;
            }
        }
        return false;
    }
}
package me.shvaich.moremwe.asm.transformers.mc.visiblebarriers;

import fr.alexdoru.mwe.api.asm.InjectionCallback;
import me.shvaich.moremwe.asm.transformers.MyTransformer;
import org.objectweb.asm.tree.*;

public class BarrierParticleTransformer_DisableRender extends MyTransformer {

    @Override
    public String[] getTargetClassName() {
        return new String[]{"net.minecraft.client.particle.Barrier"};
    }

    @Override
    public void transform(ClassNode classNode, InjectionCallback status) {
        status.setInjectionPoints(1);
        for (final MethodNode methodNode : classNode.methods) {
            if (checkMcMethod(
                    classNode,
                    methodNode,
                    "renderParticle",
                    "func_180434_a",
                    "(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/entity/Entity;FFFFFF)V"
            )) {
                final InsnList list = new InsnList();

                list.add(new MethodInsnNode(
                        INVOKESTATIC,
                        "me/shvaich/moremwe/features/VisibleBarriers",
                        "isActive",
                        "()Z",
                        false
                ));

                final LabelNode notCanceled = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, notCanceled));

                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new LdcInsnNode(Integer.MIN_VALUE));
                list.add(getMcFieldInsnNode(
                        PUTFIELD,
                        "net/minecraft/client/particle/EntityFX",
                        "particleMaxAge",
                        "field_70547_e",
                        "I"
                ));
                list.add(new InsnNode(RETURN));

                list.add(notCanceled);

                methodNode.instructions.insert(list);
                status.addInjection();
            }
        }
    }
}

package me.shvaich.moremwe;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
public class AddonBootstrap implements IFMLLoadingPlugin {
    public AddonBootstrap() {
        // No need to register addon at the moment
        // MWEApi.registerAddon("me.shvaich.moremwe.MoreMWE$IMWEAddon_MoreMWE");

        // causes crash if addon .jar comes before mwe.jar in the user's filesystem
        // MWEApi.Asm.registerTransformer("me.shvaich.moremwe.asm.transformers.mc.MinecraftTransformer_ShovelGuard");

        registerTransformers(
                "mc.MinecraftTransformer_ShovelGuard",
                "mc.visiblebarriers.BarrierParticleTransformer_DisableRender",
                "mc.visiblebarriers.BlockBarrierTransformer_VisibleBarrier",
                "mc.visiblebarriers.BlockStateMapperTransformer_RemoveBarrier"
        );
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    private static void registerTransformers(String... transformerPaths) {
        Object o = Launch.blackboard.computeIfAbsent("mwe.transformers", (k) -> new ArrayList<String>());
        if (o instanceof List) {
            final List<String> classPaths = new ArrayList<>(transformerPaths.length);
            for (final String path : transformerPaths) {
                classPaths.add("me.shvaich.moremwe.asm.transformers." + path);
            }
            ((List) o).addAll(classPaths);
        }
    }
}

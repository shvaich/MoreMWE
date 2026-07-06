package me.shvaich.moremwe;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.ArrayList;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
public class AddonBootstrap implements IFMLLoadingPlugin {
    public AddonBootstrap() {
        // No need to register addon at the moment
        // MWEApi.registerAddon("me.shvaich.moremwe.MoreMWE$IMWEAddon_MoreMWE");

        // causes crash if addon .jar comes before mwe.jar in the user's filesystem
        // MWEApi.Asm.registerTransformer("me.shvaich.moremwe.asm.transformers.mc.MinecraftTransformer_ShovelGuard");

        registerTransformer("me.shvaich.moremwe.asm.transformers.mc.MinecraftTransformer_ShovelGuard");
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

    private static void registerTransformer(String className) {
        Object o = Launch.blackboard.computeIfAbsent("mwe.transformers", (k) -> new ArrayList());
        if (o instanceof ArrayList) {
            ((ArrayList)o).add(className);
        }
    }
}

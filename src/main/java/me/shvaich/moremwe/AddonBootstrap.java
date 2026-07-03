package me.shvaich.moremwe;

import fr.alexdoru.mwe.api.MWEApi;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
public class AddonBootstrap implements IFMLLoadingPlugin {
    public AddonBootstrap() {
        MWEApi.registerAddon("me.shvaich.moremwe.MoreMWE$IMWEAddon_MoreMWE");

        MWEApi.Asm.registerTransformer("me.shvaich.moremwe.asm.transformers.mc.MinecraftTransformer_ShovelGuard");
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
}

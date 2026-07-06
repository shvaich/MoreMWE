package me.shvaich.moremwe;


import me.shvaich.moremwe.commands.CommandHypixelGroup;
import me.shvaich.moremwe.commands.CommandMoreMWE;
import me.shvaich.moremwe.commands.CommandPlay;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.events.KeybindingListener;
import me.shvaich.moremwe.events.ModAnnouncement;
import me.shvaich.moremwe.events.RightClickListener;
import me.shvaich.moremwe.features.PlayerEnergyDisplay;
import me.shvaich.moremwe.gui.data.HUDManager;
import me.shvaich.moremwe.gui.huds.ShovelGuardHUD;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

// still needed at the moment
@Mod(
    modid = MoreMWE.MOD_ID,
    name = MoreMWE.NAME,
    version = MoreMWE.VERSION,
    dependencies = "required-after:mwenhancements@[4.3,);"
)
public class MoreMWE {

    public static final String MOD_ID = "@MOD_ID@";
    public static final String NAME = "@MOD_NAME@";
    public static final String VERSION = "@MOD_VERSION@";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        MoreMWEConfig.loadConfig(new File(e.getModConfigurationDirectory(), MOD_ID + ".cfg"));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        registerCommands(
                new CommandMoreMWE(),
                new CommandPlay(),
                new CommandHypixelGroup("friend"),
                new CommandHypixelGroup("party")
        );

        registerEvents(
                new ModAnnouncement(),
                new KeybindingListener(),
                new RightClickListener(),
                new PlayerEnergyDisplay(),
                new ShovelGuardHUD()
        );

        HUDManager.register();
    }

    private static void registerCommands(ICommand... commands) {
        for (final ICommand command : commands) {
            ClientCommandHandler.instance.registerCommand(command);
        }
    }

    private static void registerEvents(Object... events) {
        for (final Object event : events) {
            MinecraftForge.EVENT_BUS.register(event);
        }
    }

//    public static final class IMWEAddon_MoreMWE implements IMWEAddon {
//        @Override
//        public String name() { return MoreMWE.NAME; }
//
//        @Override
//        public String targetVersion() { return "4.3"; }
//
//        @Override
//        public void preInit(FMLPreInitializationEvent fmlPreInitializationEvent) {}
//
//        @Override
//        public void init(FMLInitializationEvent fmlInitializationEvent) {}
//
//        @Override
//        public void postInit(FMLPostInitializationEvent fmlPostInitializationEvent) {}
//    }
}

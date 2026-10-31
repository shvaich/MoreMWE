package me.shvaich.moremwe;


import fr.alexdoru.mwe.api.MWEApi;
import me.shvaich.moremwe.commands.*;
import me.shvaich.moremwe.config.MWEHookConfig;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.events.*;
import me.shvaich.moremwe.features.MyModUpdater;
import me.shvaich.moremwe.features.PlayerEnergyDisplay;
import me.shvaich.moremwe.gui.data.HUDManager;
import me.shvaich.moremwe.gui.huds.ShovelGuardIndicator;
import me.shvaich.moremwe.gui.huds.SquadPositionInfo;
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
    dependencies = "required-after:mwenhancements@[" + MoreMWE.TARGET_MWE_VERSION + ",);"
)
public class MoreMWE {

    public static final String MOD_ID = "@MOD_ID@";
    public static final String NAME = "@MOD_NAME@";
    public static final String VERSION = "@MOD_VERSION@";
	public static final String TARGET_MWE_VERSION = "@TARGET_MWE_VERSION@";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        MoreMWEConfig.loadConfig(new File(e.getModConfigurationDirectory(), MOD_ID + ".cfg"));
        MWEApi.Config.registerConfig(MWEHookConfig.class);

        if (MoreMWEConfig.checkForUpdate && !Boolean.getBoolean("mwe.disableUpdater")) {
            new MyModUpdater(e.getSourceFile()).start();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        registerCommands(
                new CommandMoreMWE(),
                new CommandPlay(),
                new CommandVisibleBarriers(),
                new CommandHypixelGroup("friend"),
                new CommandHypixelGroup("party")
        );

        registerEvents(
                new ModAnnouncement(),
                KeybindingListener.instance(),
                PlayerEnergyDisplay.instance(),
                HUDManager.instance(),
                new ShovelGuardIndicator(),
                ChatListener.instance(),
        );

        MWEApi.Hud.registerSquadHUDExtraRenderer(new SquadPositionInfo());
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
}

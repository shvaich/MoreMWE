package me.shvaich.moremwe.config;

import fr.alexdoru.configlib.api.ConfigProperty;
import me.shvaich.moremwe.features.MWCondition;
import net.minecraft.util.EnumChatFormatting;

public class MWEHookConfig {
    @ConfigProperty(
            name = "Show Compass Information In Squad HUD",
            category = "Squad",
            subCategory = "MoreMWE - Compass Info"
    )
    public static boolean showCompassInfoInSquadHUD = false;

    @ConfigProperty(
            name = "Show in Spectator Mode",
            category = "Squad",
            subCategory = "MoreMWE - Compass Info",
            comment = "Display while in spectator mode",
            dependsOn = "Show Compass Information In Squad HUD"
    )
    public static boolean showSquadHUDCompassInfoWhenSpectator = false;

    @ConfigProperty(
            name = "Mega Walls Only",
            category = "Squad",
            subCategory = "MoreMWE - Compass Info",
            comment = "If enabled, will only be displayed during Mega Walls matches",
            dependsOn = "Show Compass Information In Squad HUD"
    )
    public static boolean isSquadHUDCompassInfoForMegaWallsOnly = true;

    @ConfigProperty(
            name = "Active During",
            category = "Squad",
            subCategory = "MoreMWE - Compass Info",
            comment = "Controls when to display during a Mega Walls match",
            dependsOn = "Show Compass Information In Squad HUD"
    )
    public static MWCondition squadHUDCompassInfoMegaWallsRenderCondition = MWCondition.DURING_DEATHMATCH;

    @ConfigProperty(
            name = "Show Distance Unit",
            category = "Squad",
            subCategory = "MoreMWE - Compass Info",
            comment = "Display 'm' after the distance value",
            dependsOn = "Show Compass Information In Squad HUD"
    )
    public static boolean showDistanceUnitInSquadHUDCompassInfo = true;

    @ConfigProperty(
            name = "Color Y-Distance",
            category = "Squad",
            subCategory = "MoreMWE - Compass Info",
            comment = "Use a different color for the y-distance value when a player is above/below you",
            dependsOn = "Show Compass Information In Squad HUD"
    )
    public static boolean colorYDistanceInSquadHUDCompassInfo = false;

    @ConfigProperty(
            name = "Player Above Color",
            category = "Squad",
            subCategory = "MoreMWE - Compass Info",
            comment = "Color of the y-distance value when a player is above you",
            dependsOn = "Color Y-Distance"
    )
    public static EnumChatFormatting squadCompassInfoPlayerAboveColor = EnumChatFormatting.DARK_GREEN;

    @ConfigProperty(
            name = "Player Below Color",
            category = "Squad",
            subCategory = "MoreMWE - Compass Info",
            comment = "Color of the y-distance value when a player is below you",
            dependsOn = "Color Y-Distance"
    )
    public static EnumChatFormatting squadCompassInfoPlayerBelowColor = EnumChatFormatting.DARK_RED;
}

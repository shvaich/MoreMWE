package me.shvaich.moremwe.config;

import me.shvaich.moremwe.config.data.*;
import me.shvaich.moremwe.features.GuardableBlock;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.moremwe.config.gui.elements.custom.ConfigGuardableBlockButton;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.gui.data.MyRendererPosition;
import me.shvaich.moremwe.gui.huds.CompassHUD;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MoreMWEConfig extends AbstractConfig {

    public static final String BLOCK_GUARD = "BlockGuard";

    @ConfigCategory(
            comment = "Designed for Moleman players in Hypixel Mega Walls.\n" +
                    "Every feature will only work in Mega Walls"
    )
    public static final String SHOVEL_GUARD = "ShovelGuard";

    @ConfigProperty(
            type = PropertyType.HUD_POSITION,
            name = "Compass HUD",
            category = "Compass",
            comment = "Displays directional information about tracked players"
    )
    public static final MyRendererPosition compassHUDPosition = new MyRendererPosition(false, 0, 0);

    @ConfigProperty(
            type = PropertyType.NUMBER,
            name = "Max Tracked Players",
            category = "Compass",
            subcategory = "Tracking",
            min = 1,
            max = CompassHUD.REAL_MAX_COMPASS_TRACKED,
            comment = "Maximum number of players that will appear on the HUD"
    )
    public static int maxCompassTrackedPlayers = 4;

    @ConfigProperty(
            type = PropertyType.SLIDER,
            name = "Max Tracking Distance",
            category = "Compass",
            subcategory = "Tracking",
            min = 10,
            max = 150,
            comment = "Maximum distance at which players will appear on the HUD"
    )
    public static int maxCompassTrackingDistance = 100;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            name = "HUD Keybind Mode",
            category = "Compass",
            subcategory = "Behaviour",
            comment = "\"Toggle HUD\" - enables or disables the HUD when the \"Compass HUD Control\" keybind is pressed.\n"
                    + "\"Hold to Show\" - requires the \"Compass HUD Control\" keybind to be held in addition to all other display conditions",
            options = { "Toggle HUD", "Hold to Show" }
    )
    public static int compassKeybindMode = 0;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show in Spectator Mode",
            category = "Compass",
            subcategory = "Behaviour",
            comment = "Display the HUD while in spectator mode"
    )
    public static boolean showCompassWhenSpectator = false;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Hide Other Spectators",
            category = "Compass",
            subcategory = "Behaviour",
            comment = "When spectating, try to hide other spectators from the HUD.\n§eSpectator detection may not work correctly on all servers or game modes"
    )
    public static boolean ignoreOtherSpectators = false;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show Distance Unit",
            category = "Compass",
            subcategory = "Display",
            comment = "Display 'm' after the distance value"
    )
    public static boolean showDistanceUnitInCompassHUD = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show HP",
            category = "Compass",
            subcategory = "Display",
            comment = "Display the player's HP"
    )
    public static boolean showHPInCompassHUD = false;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            name = "HP Display Position",
            category = "Compass",
            subcategory = "Display",
            comment = "Position of the HP value",
            options = {
                    "Last",
                    "Before Distance",
                    "After Distance",
            }
    )
    public static int hpCompassDisplayPosition = 0;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show Heart Icon",
            category = "Compass",
            subcategory = "Display",
            comment = "Display a heart after the HP value"
    )
    public static boolean showHPIconInCompassHUD = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Mega Walls Only",
            category = "Compass",
            subcategory = "Mega Walls",
            comment = "If enabled, the HUD will only be displayed in Mega Walls matches"
    )
    public static boolean isCompassForMegaWallsOnly = true;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            name = "Active During",
            category = "Compass",
            subcategory = "Mega Walls",
            options = {
                    "During Match",
                    "After Walls Fall",
                    "After First Wither Dies",
                    "During Deathmatch"
            },
            comment = "Controls when the HUD is displayed during a Mega Walls match"
    )
    public static int compassMegaWallsRenderCondition = 1;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Prefer Different Teams",
            category = "Compass",
            subcategory = "Mega Walls",
            comment = "Prioritizes selecting players from different teams over closest distance"
    )
    public static boolean prioritizeUniqueTeams = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Custom Teammate Color",
            category = "Compass",
            subcategory = "Mega Walls",
            comment = "Uses a custom color instead of the default teammate color"
    )
    public static boolean renderCustomTeammateColor = false;

    @ConfigProperty(
            type = PropertyType.COLOR,
            name = "Teammate Color",
            category = "Compass",
            subcategory = "Mega Walls",
            comment = "Color used for displaying teammates in the HUD",
            allowsTransparency = false
    )
    public static int customTeammateColor = 0xFFFF55FF;


    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Enable Block Guard",
            category = BLOCK_GUARD
    )
    public static boolean areBlocksGuarded = false;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Sneak Guard",
            category = BLOCK_GUARD,
            subcategory = "Behaviour",
            comment = "Prevents interaction with guarded blocks while sneaking with an empty hand"
    )
    public static boolean guardSneakAndEmptyHandClicks = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Allow Pickaxe Override",
            category = BLOCK_GUARD,
            subcategory = "Behaviour",
            comment = "Allows interactions with guarded blocks while holding a pickaxe"
    )
    public static boolean canPickaxeOverrideBlockGuard = false;

    private static class GuardedBlocksInfo extends CustomPropertyInfo {
        @Override
        public ConfigGuiElement[] getConfigGuiButtons(MyConfigGuiScreen screen, ConfigFieldContainer fieldData) {
            try {
                final GuardableBlock[] values = GuardableBlock.values();
                final int len = values.length;
                final ConfigGuardableBlockButton[] buttons = new ConfigGuardableBlockButton[len];
                for (int i = 0; i < len; i++)
                    buttons[i] = new ConfigGuardableBlockButton(screen, fieldData, values[i]);
                return buttons;
            }
            catch (Exception e) { throw new RuntimeException("Failed to create GuardableBlock buttons", e); }
        }
    }
    @ConfigProperty(
            type = PropertyType.CUSTOM,
            name = "Guarded Blocks",
            category = BLOCK_GUARD,
            subcategory = "Guardable Blocks",
            customPropertyClass = GuardedBlocksInfo.class
    )
    public static final Set<String> guardedBlocks = new HashSet<>(Arrays.stream(GuardableBlock.values()).map(o -> o.key).collect(Collectors.toSet()));
    //public static final Set<GuardableBlock> guardedBlocks = new HashSet<>(Arrays.asList(GuardableBlock.values()));

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Mega Walls Only",
            category = BLOCK_GUARD,
            subcategory = "Mega Walls",
            comment = "If enabled, BlockGuard will only be active during Mega Walls matches"
    )
    public static boolean isBlockGuardForMegaWallsOnly = true;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            name = "Active During",
            category = BLOCK_GUARD,
            subcategory = "Mega Walls",
            options = {
                    "During Match",
                    "After Walls Fall",
                    "During Deathmatch"
            },
            comment = "Controls when BlockGuard is active during a Mega Walls match"
    )
    public static int blockGuardMegaWallsCondition = 1;


    @ConfigProperty(
            type = PropertyType.SWITCH,
            category = SHOVEL_GUARD,
            name = "Enable Shovel Guard"
    )
    public static boolean isShovelGuarded = false;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            name = "Entity Right-Click Guard",
            category = SHOVEL_GUARD,
            subcategory = "Behaviour",
            comment = "\"All Entities\" - Guard all entity right-clicks\n"
                    + "\"Teammates Only\" - Guard only teammate right-clicks\n"
                    + "\"Disabled\" - Don't guard entity right-clicks",
            options = {
                    "All Entities",
                    "Teammates Only",
                    "Disabled"
            }
    )
    public static int shovelEntityInteractionCondition = 0;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Smart Guard",
            category = SHOVEL_GUARD,
            subcategory = "Behaviour",
            comment = "Don't guard right-clicks on unguarded blocks\n"
                    + "(disabling this can cause unintended behaviour)"
    )
    public static boolean canOverrideShovelGuard = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show " + SHOVEL_GUARD + " HUD",
            category = SHOVEL_GUARD,
            subcategory = "HUD"
    )
    public static boolean renderShovelGuardHUD = false;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            category = SHOVEL_GUARD,
            subcategory = "HUD",
            name = SHOVEL_GUARD + " HUD Style",
            options = { "Background", "Line-Over" }
    )
    public static int shovelGuardHudStyle = 0;

    @ConfigProperty(
            type = PropertyType.COLOR,
            category = SHOVEL_GUARD,
            subcategory = "HUD",
            name = SHOVEL_GUARD + " HUD Color"
    )
    public static int shovelGuardHudColor = 0x55FF0000;


    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Automatically Announce Full Energy",
            category = "Mega Walls",
            subcategory = "Energy Status",
            comment = "Automatically send a chat message when your energy reaches 100%"
    )
    public static boolean sendEnergyMessageWhenFullyCharged = false;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            name = "Announce Full Energy During",
            category = "Mega Walls",
            subcategory = "Energy Status",
            comment = "Controls when automatic full energy announcements are active",
            options = {
                    "After First Wither Dies",
                    "During Deathmatch"
            }
    )
    public static int energyMessageMegaWallsCondition = 1;


    private static MoreMWEConfig instance;

    public static void loadConfig(File file) {
        if (instance != null)
            throw new IllegalStateException("Config already created");

        instance = new MoreMWEConfig(file);
    }

    public static MoreMWEConfig instance() { return instance; }

    private MoreMWEConfig(File file) {
        super(file);
        Arrays.asList(
                "maxCompassTrackedPlayers",
                "maxCompassTrackingDistance",
                "compassKeybindMode",
                "showCompassWhenSpectator",
                "showDistanceUnitInCompassHUD",
                "showHPInCompassHUD",
                "isCompassForMegaWallsOnly",
                "compassMegaWallsRenderCondition",
                "prioritizeUniqueTeams",
                "renderCustomTeammateColor"
        ).forEach(dependantName -> addDependency(dependantName, "compassHUDPosition"));

        addDependency("ignoreOtherSpectators", "showCompassWhenSpectator");
        addDependency("hpCompassDisplayPosition", "showHPInCompassHUD");
        addDependency("showHPIconInCompassHUD", "showHPInCompassHUD");
        addDependency("customTeammateColor", "renderCustomTeammateColor");


        Arrays.asList(
                "guardSneakAndEmptyHandClicks",
                "canPickaxeOverrideBlockGuard",
                "guardedBlocks",
                "isBlockGuardForMegaWallsOnly",
                "blockGuardMegaWallsCondition"
        ).forEach(property -> addDependency(property, "areBlocksGuarded"));


        Arrays.asList(
                "shovelEntityInteractionCondition",
                "canOverrideShovelGuard",
                "renderShovelGuardHUD"
        ).forEach(property -> addDependency(property, "isShovelGuarded"));

        addDependency("shovelGuardHudStyle", "renderShovelGuardHUD");
        addDependency("shovelGuardHudColor", "renderShovelGuardHUD");


        addDependency("energyMessageMegaWallsCondition", "sendEnergyMessageWhenFullyCharged");
    }
}
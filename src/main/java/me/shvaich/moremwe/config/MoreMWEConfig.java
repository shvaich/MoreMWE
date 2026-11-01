package me.shvaich.moremwe.config;

import fr.alexdoru.configlib.api.ColorPalette;
import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.data.*;
import me.shvaich.moremwe.features.GuardableBlock;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.moremwe.config.gui.elements.custom.ConfigGuardableBlockButton;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.features.MWGameState;
import me.shvaich.moremwe.gui.data.MyRendererPosition;
import me.shvaich.moremwe.gui.huds.CompassHUD;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.MoreMWEException;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class MoreMWEConfig extends AbstractConfig {

    public static final String BLOCK_GUARD = "BlockGuard";

    @ConfigCategory(
            comment = "Designed for Moleman players in Hypixel Mega Walls"
                    + ".\nEvery feature will only work in Mega Walls"
    )
    public static final String SHOVEL_GUARD = "ShovelGuard";

    private static final int DEFAULT_HUD_BACKGROUND_COLOR = new Color(8, 8, 8, 0x60).getRGB();

    @ConfigProperty(
            type = PropertyType.HUD_POSITION,
            name = "Compass HUD",
            category = "Compass",
            comment = "Displays directional information about tracked players"
    )
    public static final MyRendererPosition compassHUDPosition = new MyRendererPosition(false, 1, 1);

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
            min = 0,
            max = 150,
            comment = "Maximum distance at which players will appear on the HUD"
                    + "\nSetting this to 0 will show players at any distance"
    )
    public static int maxCompassTrackingDistance = 100;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            name = "HUD Keybind Mode",
            category = "Compass",
            subcategory = "Behaviour",
            comment = "\"Toggle HUD\" - enables or disables the HUD when the \"Compass HUD Control\" keybind is pressed"
                    + "\n\"Hold to Show\" - requires the \"Compass HUD Control\" keybind to be held in addition to all other display conditions",
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
            comment = "When spectating, try to hide other spectators from the HUD"
                    + ".\n§eSpectator detection may not work correctly on all servers or game modes"
    )
    public static boolean ignoreOtherSpectators = false;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Mega Walls Only",
            category = "Compass",
            subcategory = "Mega Walls",
            comment = "If enabled, the HUD will only be displayed in Mega Walls matches"
    )
    public static boolean isCompassForMegaWallsOnly = true;

    @ConfigProperty(
            type = PropertyType.ENUM_SELECTOR,
            name = "Active During",
            category = "Compass",
            subcategory = "Mega Walls",
            //options = { "During Match", "After Walls Fall", "After First Wither Dies", "During Deathmatch" },
            comment = "Controls when the HUD is displayed during a Mega Walls match"
    )
    public static MWGameState compassMegaWallsRenderCondition = MWGameState.AFTER_WALLS_FALL; // 1;

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
            comment = "Uses a custom color for teammates instead of the team color"
    )
    public static boolean renderCustomTeammateColor = false;

    @ConfigProperty(
            type = PropertyType.COLOR,
            name = "Teammate Color",
            category = "Compass",
            subcategory = "Mega Walls",
            comment = "Color used when displaying teammates in the HUD",
            allowsTransparency = false
    )
    public static int customTeammateColor = ColorPalette.VanillaColors.LIGHT_PURPLE;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show Distance Unit",
            category = "Compass",
            subcategory = "Display",
            comment = "Displays 'm' after the distance value"
    )
    public static boolean showDistanceUnitInCompassHUD = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Color Y-Distance",
            category = "Compass",
            subcategory = "Display",
            comment = "§2GREEN§r if the player is above you, §4RED§r if they are below you"
    )
    public static boolean colorYDistanceInCompassHUD = false;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show HP",
            category = "Compass",
            subcategory = "Display",
            comment = "Display the player's HP in the HUD"
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
            name = "Show Background",
            category = "Compass",
            subcategory = "Display"
    )
    public static boolean showCompassHUDBackground = true;

    @ConfigProperty(
            type = PropertyType.COLOR,
            name = "Background Color",
            category = "Compass",
            subcategory = "Display"
    )
    public static int compassHUDBackgroundColor = DEFAULT_HUD_BACKGROUND_COLOR;

    @ConfigProperty(
            type = PropertyType.NUMBER,
            name = "Directional Arrow Gap",
            category = "Compass",
            subcategory = "Display",
            comment = "The width of the gap before and after the directional arrow display",
            min = CompassHUD.COLUMN_GAP,
            max = 20
    )
    public static int compassHUDArrowMargin = 10;


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
                for (int i = 0; i < len; i++) {
                    buttons[i] = new ConfigGuardableBlockButton(screen, fieldData, values[i]);
                }
                return buttons;
            }
            catch (Exception e) {
                throw new MoreMWEException(e).sendDebug("Failed to create GuardableBlock buttons");
            }
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
            type = PropertyType.ENUM_SELECTOR,
            name = "Active During",
            category = BLOCK_GUARD,
            subcategory = "Mega Walls",
            //options = { "During Match", "After Walls Fall", "During Deathmatch" },
            comment = "Controls when BlockGuard is active during a Mega Walls match"
    )
    public static MWGameState blockGuardMegaWallsCondition = MWGameState.AFTER_WALLS_FALL; // 1;


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
            comment = "\"All Entities\" - Guard all entity right-clicks"
                    + "\n\"Teammates Only\" - Guard only teammate right-clicks"
                    + "\n\"Disabled\" - Don't guard entity right-clicks",
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
            comment = "Don't guard right-clicks on unguarded blocks"
                    + "\n(disabling this can cause unintended behaviour)"
    )
    public static boolean canOverrideShovelGuard = true;

    @ConfigProperty(
            type = PropertyType.HUD_POSITION,
            name = SHOVEL_GUARD + " HUD",
            category = SHOVEL_GUARD,
            subcategory = "Display"
    )
    public static final MyRendererPosition shovelGuardHUDPosition = new MyRendererPosition(false, 0, 0);

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show HUD Background",
            category = SHOVEL_GUARD,
            subcategory = "Display"
    )
    public static boolean showShovelGuardHUDBackground = true;

    @ConfigProperty(
            type = PropertyType.COLOR,
            name = "HUD Background Color",
            category = SHOVEL_GUARD,
            subcategory = "Display"
    )
    public static int shovelGuardHUDBackgroundColor = DEFAULT_HUD_BACKGROUND_COLOR;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show " + SHOVEL_GUARD + " Indicator",
            category = SHOVEL_GUARD,
            subcategory = "Display",
            comment = "Displays an indicator over every shovel in your hotbar"
    )
    public static boolean showShovelGuardIndicator = false;

    @ConfigProperty(
            type = PropertyType.SELECTOR,
            category = SHOVEL_GUARD,
            subcategory = "Display",
            name = SHOVEL_GUARD + " Indicator Style",
            options = { "Background", "Line-Over" }
    )
    public static int shovelGuardIndicatorStyle = 0;

    @ConfigProperty(
            type = PropertyType.COLOR,
            category = SHOVEL_GUARD,
            subcategory = "Display",
            name = SHOVEL_GUARD + " Indicator Color"
    )
    public static int shovelGuardIndicatorColor = new Color(0xFF, 0, 0, 0x55).getRGB();


    @ConfigProperty(
            type = PropertyType.ENUM_SELECTOR,
            name = "Energy Status Chat Channel",
            category = "Mega Walls",
            subcategory = "Energy Status",
            comment = "The chat channel status messages are sent to"
                    + ".\n\"Default\" uses your currently selected chat channel"
    )
    public static ChatUtil.ChatChannel energyMessageChatChannel = ChatUtil.ChatChannel.DEFAULT;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Automatically Announce Full Energy",
            category = "Mega Walls",
            subcategory = "Energy Status",
            comment = "Automatically send a chat message when your energy reaches 100%"
    )
    public static boolean sendEnergyMessageWhenFullyCharged = false;

    @ConfigProperty(
            type = PropertyType.ENUM_SELECTOR,
            name = "Announce Full Energy During",
            category = "Mega Walls",
            subcategory = "Energy Status",
            //options = { "After First Wither Dies", "During Deathmatch" },
            comment = "Controls when automatic full energy announcements are active"
    )
    public static MWGameState energyMessageMegaWallsCondition = MWGameState.DURING_DEATHMATCH; // 1;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Enable MW Commands",
            category = "Mega Walls",
            subcategory = "Commands"
    )
    public static boolean enableMWCommands = true;

    @ConfigProperty(
            type = PropertyType.MINECRAFT_COLOR,
            name = "MW Commands Tab Color",
            category = "Mega Walls",
            subcategory = "Commands",
            comment = "The color of mw commands in the chat when you press [TAB]"
    )
    public static EnumChatFormatting mwCommandsTabColor = EnumChatFormatting.GRAY;

    @ConfigProperty(
            type = PropertyType.HUD_POSITION,
            name = "Spider Leap HUD",
            category = "Mega Walls",
            subcategory = "Spider Leap HUD"
    )
    public static final MyRendererPosition spiderLeapHUDPosition = new MyRendererPosition(false, 0, 0);

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Hide During Prep Phase",
            category = "Mega Walls",
            subcategory = "Spider Leap HUD",
            comment = "Hides the HUD until the walls fall"
    )
    public static boolean hideSpiderLeapHUDDuringPrepPhase = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Only Show when Relevant",
            category = "Mega Walls",
            subcategory = "Spider Leap HUD",
            comment = "Only show the HUD when holding either a Sword, Bow or Shovel"
    )
    public static boolean onlyShowSpiderHUDWhenHoldingItems = false;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Show Spider Leap HUD Background",
            category = "Mega Walls",
            subcategory = "Spider Leap HUD"
    )
    public static boolean showSpiderLeapHUDBackground = true;

    @ConfigProperty(
            type = PropertyType.COLOR,
            name = "Spider Leap HUD Background Color",
            category = "Mega Walls",
            subcategory = "Spider Leap HUD"
    )
    public static int spiderLeapHUDBackgroundColor = DEFAULT_HUD_BACKGROUND_COLOR;


    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Play Command Warp Protection",
            category = "Hypixel",
            comment = "Adds confirmation when running the \"/play\" command if you have players in your squad"
    )
    public static boolean playWarpProtection = false;


    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Check for Update",
            category = GENERAL_CATEGORY,
            subcategory = "Update",
            comment = "Check if an update is available"
    )
    public static boolean checkForUpdate = true;

    @ConfigProperty(
            type = PropertyType.SWITCH,
            name = "Automatic Update",
            category = GENERAL_CATEGORY,
            subcategory = "Update",
            comment = "Updates the mod automatically"
    )
    public static boolean automaticUpdate = false;


    private static MoreMWEConfig instance;

    public static MoreMWEConfig instance() { return instance; }

    public static void loadConfig(File file) {
        if (instance != null)
            throw new MoreMWEException();

        instance = new MoreMWEConfig(file);
    }

    @Override
    protected void onConfigUpdate(Configuration config, String savedVersion, String version) {
        if (isVersionLowerThan(savedVersion, "1.1")) {
            // Removing an old Property declared in AbstractConfig here
            // because it only exists in my MoreMWE mod version 1.0
            removeConfigProperty(GENERAL_CATEGORY, "Behaviour when Dependency is Disabled");

            migratePropertyName(SHOVEL_GUARD, "Show " + SHOVEL_GUARD + " HUD", "Show " + SHOVEL_GUARD + " Indicator");
            migratePropertyName(SHOVEL_GUARD, SHOVEL_GUARD + " HUD Style", SHOVEL_GUARD + " Indicator Style");
            migratePropertyName(SHOVEL_GUARD, SHOVEL_GUARD + " HUD Color", SHOVEL_GUARD + " Indicator Color");
        }
    }

    private MoreMWEConfig(File file) {
        super(file, MoreMWE.VERSION);
        Arrays.asList(
                "maxCompassTrackedPlayers",
                "maxCompassTrackingDistance",
                "compassKeybindMode",
                "showCompassWhenSpectator",
                "isCompassForMegaWallsOnly",
                "compassMegaWallsRenderCondition",
                "prioritizeUniqueTeams",
                "renderCustomTeammateColor",
                "showDistanceUnitInCompassHUD",
                "colorYDistanceInCompassHUD",
                "showHPInCompassHUD",
                "showCompassHUDBackground",
                "compassHUDArrowMargin"
        ).forEach(dependantName -> addDependency(dependantName, "compassHUDPosition"));

        addDependency("ignoreOtherSpectators", "showCompassWhenSpectator");
        addDependency("hpCompassDisplayPosition", "showHPInCompassHUD");
        addDependency("showHPIconInCompassHUD", "showHPInCompassHUD");
        addDependency("compassHUDBackgroundColor", "showCompassHUDBackground");
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
                "showShovelGuardIndicator"
        ).forEach(property -> addDependency(property, "isShovelGuarded"));

        addDependency("showShovelGuardHUDBackground", "shovelGuardHUDPosition");
        addDependency("shovelGuardHUDBackgroundColor", "showShovelGuardHUDBackground");

        addDependency("shovelGuardIndicatorStyle", "showShovelGuardIndicator");
        addDependency("shovelGuardIndicatorColor", "showShovelGuardIndicator");


        addDependency("energyMessageMegaWallsCondition", "sendEnergyMessageWhenFullyCharged");

        addDependency("mwCommandsTabColor", "enableMWCommands");

        Arrays.asList(
                "hideSpiderLeapHUDDuringPrepPhase",
                "onlyShowSpiderHUDWhenHoldingItems",
                "showSpiderLeapHUDBackground"
        ).forEach(property -> addDependency(property, "spiderLeapHUDPosition"));

        addDependency("spiderLeapHUDBackgroundColor", "showSpiderLeapHUDBackground");


        addDependency("automaticUpdate", "checkForUpdate");
    }
}
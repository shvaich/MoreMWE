package me.shvaich.moremwe.gui.huds;


import fr.alexdoru.configlib.api.ColorPalette.VanillaColors;
import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.enums.MWTeam;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.events.KeybindingListener;
import me.shvaich.moremwe.gui.data.AbstractRenderer;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.ColorUtil;
import me.shvaich.moremwe.utils.GuiUtil;
import me.shvaich.moremwe.utils.PlayerUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

public class CompassHUD extends AbstractRenderer {

    private static final Comparator<PlayerTrackingData> HUD_ORDER = Comparator.comparingDouble(p -> p.distance);
    public static final int REAL_MAX_COMPASS_TRACKED = 8;
    public static final int COLUMN_GAP = 6;
    private static final int ROW_GAP = 2;
    private static final int SKIN_WIDTH = 8 + 2;
    private static final int Y_DISTANCE_ARROW_PREFIX_WIDTH = 8; // mc.fontRendererObj.getStringWidth("▲ ");

    private final PlayerTrackingData[] dummyPlayers;
    private final PlayerTrackingData[] compassTrackedPlayers = new PlayerTrackingData[REAL_MAX_COMPASS_TRACKED];

    private final EntityPlayer[] scratchPlayers = new EntityPlayer[REAL_MAX_COMPASS_TRACKED];
    private final double[] scratchPlayerDistancesSq = new double[REAL_MAX_COMPASS_TRACKED];
    private final int[] scratchPlayerToTeamIndex = new int[REAL_MAX_COMPASS_TRACKED];

    private final int[] scratchTeamPlayerCounts;
    private final int[] scratchTeamFurthestPlayerIndexes;
    private final double[] scratchTeamFurthestDistancesSq;

    private int trackedPlayersCount;

    private int longestDistanceStringWidth;
    private int longestHPStringWidth;
    private int longestEnergyStringWidth;
    private boolean hasNonZeroYDistanceValue;

    public CompassHUD() {
        super(MoreMWEConfig.compassHUDPosition);

        final int amountOfTeams = MWTeam.values().length + 1; // a player without an identifiable team counts as a different team
        this.scratchTeamPlayerCounts = new int[amountOfTeams];
        this.scratchTeamFurthestPlayerIndexes = new int[amountOfTeams];
        this.scratchTeamFurthestDistancesSq = new double[amountOfTeams];

        for (int i = 0; i < compassTrackedPlayers.length; i++) {
            compassTrackedPlayers[i] = new PlayerTrackingData();
        }

        this.dummyPlayers = new PlayerTrackingData[]{
                new PlayerTrackingData(new ResourceLocation("textures/entity/steve.png"), VanillaColors.GREEN, 0, 0, 40, 65),
                new PlayerTrackingData(new ResourceLocation("textures/entity/alex.png"), VanillaColors.BLUE, 1, 1, 20, 31),
                new PlayerTrackingData(new ResourceLocation("textures/entity/zombie/zombie.png"), VanillaColors.RED, 5, -1, 10, 17),
                new PlayerTrackingData(new ResourceLocation("textures/entity/zombie_pigman.png"), VanillaColors.YELLOW, 1, 5, 5, 84)
        };
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isEnabled(Minecraft mc, long currentTimeMillis) {
        if (!rendererPosition.isEnabled()
                || (MoreMWEConfig.compassKeybindMode == 1 && KeybindingListener.instance().isCompassHUDKeyAssignedAndNotHeldDown())
                || (!MoreMWEConfig.showCompassWhenSpectator && PlayerUtil.isTheMcPlayerSpectator(mc.thePlayer))
        ) return false;

        return MWEApi.Scoreboard.getScoreboardParser().isInMwGame() ?
                MoreMWEConfig.compassMegaWallsRenderCondition.isActive(MWEApi.Scoreboard.getScoreboardParser())
                : !MoreMWEConfig.isCompassForMegaWallsOnly;
    }

    @Override
    public void render(Minecraft mc, int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        if (trackedPlayersCount == 0) {
            final int x = rendererPosition.getDrawX();
            final int y = rendererPosition.getDrawY();
            drawBackground(x, y, thisWidth, thisHeight);
            mc.fontRendererObj.drawStringWithShadow("None!", x, y, VanillaColors.RED);
            return;
        }
        renderCompassHUD(mc, compassTrackedPlayers, trackedPlayersCount, thisWidth, thisHeight);
    }

    @Override
    public int getWidth(Minecraft mc) {
        if (trackedPlayersCount == 0) return mc.fontRendererObj.getStringWidth("None!");
        return getMaxColumnWidth(mc, compassTrackedPlayers, trackedPlayersCount);
    }

    @Override
    public int getHeight(Minecraft mc) {
        if (trackedPlayersCount == 0) return mc.fontRendererObj.FONT_HEIGHT - 1;
        return getRowHeight() * trackedPlayersCount - ROW_GAP;
    }

    @Override
    public void renderDummy(Minecraft mc, int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        final int renderedDummiesCount = getDummiesToRenderCount();
        final int maxCompassTrackedPlayersCount = getMaxCompassTrackedPlayersCount();
        renderCompassHUD(mc, dummyPlayers, renderedDummiesCount, thisWidth, thisHeight);
        if (renderedDummiesCount < maxCompassTrackedPlayersCount) {
            final int missing = maxCompassTrackedPlayersCount - renderedDummiesCount;
            final String text = "+" + missing + " More";
            final int x = rendererPosition.getDrawX() + (thisWidth - mc.fontRendererObj.getStringWidth(text)) / 2;
            final int y = rendererPosition.getDrawY() + thisHeight - mc.fontRendererObj.FONT_HEIGHT + 1;
            mc.fontRendererObj.drawStringWithShadow(text, x, y, ModResources.WHITE);
        }
    }

    @Override
    public int getDummyWidth(Minecraft mc) {
        return getMaxColumnWidth(mc, dummyPlayers, getDummiesToRenderCount());
    }

    @Override
    public int getDummyHeight(Minecraft mc) {
        final int renderedDummiesCount = getDummiesToRenderCount();
        int height = getRowHeight() * renderedDummiesCount - ROW_GAP;

        if (renderedDummiesCount < getMaxCompassTrackedPlayersCount())
            height += ROW_GAP + mc.fontRendererObj.FONT_HEIGHT - 1;

        return height;
    }

    private int getDummiesToRenderCount() {
        return Math.min(getMaxCompassTrackedPlayersCount(), dummyPlayers.length);
    }

    private void renderCompassHUD(Minecraft mc, PlayerTrackingData[] pArr, int length, int thisWidth, int thisHeight) {
        final int x = rendererPosition.getDrawX();
        int y = rendererPosition.getDrawY();
        int distanceX = x + SKIN_WIDTH;
        int arrowX = distanceX + longestDistanceStringWidth + MoreMWEConfig.compassHUDArrowMargin;
        int healthX = 0;
        float thePlayerMaxHP = 0;
        final boolean renderEnergy = MoreMWEConfig.showEnergyInCompassHUD && MWEApi.Scoreboard.getScoreboardParser().isInMwGame();
        final boolean showHP = MoreMWEConfig.showHPInCompassHUD;
        if (showHP) {
            thePlayerMaxHP = mc.thePlayer.getMaxHealth();
            switch (MoreMWEConfig.hpCompassDisplayPosition) {
                case 0: {
                    healthX = x + thisWidth - longestHPStringWidth;
                    if (renderEnergy) {
                        healthX -= longestEnergyStringWidth + COLUMN_GAP;
                    }
                    break;
                }

                case 1: {
                    final int hpWidth = longestHPStringWidth + COLUMN_GAP;
                    healthX = distanceX;
                    distanceX += hpWidth;
                    arrowX += hpWidth;
                    break;
                }

                case 2: {
                    healthX = distanceX + longestDistanceStringWidth + COLUMN_GAP;
                    arrowX = healthX + longestHPStringWidth + MoreMWEConfig.compassHUDArrowMargin;
                    break;
                }
            }
        }
        final int rowHeight = getRowHeight();
        final int realRowHeight = rowHeight - ROW_GAP;
        drawBackground(x, y, thisWidth, thisHeight);
        for (int i = 0; i < length; i++) {
            final PlayerTrackingData player = pArr[i];
            GuiUtil.drawSkinHead(mc, player.skin, x, y);
            final int teamColor = player.teamColor;
            final String distanceStr = ((int) player.distance) + (MoreMWEConfig.showDistanceUnitInCompassHUD ? "m" : "");
            mc.fontRendererObj.drawStringWithShadow(distanceStr, distanceX, y, teamColor);

            final int arrowY = y + (realRowHeight - 6) / 2;
            GuiUtil.drawDirectionArrow(arrowX, arrowY, player.relativeYaw, teamColor);

            final int yDistance = player.distanceY;
            int yDistanceX = arrowX + 5 + MoreMWEConfig.compassHUDArrowMargin;
            if (yDistance == 0 && hasNonZeroYDistanceValue) {
                yDistanceX += MoreMWEConfig.showYDistanceArrowsInCompassHUD ? Y_DISTANCE_ARROW_PREFIX_WIDTH : GuiUtil.DIGIT_AND_SIGN_WIDTH;
            }
            final int yDistanceColor = (yDistance == 0 || !MoreMWEConfig.colorYDistanceInCompassHUD) ? MoreMWEConfig.compassHUDDefaultYDistanceColor : (yDistance < 0 ? MoreMWEConfig.compassHUDPlayerBelowColor : MoreMWEConfig.compassHUDPlayerAboveColor);
            mc.fontRendererObj.drawStringWithShadow(getYDistanceText(yDistance, MoreMWEConfig.showYDistanceArrowsInCompassHUD), yDistanceX, y, yDistanceColor);
            if (showHP) {
                String text = String.valueOf(getDisplayHP(player.health));
                if (MoreMWEConfig.showHPIconInCompassHUD) {
                    text += EnumChatFormatting.RED + " ❤";
                }
                mc.fontRendererObj.drawStringWithShadow(text, healthX, y, ColorUtil.getValueColor(thePlayerMaxHP, player.health));
            }
            if (renderEnergy) {
                mc.fontRendererObj.drawStringWithShadow(String.valueOf(player.experienceLevel), x + thisWidth - longestEnergyStringWidth, y, ColorUtil.getValueColor(100, player.experienceLevel));
            }
            y += rowHeight;
        }
    }

    private void drawBackground(int left, int top, int thisWidth, int thisHeight) {
        if (MoreMWEConfig.showCompassHUDBackground) {
            drawBackground(left, top, thisWidth, thisHeight, MoreMWEConfig.compassHUDBackgroundColor);
        }
    }

    private int getMaxColumnWidth(Minecraft mc, PlayerTrackingData[] pArr, int length) {
        this.longestDistanceStringWidth = 0;
        this.longestHPStringWidth = 0;
        this.longestEnergyStringWidth = 0;
        int longestYDistanceStringWidth = 0;
        this.hasNonZeroYDistanceValue = false;
        final boolean renderEnergy = MoreMWEConfig.showEnergyInCompassHUD && MWEApi.Scoreboard.getScoreboardParser().isInMwGame();
        for (int i = 0; i < length; i++) {
            final PlayerTrackingData p = pArr[i];

            final int distanceStrWidth = GuiUtil.getNumberWidth((int) p.distance);
            if (distanceStrWidth > longestDistanceStringWidth) longestDistanceStringWidth = distanceStrWidth;

            final int yDistanceStrWidth = getYDistanceTextWidth(p.distanceY, MoreMWEConfig.showYDistanceArrowsInCompassHUD);
            if (yDistanceStrWidth > longestYDistanceStringWidth) longestYDistanceStringWidth = yDistanceStrWidth;
            if (p.distanceY != 0) hasNonZeroYDistanceValue = true;

            if (MoreMWEConfig.showHPInCompassHUD) {
                final int hpStringWidth = GuiUtil.getNumberWidth(getDisplayHP(p.health));
                if (hpStringWidth > longestHPStringWidth) longestHPStringWidth = hpStringWidth;
            }

            if (renderEnergy) {
                final int experienceStringWidth = GuiUtil.getNumberWidth(p.experienceLevel);
                if (experienceStringWidth > longestEnergyStringWidth) longestEnergyStringWidth = experienceStringWidth;
            }
        }
        if (longestDistanceStringWidth != 0 && MoreMWEConfig.showDistanceUnitInCompassHUD) {
            final int charWidth = mc.fontRendererObj.getCharWidth('m');
            longestDistanceStringWidth += charWidth;
        }
        if (longestHPStringWidth != 0 && MoreMWEConfig.showHPIconInCompassHUD) {
            longestHPStringWidth += mc.fontRendererObj.getStringWidth(" ❤");
        }

        int maxColumnWidth = SKIN_WIDTH + longestDistanceStringWidth + longestYDistanceStringWidth + MoreMWEConfig.compassHUDArrowMargin + 5 + MoreMWEConfig.compassHUDArrowMargin + longestHPStringWidth + longestEnergyStringWidth;
        if (longestHPStringWidth != 0) maxColumnWidth += COLUMN_GAP;
        if (renderEnergy) maxColumnWidth += COLUMN_GAP;
        return maxColumnWidth;
    }

    private static int getMaxCompassTrackedPlayersCount() {
        return MathHelper.clamp_int(MoreMWEConfig.maxCompassTrackedPlayers, 1, REAL_MAX_COMPASS_TRACKED);
    }

    private static int getRowHeight() {
        return 8 + ROW_GAP;
    }

    private static int getDisplayHP(float health) {
        return health > 999 ? 999 : ((int) health);
    }

    public static String getYDistanceText(int yDistance, boolean showArrows) {
        if (yDistance == 0) return "0";
        return showArrows ?
                ((yDistance < 0 ? '▼' : '▲') + " " + StringUtil.getIntWithoutSign(yDistance))
                : StringUtil.getIntWithSign(yDistance, false);
    }

    public static int getYDistanceTextWidth(int yDistance, boolean showArrows) {
        return showArrows ?
                (yDistance == 0 ? GuiUtil.DIGIT_AND_SIGN_WIDTH : (GuiUtil.getNumberWithoutSignWidth(yDistance) + Y_DISTANCE_ARROW_PREFIX_WIDTH))
                : GuiUtil.getNumberWidth(yDistance, true, false);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        final Minecraft mc;
        if ((mc = Minecraft.getMinecraft()).theWorld == null || mc.thePlayer == null || !isEnabled(mc, -1)) {
            trackedPlayersCount = 0;
            return;
        }

        final EntityPlayerSP thePlayer = mc.thePlayer;
        final boolean isInMwGame = MWEApi.Scoreboard.getScoreboardParser().isInMwGame();
        try {
            final char selfTeamChar = (isInMwGame && MoreMWEConfig.renderCustomTeammateColor) ? MWEApi.Player.getPlayerInfo(thePlayer).getPlayerTeamColor() : '\0';
            final NetHandlerPlayClient netHandler = mc.getNetHandler();
            final int trackedCount = populateTrackedPlayers(mc, thePlayer, isInMwGame);
            for (int i = 0; i < trackedCount; i++) {
                compassTrackedPlayers[i].update(mc, netHandler, isInMwGame, thePlayer, scratchPlayers[i], selfTeamChar);
                scratchPlayers[i] = null;
                //scratchPlayerDistancesSq[i] = 0;
                //scratchPlayerToTeamIndex[i] = 0;
            }
            Arrays.sort(compassTrackedPlayers, 0, trackedCount, HUD_ORDER);
            trackedPlayersCount = trackedCount;
        }
        catch (Throwable t) {
            Arrays.fill(scratchPlayers, null);
            trackedPlayersCount = 0;
            throw t;
        }
    }

    private int populateTrackedPlayers(Minecraft mc, EntityPlayerSP thePlayer, boolean isInMwGame) {
        final boolean ignoreOtherSpectators = MoreMWEConfig.ignoreOtherSpectators && PlayerUtil.isTheMcPlayerSpectator(thePlayer);
        final boolean prioritizeUniqueTeams = isInMwGame && MoreMWEConfig.prioritizeUniqueTeams;
        final int maxCompassTrackedPlayers = getMaxCompassTrackedPlayersCount();
        final int maxCompassTrackingDistance = Math.max(MoreMWEConfig.maxCompassTrackingDistance, 0);
        final double maxCompassTrackingDistanceSq = maxCompassTrackingDistance * maxCompassTrackingDistance;

        // used if (prioritizeUniqueTeams == true)
        final int amountOfTeams = scratchTeamPlayerCounts.length;
        int maxTeamCount = 0;

        // used if (prioritizeUniqueTeams == false)
        int furthestPlayerIndex = -1;
        double furthestDistanceSq = -1;

        int trackedCount = 0;

        if (prioritizeUniqueTeams) {
            for (int i = 0; i < amountOfTeams; i++) {
                scratchTeamPlayerCounts[i] = 0;
                scratchTeamFurthestPlayerIndexes[i] = -1;
                scratchTeamFurthestDistancesSq[i] = -1;
            }
        }

        for (final EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == null
                    || player == thePlayer
                    || player.isDead
                    || player.ticksExisted < 20
                    || !isValidPlayer(player.getUniqueID())
                    || (ignoreOtherSpectators && isPlayerLikelySpectator(player)))
            {
                continue;
            }

            final double distanceSq = thePlayer.getDistanceSqToEntity(player);
            if (maxCompassTrackingDistanceSq > 0 && distanceSq > maxCompassTrackingDistanceSq) continue;

            int teamIndex = 0; // treat players with "no team" as a separate team
            if (prioritizeUniqueTeams) {
                final MWTeam team = PlayerUtil.getPlayerTeam(player);
                if (team != null) teamIndex = team.ordinal() + 1;
            }

            if (trackedCount != maxCompassTrackedPlayers) {
                scratchPlayers[trackedCount] = player;
                scratchPlayerDistancesSq[trackedCount] = distanceSq;
                if (prioritizeUniqueTeams) {
                    scratchPlayerToTeamIndex[trackedCount] = teamIndex;
                    final int teamCount = ++scratchTeamPlayerCounts[teamIndex];
                    if (teamCount > maxTeamCount) maxTeamCount = teamCount;

                    if (distanceSq > scratchTeamFurthestDistancesSq[teamIndex]) {
                        scratchTeamFurthestDistancesSq[teamIndex] = distanceSq;
                        scratchTeamFurthestPlayerIndexes[teamIndex] = trackedCount;
                    }
                }
                else if (distanceSq > furthestDistanceSq) {
                    furthestDistanceSq = distanceSq;
                    furthestPlayerIndex = trackedCount;
                }
                trackedCount++;
                continue;
            }

            if (prioritizeUniqueTeams) {
                final int teamCount = scratchTeamPlayerCounts[teamIndex];
                int targetIndex = -1;
                double targetDistanceSq = -1;
                boolean isFromMinorityTeam = false;
                if (teamCount == maxTeamCount) {
                    targetIndex = scratchTeamFurthestPlayerIndexes[teamIndex];
                    targetDistanceSq = scratchTeamFurthestDistancesSq[teamIndex];
                }
                else {
                    // find the furthest player who is in a team with most players
                    for (int i = 0; i < amountOfTeams; i++) {
                        if (scratchTeamPlayerCounts[i] == maxTeamCount && scratchTeamFurthestDistancesSq[i] > targetDistanceSq) {
                            targetDistanceSq = scratchTeamFurthestDistancesSq[i];
                            targetIndex = scratchTeamFurthestPlayerIndexes[i];
                        }
                    }
                    isFromMinorityTeam = teamCount != (maxTeamCount - 1);

                    // if not from minority team, replace your team's furthest player if
                    // he is further away than the furthest player of a team with most players
                    if (!isFromMinorityTeam && scratchTeamFurthestDistancesSq[teamIndex] > targetDistanceSq) {
                        targetDistanceSq = scratchTeamFurthestDistancesSq[teamIndex];
                        targetIndex = scratchTeamFurthestPlayerIndexes[teamIndex];
                    }
                }

                if (targetIndex != -1 && (isFromMinorityTeam || distanceSq < targetDistanceSq)) {
                    scratchPlayers[targetIndex] = player;
                    scratchPlayerDistancesSq[targetIndex] = distanceSq;
                    final int replacedPlayerTeamIndex = scratchPlayerToTeamIndex[targetIndex];
                    final int replacedPlayerTeamCount = scratchTeamPlayerCounts[replacedPlayerTeamIndex];
                    if (replacedPlayerTeamIndex != teamIndex) {
                        scratchPlayerToTeamIndex[targetIndex] = teamIndex;
                        final int newTeamCount = ++scratchTeamPlayerCounts[teamIndex];
                        scratchTeamPlayerCounts[replacedPlayerTeamIndex]--;
                        if (newTeamCount < maxTeamCount) {
                            maxTeamCount = 0;
                            for (int i = 0; i < amountOfTeams; i++) {
                                if (scratchTeamPlayerCounts[i] > maxTeamCount) maxTeamCount = scratchTeamPlayerCounts[i];
                            }
                        }
                    }
                    scratchTeamFurthestPlayerIndexes[replacedPlayerTeamIndex] = -1;
                    scratchTeamFurthestDistancesSq[replacedPlayerTeamIndex] = -1;
                    if (replacedPlayerTeamCount > 1) {
                        int idx = -1;
                        double dSq = -1;
                        for (int i = 0; i < trackedCount; i++) {
                            if (scratchPlayerToTeamIndex[i] == replacedPlayerTeamIndex && scratchPlayerDistancesSq[i] > dSq) {
                                dSq = scratchPlayerDistancesSq[i];
                                idx = i;
                            }
                        }
                        scratchTeamFurthestPlayerIndexes[replacedPlayerTeamIndex] = idx;
                        scratchTeamFurthestDistancesSq[replacedPlayerTeamIndex] = dSq;
                    }

                    if (distanceSq > scratchTeamFurthestDistancesSq[teamIndex]) {
                        scratchTeamFurthestDistancesSq[teamIndex] = distanceSq;
                        scratchTeamFurthestPlayerIndexes[teamIndex] = targetIndex;
                    }
                }
            }
            else if (distanceSq < furthestDistanceSq) {
                scratchPlayers[furthestPlayerIndex] = player;
                scratchPlayerDistancesSq[furthestPlayerIndex] = distanceSq;
                furthestDistanceSq = -1;
                for (int i = 0; i < trackedCount; i++) {
                    if (scratchPlayerDistancesSq[i] > furthestDistanceSq) {
                        furthestDistanceSq = scratchPlayerDistancesSq[i];
                        furthestPlayerIndex = i;
                    }
                }
            }
        }

        return trackedCount;
    }

    private static boolean isValidPlayer(UUID uuid) {
        if (uuid == null) return false;
        final int value = uuid.version();
        return value == 1 || value == 4;
    }

    private static boolean isPlayerLikelySpectator(EntityPlayer player) {
        //if (player == null) return false;
        return player.isSpectator() || (
                player.isInvisible() && getPlayerTeamColorChar(player) == '7'
        );
    }

    private static char getPlayerTeamColorChar(EntityPlayer player) {
        final ScorePlayerTeam team = (ScorePlayerTeam) player.getTeam();
        if (team != null) {
            final String teamColorStr = team.getColorPrefix();
            if (teamColorStr != null && teamColorStr.length() > 1 && teamColorStr.charAt(0) == '§') {
                final char ch = teamColorStr.charAt(1);
                if (StringUtil.isColorCode(ch))
                    return ch;
            }
        }
        return '\0';
    }


    private static final class PlayerTrackingData {
        public ResourceLocation skin;
        public int teamColor;
        public int distanceY;
        public double distance;
        public float relativeYaw;
        public float health;
        public int experienceLevel;

        public PlayerTrackingData() { this.skin = DefaultPlayerSkin.getDefaultSkinLegacy(); }

        public void update(Minecraft mc, NetHandlerPlayClient netHandler, boolean isInMwGame, EntityPlayerSP theMcPlayer, EntityPlayer trackedPlayer, char myTeamColorChar) {
            final double distX = trackedPlayer.posX - theMcPlayer.posX;
            final double distY = trackedPlayer.posY - theMcPlayer.posY;
            final double distZ = trackedPlayer.posZ - theMcPlayer.posZ;
            this.distanceY = (int) distY;
            this.distance = Math.sqrt(distX * distX + distY * distY + distZ * distZ);

            this.health = trackedPlayer.getHealth();
            this.experienceLevel = trackedPlayer.experienceLevel;

            final float angle = (float) Math.toDegrees(Math.atan2(distZ, distX)) - 90;
            float relativeYaw = angle - theMcPlayer.rotationYaw;
            relativeYaw = (relativeYaw + 180) % 360;
            if (relativeYaw < 0) relativeYaw += 360;
            relativeYaw -= 180;
            this.relativeYaw = relativeYaw;

            final NetworkPlayerInfo info = netHandler.getPlayerInfo(trackedPlayer.getUniqueID());
            this.skin = info != null ? info.getLocationSkin() : DefaultPlayerSkin.getDefaultSkinLegacy();

            int teamColorIn = ModResources.WHITE;
            if (isInMwGame) {
                final char playerTeamColorChar = MWEApi.Player.getPlayerInfo(trackedPlayer).getPlayerTeamColor();
                if (playerTeamColorChar != '\0') {
                    if (playerTeamColorChar == myTeamColorChar) teamColorIn = MoreMWEConfig.customTeammateColor;
                    else if (StringUtil.isColorCode(playerTeamColorChar)) teamColorIn = ColorUtil.toOpaque(mc.fontRendererObj.getColorCode(StringUtil.toLowerCase(playerTeamColorChar)));
                }
            }
            this.teamColor = teamColorIn;
        }

        // create dummy
        private PlayerTrackingData(ResourceLocation skin, int teamColor, double distance, int distanceY, float health, int experienceLevel) {
            this.skin = skin;
            this.teamColor = teamColor;
            this.distanceY = distanceY;
            this.distance = distance;
            this.health = health;
            this.relativeYaw = 0;
            this.experienceLevel = experienceLevel;
        }
    }
}
package me.shvaich.moremwe.gui.huds;


import fr.alexdoru.configlib.api.ColorPalette.VanillaColors;
import fr.alexdoru.mwe.api.MWEApi;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

public class CompassHUD extends AbstractRenderer {

    public static final int REAL_MAX_COMPASS_TRACKED = 8;
    public static final int COLUMN_GAP = 6;
    private static final int ROW_GAP = 2;

    private final PlayerTrackingData[] dummyPlayers;
    private final PlayerTrackingData[] compassTrackedPlayers = new PlayerTrackingData[REAL_MAX_COMPASS_TRACKED];
    private int trackedPlayersCount;
    private int skinWidth;
    private int longestDistanceStringWidth;
    private int longestHPStringWidth;

    public CompassHUD() {
        super(MoreMWEConfig.compassHUDPosition);
        this.dummyPlayers = new PlayerTrackingData[]{
                new PlayerTrackingData(new ResourceLocation("textures/entity/steve.png"), VanillaColors.GREEN, 0, 0, 40),
                new PlayerTrackingData(new ResourceLocation("textures/entity/alex.png"), VanillaColors.BLUE, 1, 1, 20),
                new PlayerTrackingData(new ResourceLocation("textures/entity/zombie/zombie.png"), VanillaColors.RED, -1, 5, 10),
                new PlayerTrackingData(new ResourceLocation("textures/entity/zombie_pigman.png"), VanillaColors.YELLOW, 5, 1, 5)
        };
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isEnabled(Minecraft mc, long currentTimeMillis) {
        if (!rendererPosition.isEnabled()
                || KeybindingListener.hideCompassHUDFromKey()
                || (!MoreMWEConfig.showCompassWhenSpectator && PlayerUtil.isTheMcPlayerSpectator(mc.thePlayer))
        ) return false;

        if (!MWEApi.Scoreboard.getScoreboardParser().isInMwGame()) return !MoreMWEConfig.isCompassForMegaWallsOnly;
        switch (MoreMWEConfig.compassMegaWallsRenderCondition) {
            case 0: return true;
            case 1: return !MWEApi.Scoreboard.getScoreboardParser().isPrepPhase();
            case 2: return MWEApi.Scoreboard.getScoreboardParser().getWitherCount() < 4;
            case 3: return MWEApi.Scoreboard.getScoreboardParser().isDeathmatch();
            default: return false; // will never run
        }
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
        renderCompassHUD(mc, dummyPlayers, renderedDummiesCount, thisWidth, thisHeight);
        if (renderedDummiesCount < MoreMWEConfig.maxCompassTrackedPlayers) {
            final int missing = MoreMWEConfig.maxCompassTrackedPlayers - renderedDummiesCount;
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

        if (renderedDummiesCount < MoreMWEConfig.maxCompassTrackedPlayers)
            height += ROW_GAP + mc.fontRendererObj.FONT_HEIGHT - 1;

        return height;
    }

    private void renderCompassHUD(Minecraft mc, PlayerTrackingData[] pArr, int length, int thisWidth, int thisHeight) {
        final int x = rendererPosition.getDrawX();
        int y = rendererPosition.getDrawY();
        int distanceX = x + skinWidth;
        int arrowX = distanceX + longestDistanceStringWidth + MoreMWEConfig.compassHUDArrowMargin;
        int healthX = -1;
        final float thePlayerMaxHP;
        if (MoreMWEConfig.showHPInCompassHUD) {
            thePlayerMaxHP = mc.thePlayer.getMaxHealth();
            switch (MoreMWEConfig.hpCompassDisplayPosition) {
                case 0: {
                    healthX = x + thisWidth - longestHPStringWidth;
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
        else {
            thePlayerMaxHP = 0;
        }
        final int rowHeight = getRowHeight();
        final int realRowHeight = rowHeight - ROW_GAP;
        drawBackground(x, y, thisWidth, thisHeight);
        for (int i = 0; i < length; i++) {
            final PlayerTrackingData player = pArr[i];
            if (player.skin != null) {
                GuiUtil.drawSkinHead(mc, player.skin, x, y);
            }
            final int teamColor = player.teamColor;
            final String distanceStr = ((int) player.distance) + (MoreMWEConfig.showDistanceUnitInCompassHUD ? "m" : "");
            mc.fontRendererObj.drawStringWithShadow(distanceStr, distanceX, y, teamColor);

            final int arrowY = y + (realRowHeight - 6) / 2;
            GuiUtil.drawDirectionArrow(arrowX, arrowY, player.relativeYaw, teamColor);

            final int yDistanceX = arrowX + 5 + MoreMWEConfig.compassHUDArrowMargin;
            final int yDistance = (int) player.distanceY;
            final int yDistanceColor = (!MoreMWEConfig.colorYDistanceInCompassHUD || yDistance == 0) ? VanillaColors.GRAY
                    : (yDistance < 0 ? VanillaColors.DARK_RED : VanillaColors.DARK_GREEN);
            mc.fontRendererObj.drawStringWithShadow(StringUtil.getIntWithSign(yDistance, false), yDistanceX, y, yDistanceColor);
            if (healthX != -1) {
                String text = ColorUtil.getHPColor(thePlayerMaxHP, player.health) + String.valueOf(getDisplayHP(player.health));
                if (MoreMWEConfig.showHPIconInCompassHUD) {
                    text += EnumChatFormatting.RED + " ❤";
                }
                mc.fontRendererObj.drawStringWithShadow(text, healthX, y, ModResources.WHITE);
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
        this.skinWidth = 0;
        this.longestDistanceStringWidth = 0;
        this.longestHPStringWidth = 0;
        int longestYDistanceStringWidth = 0;
        for (int i = 0; i < length; i++) {
            final PlayerTrackingData p = pArr[i];

            if (p.skin != null && skinWidth == 0) this.skinWidth = 8 + 2;

            final int distanceStrWidth = GuiUtil.getNumberWidth((int) p.distance);
            if (distanceStrWidth > longestDistanceStringWidth) this.longestDistanceStringWidth = distanceStrWidth;

            final int yDistanceStrWidth = GuiUtil.getNumberWidth((int) p.distanceY, true, false);
            if (yDistanceStrWidth > longestYDistanceStringWidth) longestYDistanceStringWidth = yDistanceStrWidth;

            if (MoreMWEConfig.showHPInCompassHUD) {
                final int hpStringWidth = GuiUtil.getNumberWidth(getDisplayHP(p.health));
                if (hpStringWidth > longestHPStringWidth) this.longestHPStringWidth = hpStringWidth;
            }
        }
        if (longestDistanceStringWidth != 0 && MoreMWEConfig.showDistanceUnitInCompassHUD) {
            final int charWidth = mc.fontRendererObj.getCharWidth('m');
            this.longestDistanceStringWidth += charWidth;
        }
        if (longestHPStringWidth != 0 && MoreMWEConfig.showHPIconInCompassHUD) {
            this.longestHPStringWidth += mc.fontRendererObj.getStringWidth(" ❤");
        }
        return skinWidth + longestDistanceStringWidth + longestYDistanceStringWidth + MoreMWEConfig.compassHUDArrowMargin + 5 + MoreMWEConfig.compassHUDArrowMargin + longestHPStringWidth + (longestHPStringWidth == 0 ? 0 : COLUMN_GAP);
    }

    private int getRowHeight() {
        return 8 + ROW_GAP;
    }

    private int getDummiesToRenderCount() {
        return Math.min(dummyPlayers.length, MoreMWEConfig.maxCompassTrackedPlayers);
    }

    private static int getDisplayHP(float health) {
        return health > 999 ? 999 : ((int) health);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        for (int i = 0; i < trackedPlayersCount; i++) {
            compassTrackedPlayers[i] = null;
        }
        trackedPlayersCount = 0;

        final Minecraft mc;
        if ((mc = Minecraft.getMinecraft()).theWorld == null || mc.thePlayer == null || !isEnabled(mc, -1)) return;

        final EntityPlayerSP thePlayer = mc.thePlayer;
        final boolean isInMwGame = MWEApi.Scoreboard.getScoreboardParser().isInMwGame();
        final boolean ignoreOtherSpectators = MoreMWEConfig.ignoreOtherSpectators && PlayerUtil.isTheMcPlayerSpectator(thePlayer);
        final boolean prioritizeUniqueTeams = isInMwGame && MoreMWEConfig.prioritizeUniqueTeams;
        final int maxCompassTrackingDistanceSq = MoreMWEConfig.maxCompassTrackingDistance * MoreMWEConfig.maxCompassTrackingDistance;
        final int maxCompassTrackedPlayers = MoreMWEConfig.maxCompassTrackedPlayers;
        final char selfTeamChar = (isInMwGame && MoreMWEConfig.renderCustomTeammateColor) ? MWEApi.Player.getPlayerInfo(thePlayer).getPlayerTeamColor() : '\0';
        final NetHandlerPlayClient netHandler = mc.getNetHandler();
        final int[] teamCounts = prioritizeUniqueTeams ? new int[13] : null;
        int maxTeamCount = 0;
        for (final EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == null
                    || player == thePlayer
                    || player.isDead
                    || player.ticksExisted < 20
                    || !isValidPlayer(player.getUniqueID())
                    || (ignoreOtherSpectators && isPlayerLikelySpectator(player))) {
                continue;
            }

            if (maxCompassTrackingDistanceSq != 0) {
                final double distanceSq = thePlayer.getDistanceSqToEntity(player);
                if (distanceSq > maxCompassTrackingDistanceSq) continue;
            }

            final PlayerTrackingData pData = new PlayerTrackingData(player, thePlayer, netHandler, isInMwGame, selfTeamChar);
            if (trackedPlayersCount != maxCompassTrackedPlayers) {
                compassTrackedPlayers[trackedPlayersCount++] = pData;
                if (prioritizeUniqueTeams) {
                    final int teamCount = ++teamCounts[pData.teamColorIndex];
                    if (teamCount > maxTeamCount) maxTeamCount = teamCount;
                }
                continue;
            }
            if (prioritizeUniqueTeams) {
                // this might look a bit weird, but it works
                int targetIndex = -1;
                double targetDistance = -1;
                final int pTeamIndex = pData.teamColorIndex;
                final int pTeamCount = teamCounts[pTeamIndex];
                final boolean isTeamWithHighestCount = pTeamCount == maxTeamCount;
                final boolean teamHasPlayersAndIsOneLessThanHighestCount = !isTeamWithHighestCount && pTeamCount != 0 && pTeamCount == (maxTeamCount-1);
                for (int i = 0; i < maxCompassTrackedPlayers; i++) {
                    final PlayerTrackingData trackedPlayer = compassTrackedPlayers[i];
                    final int trackedPlayerTeamIndex = trackedPlayer.teamColorIndex;
                    final double trackedPlayerDistance = trackedPlayer.distance;
                    if (isTeamWithHighestCount) {
                        if (trackedPlayerTeamIndex == pTeamIndex && trackedPlayerDistance > targetDistance) {
                            targetDistance = trackedPlayerDistance;
                            targetIndex = i;
                        }
                    }
                    else if (teamHasPlayersAndIsOneLessThanHighestCount) {
                        if (teamCounts[trackedPlayerTeamIndex] == maxTeamCount) {
                            if (trackedPlayerDistance > targetDistance || (
                                    targetIndex == pTeamIndex
                                            && pData.distance < trackedPlayerDistance
                            )) {
                                targetDistance = trackedPlayerDistance;
                                targetIndex = i;
                            }
                        }
                        else if (trackedPlayerTeamIndex == pTeamIndex && (targetIndex == pTeamIndex || pData.distance >= targetDistance) && trackedPlayerDistance > targetDistance) {
                            targetDistance = trackedPlayerDistance;
                            targetIndex = i;
                        }
                    }
                    else if (teamCounts[trackedPlayerTeamIndex] == maxTeamCount) {
                        if (trackedPlayerDistance > targetDistance) {
                            targetDistance = trackedPlayerDistance;
                            targetIndex = i;
                        }
                    }
                }
                if (targetIndex != -1 && (!(isTeamWithHighestCount || teamHasPlayersAndIsOneLessThanHighestCount) || pData.distance < targetDistance)) {
                    final int replacedPlayerTeamIndex = compassTrackedPlayers[targetIndex].teamColorIndex;
                    compassTrackedPlayers[targetIndex] = pData;
                    teamCounts[replacedPlayerTeamIndex]--;
                    teamCounts[pTeamIndex]++;
                    maxTeamCount = 0;
                    for (final int count : teamCounts)
                        if (count > maxTeamCount) maxTeamCount = count;
                }
            }
            else {
                int furthestPlayerIndex = 0;
                double furthestDistance = compassTrackedPlayers[0].distance;
                for (int i = 1; i < maxCompassTrackedPlayers; i++) {
                    final double d = compassTrackedPlayers[i].distance;
                    if (d > furthestDistance) {
                        furthestDistance = d;
                        furthestPlayerIndex = i;
                    }
                }
                if (pData.distance < furthestDistance) {
                    compassTrackedPlayers[furthestPlayerIndex] = pData;
                }
            }
        }
        Arrays.sort(compassTrackedPlayers, 0, trackedPlayersCount, Comparator.comparingDouble(p -> p.distance));
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
        public final ResourceLocation skin;
        public final int teamColor;
        public final byte teamColorIndex; // DON'T USE THIS
        public final double distanceY;
        public final double distance;
        public final float relativeYaw;
        public final float health;

        public PlayerTrackingData(EntityPlayer trackedPlayer, EntityPlayerSP me, NetHandlerPlayClient netHandler, boolean isInMwGame, char myTeamChar) {
            final double distX = trackedPlayer.posX - me.posX;
            final double distY = trackedPlayer.posY - me.posY;
            final double distZ = trackedPlayer.posZ - me.posZ;
            this.distanceY = distY;
            this.distance = Math.sqrt(distX * distX + distY * distY + distZ * distZ);

            this.health = trackedPlayer.getHealth();

            final float angle = (float) Math.toDegrees(Math.atan2(distZ, distX)) - 90;
            float relativeYaw = angle - me.rotationYaw;
            relativeYaw = (relativeYaw + 180) % 360;
            if (relativeYaw < 0) relativeYaw += 360;
            relativeYaw -= 180;
            this.relativeYaw = relativeYaw;

            final NetworkPlayerInfo info = netHandler.getPlayerInfo(trackedPlayer.getUniqueID());
            this.skin = info != null ? info.getLocationSkin() : null;

            int teamColorIn = ModResources.WHITE;
            byte teamColorIndexIn = 0; // treat players with "no team" as a separate team
            final char teamColorChar;
            if (isInMwGame && (teamColorChar = MWEApi.Player.getPlayerInfo(trackedPlayer).getPlayerTeamColor()) != '\0') {
                switch (teamColorChar) {
                    case 'c': { // RED
                        teamColorIn = VanillaColors.RED;
                        teamColorIndexIn = 1;
                    } break;

                    case 'd': { // LIGHT_PURPLE
                        teamColorIn = VanillaColors.LIGHT_PURPLE;
                        teamColorIndexIn = 2;
                    } break;

                    case '5': { // DARK_PURPLE
                        teamColorIn = VanillaColors.DARK_PURPLE;
                        teamColorIndexIn = 3;
                    } break;

                    case 'a': { // GREEN
                        teamColorIn = VanillaColors.GREEN;
                        teamColorIndexIn = 4;
                    } break;

                    case '2': { // DARK_GREEN
                        teamColorIn = VanillaColors.DARK_GREEN;
                        teamColorIndexIn = 5;
                    } break;

                    case '8': { // DARK_GRAY
                        teamColorIn = VanillaColors.DARK_GRAY;
                        teamColorIndexIn = 6;
                    } break;

                    case '9': { // BLUE
                        teamColorIn = VanillaColors.BLUE;
                        teamColorIndexIn = 7;
                    } break;

                    case '1': { // DARK_BLUE
                        teamColorIn = VanillaColors.DARK_BLUE;
                        teamColorIndexIn = 8;
                    } break;

                    case '3': { // DARK_AQUA
                        teamColorIn = VanillaColors.DARK_AQUA;
                        teamColorIndexIn = 9;
                    } break;

                    case 'e': { // YELLOW
                        teamColorIn = VanillaColors.YELLOW;
                        teamColorIndexIn = 10;
                    } break;

                    case '6': { // GOLD
                        teamColorIn = VanillaColors.GOLD;
                        teamColorIndexIn = 11;
                    } break;

                    case 'f': { // WHITE
                        teamColorIn = ModResources.WHITE;
                        teamColorIndexIn = 12;
                    } break;
                }
                // FACT: myTeamChar == '\0' if: MoreMWEConfig.renderCustomTeammateColor == false
                if (teamColorIndexIn != 0 && /* MoreMWEConfig.renderCustomTeammateColor && */ teamColorChar == myTeamChar) {
                    teamColorIn = MoreMWEConfig.customTeammateColor;
                }
            }
            this.teamColor = teamColorIn;
            this.teamColorIndex = teamColorIndexIn;
        }

        // create dummy
        private PlayerTrackingData(ResourceLocation skin, int teamColor, double distanceY, double distance, float health) {
            this.skin = skin;
            this.teamColor = teamColor;
            this.distanceY = distanceY;
            this.distance = distance;
            this.health = health;
            this.teamColorIndex = -1;
            this.relativeYaw = 0;
        }
    }
}
package me.shvaich.moremwe.gui.huds;

import fr.alexdoru.configlib.api.ColorPalette;
import fr.alexdoru.mwe.api.ISquadInfoRenderer;
import fr.alexdoru.mwe.api.MWEApi;
import me.shvaich.moremwe.config.MWEHookConfig;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.GuiUtil;
import me.shvaich.moremwe.utils.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SquadPositionInfo implements ISquadInfoRenderer {

    private static final int ARROW_LEFT_MARGIN = 8;

    @Override
    public int getWidth(@NotNull NetworkPlayerInfo netInfo, @Nullable EntityPlayer entityPlayer) {
        if (entityPlayer == null) return 0;
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.thePlayer == entityPlayer || !isEnabled(mc)) return 0;
        final int distance = (int) entityPlayer.getDistanceToEntity(mc.thePlayer);
        final int yDistance = (int) (entityPlayer.posY - mc.thePlayer.posY);
        return mc.fontRendererObj.getStringWidth(getDistanceText(distance, yDistance)) + ARROW_LEFT_MARGIN + 5 + 2;
    }

    @Override
    public void render(@NotNull NetworkPlayerInfo netInfo, @Nullable EntityPlayer entityPlayer, int x, int y, int reservedWidth) {
        if (entityPlayer == null) return;
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.thePlayer == entityPlayer) return;

        final double distX = entityPlayer.posX - mc.thePlayer.posX;
        final double distY = entityPlayer.posY - mc.thePlayer.posY;
        final double distZ = entityPlayer.posZ - mc.thePlayer.posZ;

        final int distance = (int) Math.sqrt(distX * distX + distY * distY + distZ * distZ);
        final float angle = (float) Math.toDegrees(Math.atan2(distZ, distX)) - 90;
        float rotation = angle - mc.thePlayer.rotationYaw;
        rotation = (rotation + 180) % 360;
        if (rotation < 0) rotation += 360;
        rotation -= 180;

        int drawX = x;
        drawX = mc.fontRendererObj.drawStringWithShadow(getDistanceText(distance, (int) distY), drawX, y, ModResources.WHITE);
        drawX += ARROW_LEFT_MARGIN;
        GuiUtil.drawDirectionArrow(drawX, y + 1, rotation, ColorPalette.VanillaColors.LIGHT_PURPLE);
    }

    private static boolean isEnabled(Minecraft mc) {
        if (!MWEHookConfig.showCompassInfoInSquadHUD) return false;
        if (!MWEHookConfig.showSquadHUDCompassInfoWhenSpectator && PlayerUtil.isTheMcPlayerSpectator(mc.thePlayer)) return false;
        if (!MWEApi.Scoreboard.getScoreboardParser().isInMwGame()) return !MWEHookConfig.isSquadHUDCompassInfoForMegaWallsOnly;
        switch (MWEHookConfig.squadHUDCompassInfoMegaWallsRenderCondition) {
            case DURING_GAME: return true;
            case AFTER_WALLS_FALL: return !MWEApi.Scoreboard.getScoreboardParser().isPrepPhase();
            case AFTER_FIRST_WITHER_DEATH: return MWEApi.Scoreboard.getScoreboardParser().getWitherCount() < 4;
            case DURING_DEATHMATCH: return MWEApi.Scoreboard.getScoreboardParser().isDeathmatch();
            default: return false;
        }
    }

    private static String getDistanceText(int distance, int yDistance) {
        final EnumChatFormatting yDistanceColor = (!MWEHookConfig.colorYDistanceInSquadHUDCompassInfo || yDistance == 0) ? MWEHookConfig.squadCompassInfoDefaultYDistanceColor :
                (yDistance > 0 ? MWEHookConfig.squadCompassInfoPlayerAboveColor : MWEHookConfig.squadCompassInfoPlayerBelowColor);
        return "(" + EnumChatFormatting.LIGHT_PURPLE + distance + (MWEHookConfig.showDistanceUnitInSquadHUDCompassInfo ? "m" : "")
                + "  " + yDistanceColor + yDistance + EnumChatFormatting.RESET + ")";
    }
}

//    private final List<PosData> posDataList = new ArrayList<>();
//
//    public void clearData() { posDataList.clear(); }
//
//    public void processData(int listSize, List<NetworkPlayerInfo> netInfoList, List<EntityPlayer> playerList) {
//        final EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
//        if (thePlayer == null) {
//            posDataList.addAll(Collections.nCopies(listSize, null));
//            return;
//        }
//        for (int i = 0; i < listSize; i++) {
//            final EntityPlayer player = playerList.get(i);
//            posDataList.add(player == null ? null : new PosData(thePlayer, player));
//        }
//    }
//
//    public int getWidth(int listIndex, @NotNull NetworkPlayerInfo netInfo, @Nullable EntityPlayer entityPlayer) {
//        final PosData posData = posDataList.get(listIndex);
//        if (posData == null) return 0;
//        final Minecraft mc = Minecraft.getMinecraft();
//        return mc.fontRendererObj.getCharWidth('(') * 2
//                + GuiUtil.getNumberWidth(posData.distance) + 6
//                + (MoreMWEConfig.showDistanceUnitInCompassHUD ? mc.fontRendererObj.getCharWidth('m') : 0)
//                + GuiUtil.getNumberWidth(posData.yDistance, true, false) + 6
//                + 5;
//    }
//
//    public void render(int listIndex, @NotNull NetworkPlayerInfo netInfo, @Nullable EntityPlayer entityPlayer, int x, int y, int reservedWidth, int reservedHeight) {
//        final PosData posData = posDataList.get(listIndex);
//        if (posData == null) return;
//        final Minecraft mc = Minecraft.getMinecraft();
//
//        final int bracketsColor = ModResources.WHITE;
//        int drawX = x;
//        final String distanceString = EnumChatFormatting.LIGHT_PURPLE.toString() + posData.distance + (MoreMWEConfig.showDistanceUnitInCompassHUD ? "m" : "");
//        drawX = mc.fontRendererObj.drawStringWithShadow("(" + distanceString, drawX, y, bracketsColor);
//        drawX += 6;
//
//        drawX = mc.fontRendererObj.drawStringWithShadow(CompassHelper.getYDistanceText(posData.yDistance) + EnumChatFormatting.RESET + ")", drawX, y, bracketsColor);
//        drawX += 6;
//
//        CompassHelper.drawDirectionArrow(drawX, y, posData.rotation, ColorPalette.VanillaColors.LIGHT_PURPLE);
//    }
//
//    private static final class PosData {
//        public final int distance;
//        public final int yDistance;
//        public final float rotation;
//        public PosData(EntityPlayerSP thePlayer, EntityPlayer player) {
//            final double distX = player.posX - thePlayer.posX;
//            final double distY = player.posY - thePlayer.posY;
//            final double distZ = player.posZ - thePlayer.posZ;
//            this.distance = (int) Math.sqrt(distX * distX + distY * distY + distZ * distZ);
//            this.yDistance = (int) distY;
//            final float angle = (float) Math.toDegrees(Math.atan2(distZ, distX)) - 90;
//            float rotationIn = angle - thePlayer.rotationYaw;
//            rotationIn = (rotationIn + 180) % 360;
//            if (rotationIn < 0) rotationIn += 360;
//            rotationIn -= 180;
//            this.rotation = rotationIn;
//        }
//    }
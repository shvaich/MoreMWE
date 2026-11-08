package me.shvaich.moremwe.utils;

import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.enums.MWClass;
import fr.alexdoru.mwe.api.enums.MWTeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.*;

public final class PlayerUtil {
    private PlayerUtil() {}

    public static boolean isTheMcPlayerSpectator(EntityPlayerSP thePlayer) {
        if (thePlayer == null) return false;
        if (thePlayer.isSpectator()) return true;
        final PotionEffect invisEffect = thePlayer.getActivePotionEffect(Potion.invisibility);
        return invisEffect != null && invisEffect.getIsPotionDurationMax() && thePlayer.capabilities.allowFlying && !thePlayer.capabilities.isCreativeMode;
    }

    public static List<String> getTabPlayerNames(boolean ignoreSelf) {
        final Minecraft mc = Minecraft.getMinecraft();
        final NetHandlerPlayClient netHandler = mc.getNetHandler();
        if (netHandler == null) return new ArrayList<>();
        UUID thePlayerUUID = null;
        if (ignoreSelf && mc.thePlayer != null) {
            thePlayerUUID = mc.thePlayer.getUniqueID();
        }
        final boolean tryIgnoreSelf = thePlayerUUID != null;
        final Collection<NetworkPlayerInfo> playerInfoMap = netHandler.getPlayerInfoMap();
        final List<String> players = new ArrayList<>(playerInfoMap.size());
        for (final NetworkPlayerInfo netInfo : playerInfoMap) {
            final String playerName = netInfo.getGameProfile().getName();
            if (playerName != null && !(tryIgnoreSelf && thePlayerUUID.equals(netInfo.getGameProfile().getId()))) {
                players.add(playerName);
            }
        }
        return players;
    }

    public static List<String> getTabPlayerNames() { return getTabPlayerNames(true); }

    public static boolean isValidUsername(String name) {
        if (name == null) return false;
        final int len = name.length();
        if (len < 2 || len > 16) return false;
        for (int i = 0; i < len; i++) {
            final char ch = name.charAt(i);
            if (
                !(ch >= 'a' && ch <= 'z')
                && !(ch >= 'A' && ch <= 'Z')
                && !(ch >= '0' && ch <= '9')
                && ch != '_'
            ) return false;
        }
        return true;
    }

    public static MWTeam getPlayerTeam(EntityPlayer player) {
        if (player == null) return null;
        final char teamChar = MWEApi.Player.getPlayerInfo(player).getPlayerTeamColor();
        return teamChar != '\0' ? MWTeam.fromColorChar(teamChar) : null;
    }

    /** Convenient way to get a player's MWClass without worrying about {@link NullPointerException} */
    public static MWClass getPlayerMWClass(EntityPlayer player) {
        return player == null ? null : MWEApi.Player.getPlayerInfo(player).getMWClass();
    }
}

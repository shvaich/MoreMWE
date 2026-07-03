package me.shvaich.moremwe.asm.hooks;

import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.enums.MWClass;
import me.shvaich.moremwe.config.MoreMWEConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;

public class MinecraftHook_ShovelGuard {
    public static boolean shouldCancelRightClick(Minecraft mc) {
        if (MoreMWEConfig.shovelEntityInteractionCondition != 2 && doShovelGuard(mc.thePlayer)) {
            if (MoreMWEConfig.shovelEntityInteractionCondition == 1 && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
                final char thePlayerTeam = MWEApi.Player.getPlayerInfo(mc.thePlayer).getPlayerTeamColor();
                return thePlayerTeam == '\0' || thePlayerTeam == MWEApi.Player.getPlayerInfo((EntityPlayer) mc.objectMouseOver.entityHit).getPlayerTeamColor();
            }
            return true;
        }
        return false;
    }

    public static boolean doShovelGuard(EntityPlayer thePlayer) {
        if (thePlayer != null
                && MoreMWEConfig.isShovelGuarded
                && MWEApi.Scoreboard.getScoreboardParser().isInMwGame()
                && MWEApi.Player.getPlayerInfo(thePlayer).getMWClass() == MWClass.MOLEMAN
        ) {
            final ItemStack itemStack = thePlayer.getHeldItem();
            return itemStack != null && itemStack.getItem() instanceof ItemSpade;
        }
        return false;
    }
}

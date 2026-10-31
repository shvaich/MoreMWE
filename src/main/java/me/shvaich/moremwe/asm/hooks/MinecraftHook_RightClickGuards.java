package me.shvaich.moremwe.asm.hooks;

import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.enums.MWClass;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.features.GuardableBlock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public final class MinecraftHook_RightClickGuards {

    public static boolean shouldCancelRightClick(Minecraft mc, ItemStack heldItem) {
        if (mc.objectMouseOver == null) return false;
        final EntityPlayerSP thePlayer = mc.thePlayer;
        switch (mc.objectMouseOver.typeOfHit) {
            case ENTITY: {
                if (MoreMWEConfig.shovelEntityInteractionCondition != 2 && isShovelGuardEnabled(thePlayer, heldItem)) {
                    if (MoreMWEConfig.shovelEntityInteractionCondition == 1 && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
                        final char thePlayerTeam = MWEApi.Player.getPlayerInfo(thePlayer).getPlayerTeamColor();
                        return thePlayerTeam != '\0' && thePlayerTeam == MWEApi.Player.getPlayerInfo((EntityPlayer) mc.objectMouseOver.entityHit).getPlayerTeamColor();
                    }
                    return true;
                }
                return false;
            }

            case BLOCK: {
                final WorldClient theWorld = mc.theWorld;
                final BlockPos blockPos = mc.objectMouseOver.getBlockPos();
                GuardableBlock guardableBlock = null;
                boolean hasFetchedGuardableBlock = false;
                if (isBlockGuardEnabled()) {
                    final Block clickedBlock = theWorld.getBlockState(blockPos).getBlock();
                    if (!clickedBlock.isAir(theWorld, blockPos)) {
                        if (MoreMWEConfig.canPickaxeOverrideBlockGuard && heldItem != null && heldItem.getItem() instanceof ItemPickaxe) {
                            return false;
                        }

                        if (!thePlayer.isSneaking() || (MoreMWEConfig.guardSneakAndEmptyHandClicks && heldItem == null)) {
                            guardableBlock = GuardableBlock.fromBlock(clickedBlock);
                            if (guardableBlock != null && MoreMWEConfig.guardedBlocks.contains(guardableBlock.key)) {
                                return true;
                            }
                            hasFetchedGuardableBlock = true;
                        }
                    }
                }

                if (isShovelGuardEnabled(thePlayer, heldItem)) {
                    return !MoreMWEConfig.canOverrideShovelGuard
                            || (hasFetchedGuardableBlock ? guardableBlock : GuardableBlock.fromBlock(theWorld.getBlockState(blockPos).getBlock())) == null;
                }
                return false;
            }
        }
        return isShovelGuardEnabled(thePlayer, heldItem);
    }

    private static boolean isBlockGuardEnabled() {
        return MoreMWEConfig.areBlocksGuarded
                && !MoreMWEConfig.guardedBlocks.isEmpty()
                && MWEApi.Scoreboard.getScoreboardParser().isInMwGame() ? MoreMWEConfig.blockGuardMegaWallsCondition.isActive(MWEApi.Scoreboard.getScoreboardParser()) : !MoreMWEConfig.isBlockGuardForMegaWallsOnly;
    }

    private static boolean isShovelGuardEnabled(EntityPlayerSP thePlayer, ItemStack heldItem) {
        return MoreMWEConfig.isShovelGuarded
                && heldItem != null
                && heldItem.getItem() instanceof ItemSpade
                && MWEApi.Scoreboard.getScoreboardParser().isInMwGame()
                && MWEApi.Player.getPlayerInfo(thePlayer).getMWClass() == MWClass.MOLEMAN;
    }
}

package me.shvaich.moremwe.features;

import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.enums.MWClass;
import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PlayerEnergyDisplay {

    private int lastLevel;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        if (!MoreMWEConfig.sendEnergyMessageWhenFullyCharged
                || !MWEApi.Scoreboard.getScoreboardParser().isInMwGame()
                || !megaWallsCondition()
        ) {
            lastLevel = 0;
            return;
        }

        final int currentLevel = Minecraft.getMinecraft().thePlayer.experienceLevel;
        if (currentLevel >= 100 && lastLevel < 100) {
            sendEnergyMessage();
        }
        lastLevel = currentLevel;
    }

    private static boolean megaWallsCondition() {
        return MoreMWEConfig.energyMessageMegaWallsCondition == 0 ?
                MWEApi.Scoreboard.getScoreboardParser().getWitherCount() < 4
                : MWEApi.Scoreboard.getScoreboardParser().isDeathmatch();
    }

    public static void sendEnergyMessage() {
        final EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) {
            MoreMWE.LOGGER.error("Failed to send energy message.");
            return;
        }
        final String errorMsg;
        success: {
            if (!MWEApi.Scoreboard.getScoreboardParser().isInMwGame()) {
                errorMsg = "Energy status only works in Mega Walls!";
                break success;
            }

            if (PlayerUtil.isTheMcPlayerSpectator(thePlayer)) {
                errorMsg = "Energy status doesn't work as spectator!";
                break success;
            }

            final MWClass mwClass = MWEApi.Player.getPlayerInfo(thePlayer).getMWClass();
            if (mwClass == null) {
                errorMsg = "Failed to identify MW Class!";
                break success;
            }

            sendMessage(thePlayer, mwClass);
            return;
        }
        ChatUtil.addErrorMessage(errorMsg);
    }

    private static void sendMessage(EntityPlayerSP thePlayer, MWClass mwClass) {
        final String abilityName;
        final int energyPerMeleeHit, energyPerArrowHit;
        switch (mwClass) {
            case ANGEL: {
                abilityName = "Divine Intervention";
                energyPerMeleeHit = energyPerArrowHit = 12;
                break;
            }
            case ARCANIST: {
                abilityName = "Arcane Beam";
                energyPerMeleeHit = energyPerArrowHit = 34;
                break;
            }
            case ASSASSIN: {
                abilityName = "Shadow Cloak";
                energyPerMeleeHit = energyPerArrowHit = 10;
                break;
            }
            case AUTOMATON: {
                abilityName = "EMP";
                energyPerMeleeHit = energyPerArrowHit = 4;
                break;
            }
            case BLAZE: {
                abilityName = "Immolating Burst";
                energyPerMeleeHit = 8;
                energyPerArrowHit = 4;
                break;
            }
            case COW: {
                abilityName = "Soothing Moo";
                energyPerMeleeHit = 25;
                energyPerArrowHit = 20;
                break;
            }
            case CREEPER: {
                abilityName = "Detonate";
                energyPerMeleeHit = 30;
                energyPerArrowHit = 20;
                break;
            }
            case DRAGON: {
                abilityName = "Scorching Breath";
                energyPerMeleeHit = 12;
                energyPerArrowHit = 8;
                break;
            }
            case DREADLORD: {
                abilityName = "Shadow Burst";
                energyPerMeleeHit = energyPerArrowHit = 10;
                break;
            }
            case ENDERMAN: {
                abilityName = "Teleport";
                energyPerMeleeHit = energyPerArrowHit = 20;
                break;
            }
            case GOLEM: {
                abilityName = "Iron Punch";
                energyPerMeleeHit = energyPerArrowHit = 10;
                break;
            }
            case HEROBRINE: {
                abilityName = "Wrath";
                energyPerMeleeHit = energyPerArrowHit = 25;
                break;
            }
            case HUNTER: {
                abilityName = "Eagle's Eye";
                energyPerMeleeHit = 4;
                energyPerArrowHit = 8;
                break;
            }
            case MOLEMAN: {
                abilityName = "Dig";
                energyPerMeleeHit = energyPerArrowHit = 10;
                break;
            }
            case PHOENIX: {
                abilityName = "Spirit Bond";
                energyPerMeleeHit = 8;
                energyPerArrowHit = 14;
                break;
            }
            case PIGMAN: {
                abilityName = "Burning Soul";
                energyPerMeleeHit = energyPerArrowHit = 10;
                break;
            }
            case PIRATE: {
                abilityName = "Cannon Fire";
                energyPerMeleeHit = energyPerArrowHit = 12;
                break;
            }
            case RENEGADE: {
                abilityName = "Rend";
                energyPerMeleeHit = 13;
                energyPerArrowHit = 17;
                break;
            }
            case SHAMAN: {
                abilityName = "Tornado";
                energyPerMeleeHit = energyPerArrowHit = 10;
                break;
            }
            case SHARK: {
                abilityName = "From the Depths";
                energyPerMeleeHit = energyPerArrowHit = 18;
                break;
            }
            case SHEEP: {
                abilityName = "Wool War";
                energyPerMeleeHit = 10;
                energyPerArrowHit = 5;
                break;
            }
            case SKELETON: {
                abilityName = "Explosive Arrow";
                energyPerMeleeHit = 0;
                energyPerArrowHit = 20;
                break;
            }
            case SNOWMAN: {
                abilityName = "Ice Bolt";
                energyPerMeleeHit = energyPerArrowHit = 8;
                break;
            }
            case SPIDER: {
                abilityName = "Leap";
                energyPerMeleeHit = energyPerArrowHit = 8;
                break;
            }
            case SQUID: {
                abilityName = "Squid Splash";
                energyPerMeleeHit = energyPerArrowHit = 10;
                break;
            }
            case WEREWOLF: {
                abilityName = "Lycanthropy";
                energyPerMeleeHit = energyPerArrowHit = 10;
                break;
            }
            case ZOMBIE: {
                abilityName = "Circle of Healing";
                energyPerMeleeHit = energyPerArrowHit = 12;
                break;
            }

            default: {
                ChatUtil.addErrorMessage("Unknown Class: " + mwClass.className);
                return;
            }
        }
        final int energy = thePlayer.experienceLevel;
        if (energy >= 100) {
            thePlayer.sendChatMessage("✫ " + abilityName + " is fully charged! (100%)");
        }
        else {
            final String chargeTier = energy < 35 ? "low" : energy < 70 ? "medium" : "high";
            final String hitsNeededStr = getHitsNeededString(energy, energyPerMeleeHit, energyPerArrowHit);
            thePlayer.sendChatMessage(abilityName + " is at " + chargeTier + " charge. (" + energy + "%) -> (" + hitsNeededStr + ")");
        }
    }


    private static String getHitsNeededString(int energy, int energyPerMeleeHit, int energyPerArrowHit) {
        final int missingEnergy = 100 - energy;
        String hitsNeededStr = "";
        if (energyPerMeleeHit != 0) {
            final int meleeHitsNeeded = (missingEnergy + energyPerMeleeHit - 1) / energyPerMeleeHit;
            hitsNeededStr = "Need " + meleeHitsNeeded + " melee " + (meleeHitsNeeded == 1 ? "hit" : "hits");
        }
        if (energyPerArrowHit != 0) {
            final int arrowHitsNeeded = (missingEnergy + energyPerArrowHit - 1) / energyPerArrowHit;
            hitsNeededStr += hitsNeededStr.isEmpty() ? "Need " : " or ";
            hitsNeededStr += arrowHitsNeeded + " arrow " + (arrowHitsNeeded == 1 ? "hit" : "hits");
        }
        return hitsNeededStr;
    }
}
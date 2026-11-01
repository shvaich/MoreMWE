package me.shvaich.moremwe.commands.mwcommands;

import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.features.PlayerEnergyDisplay;
import me.shvaich.moremwe.utils.ChatUtil;

import java.util.Collections;
import java.util.List;

public class MWCommandEnergy extends AbstractMWCommand {
    @Override
    public String getCommandName() {
        return "energy";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("e");
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length > 0) {
            final String subcommand = args[0];
            if (isHelpSubcommand(subcommand)) {
                printHelp();
                return;
            }
            if ("t".equalsIgnoreCase(subcommand) || "toggle".equalsIgnoreCase(subcommand)) {
                MoreMWEConfig.sendEnergyMessageWhenFullyCharged = !MoreMWEConfig.sendEnergyMessageWhenFullyCharged;
                ChatUtil.addPropertyMessage("Automatically Announce Full Energy", MoreMWEConfig.sendEnergyMessageWhenFullyCharged);
                MoreMWEConfig.instance().save();
                return;
            }
        }
        PlayerEnergyDisplay.instance().sendEnergyMessage();
    }

    @Override
    public List<String> onTabComplete(String[] args) {
        if (args.length <= 1) return getListOfStringsMatchingLastWord(args, "toggle", "help");
        return Collections.emptyList();
    }

    @Override
    protected void printHelp(String slashCommand) {
        printHelpBlock("Energy", new String[][]{
                { slashCommand, "Sends a chat message with your current energy" },
                { slashCommand + " toggle", "Toggles automatic full energy announcement" }
        });
    }
}
package me.shvaich.moremwe.commands;

import fr.alexdoru.mwe.api.MWEApi;
import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.features.PlayerEnergyDisplay;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.GuiUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.command.ICommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandMoreMWE extends MyAbstractCommand {

    @Override
    public String getCommandName() {
        return "moremwe";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("mmwe", "more_mwe");
    }

    @Override
    protected void onCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            final String subcommand = args[0];
            if (isHelpSubcommand(subcommand)) {
                printCommandHelp();
                return;
            }

            if (isEnergySubcommand(subcommand)) {
                if (args.length > 1) {
                    switch (StringUtil.toLowerCase(args[1])) {
                        case "t":
                        case "toggle": {
                            MoreMWEConfig.sendEnergyMessageWhenFullyCharged = !MoreMWEConfig.sendEnergyMessageWhenFullyCharged;
                            ChatUtil.addPropertyMessage("Automatically Announce Full Energy", MoreMWEConfig.sendEnergyMessageWhenFullyCharged);
                            MoreMWEConfig.instance().saveOnlyOneProperty("sendEnergyMessageWhenFullyCharged");
                            return;
                        }
                    }
                }
                PlayerEnergyDisplay.sendEnergyMessage();
                return;
            }
        }
        GuiUtil.openScreen(MoreMWEConfig.instance().getConfigGuiScreen());
    }

    @Override
    protected List<String> onTabComplete(ICommandSender sender, String[] args) {
        if (args.length <= 1) {
            return getListOfStringsMatchingLastWord(args, "energy", "help");
        }
        else if (args.length == 2 && isEnergySubcommand(args[0])) {
            return getListOfStringsMatchingLastWord(args, "toggle");
        }
        return Collections.emptyList();
    }

    @Override
    protected void printCommandHelp(String slashCommand) {
        final String bar = getHelpBarColor() + ChatUtil.bar();
        MWEApi.Chat.addChatMessage(bar);
        printCommandHelpBlock(MoreMWE.NAME + " Help", new String[][]{
                { slashCommand, "Opens the configuration GUI" },
                { slashCommand + " energy", "Sends a chat message with your current energy" },
                { slashCommand + " energy toggle", "Toggles automatic full energy announcement" }
        });
        MWEApi.Chat.addChatMessage("");
        printCommandHelpBlock("Hypixel Command Helpers", new String[][]{
                { "/play", "See /play help", "/play help" },
                { "/friend", "See /friend moremwe_help", "/friend moremwe_help" },
                { "/party", "See /party moremwe_help", "/party moremwe_help" }
        });
        MWEApi.Chat.addChatMessage(bar);
    }

    private static boolean isEnergySubcommand(String str) {
        return "e".equalsIgnoreCase(str) || "energy".equalsIgnoreCase(str);
    }
}

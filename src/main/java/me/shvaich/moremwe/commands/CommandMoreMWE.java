package me.shvaich.moremwe.commands;

import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

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
        }
        GuiUtil.openScreen(MoreMWEConfig.instance().getConfigGuiScreen());
    }

    @Override
    protected List<String> onTabComplete(ICommandSender sender, String[] args) {
        if (args.length <= 1) {
            return getListOfStringsMatchingLastWord(args, "help");
        }
        return Collections.emptyList();
    }

    @Override
    protected void printCommandHelp(String slashCommand) {
        final String bar = EnumChatFormatting.DARK_GRAY + ChatUtil.bar();
        ChatUtil.addChatMessage(bar);
        printCommandHelpBlock(MoreMWE.NAME + " Help", new String[][]{
                { slashCommand, "Opens the configuration GUI" }
        });
        ChatUtil.addChatMessage("");
        printCommandHelpBlock(MoreMWE.NAME + "'s Commands", new String[][]{
                { "/visiblebarriers", "See /visiblebarriers help", "/visiblebarriers help" }
        });
        ChatUtil.addChatMessage("");
        printCommandHelpBlock("Hypixel Command Helpers", new String[][]{
                { "/play", "See /play help", "/play help" },
                { "/friend", "See /friend moremwe_help", "/friend moremwe_help" },
                { "/party", "See /party moremwe_help", "/party moremwe_help" }
        });
        ChatUtil.addChatMessage(bar);
    }
}

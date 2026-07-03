package me.shvaich.moremwe.commands;

import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.features.PlayerEnergyDisplay;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.GuiUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

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
        return Arrays.asList("mmwe", "m_mwe", "more_mwe");
    }

    @Override
    protected void onCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            final String subcommand = StringUtil.toLowerCase(args[0]);
            if (isHelpSubcommand(subcommand)) {
                final String slashCommand = '/' + getCommandName();
                final IChatComponent msg = new ChatComponentText(getHelpBar() + "\n" + getHelpHeader(MoreMWE.NAME + " Help"))
                        .appendSibling(getHelpLineWithShortcut(slashCommand, "Opens the configuration GUI", "mmwe"))
                        .appendSibling(getHelpLineWithShortcut(slashCommand + " energy", "Sends a chat message with your current energy", "e"))
                        .appendSibling(getHelpLineWithShortcut(slashCommand + " energy toggle", "Toggles automatic full energy announcement", "t"))
                        .appendText("\n" + getHelpHeader("Hypixel Command Helpers"))
                        .appendSibling(getHelpLine("/play", "See /play help", "/play help"))
                        .appendSibling(getHelpLineWithShortcut("/friend", "See /friend mwu_help", "/friend mwu_help", "f"))
                        .appendSibling(getHelpLineWithShortcut("/party", "See /party mwu_help", "/party mwu_help", "p"))
                        .appendText("\n" + ChatUtil.centerLine(EnumChatFormatting.GRAY + "Some commands have a shortcut. Shortcuts are shown in parentheses after the description\n") + getHelpBar());

                ChatUtil.addChatMessage(msg);
                return;
            }

            if (isEnergySubcommand(subcommand)) {
                if (args.length > 1) {
                    switch (StringUtil.toLowerCase(args[1])) {
                        case "t":
                        case "toggle": {
                            MoreMWEConfig.sendEnergyMessageWhenFullyCharged = !MoreMWEConfig.sendEnergyMessageWhenFullyCharged;
                            ChatUtil.addChatMessage(ChatUtil.getModTag() + "Automatically Announce Full Energy: " + ChatUtil.getBooleanMsg(MoreMWEConfig.sendEnergyMessageWhenFullyCharged));
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
        else if (args.length == 2 && isEnergySubcommand(StringUtil.toLowerCase(args[0]))) {
            return getListOfStringsMatchingLastWord(args, "toggle");
        }
        return Collections.emptyList();
    }

    private static boolean isEnergySubcommand(String str) {
        return "e".equals(str) || "energy".equals(str);
    }
}

package me.shvaich.moremwe.commands;

import me.shvaich.moremwe.features.VisibleBarriers;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

public class CommandVisibleBarriers extends MyAbstractCommand {

    @Override
    public String getCommandName() {
        return "visiblebarriers";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("vb");
    }

    @Override
    protected void onCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            switch (StringUtil.toLowerCase(args[0])) {
                case "on":
                case "off": {
                    final boolean isCommandOn = "on".equalsIgnoreCase(args[0]);
                    if (VisibleBarriers.isActive() == isCommandOn) {
                        ChatUtil.addChatMessage(ChatUtil.getModTag() + EnumChatFormatting.GRAY + "Visible Barriers is already " + ChatUtil.getBooleanMsg(isCommandOn));
                    }
                    else {
                        VisibleBarriers.toggle();
                    }
                    return;
                }

                case "t":
                case "toggle": {
                    VisibleBarriers.toggle();
                    return;
                }

                case "s":
                case "status":
                case "show": {
                    VisibleBarriers.sendStatus();
                    return;
                }
            }
        }
        printCommandHelp();
    }

    @Override
    protected List<String> onTabComplete(ICommandSender sender, String[] args) {
        if (args.length > 1) return Collections.emptyList();
        return getListOfStringsMatchingLastWord(args, "on", "off", "toggle", "show");
    }

    @Override
    protected void printCommandHelp(String slashCommand) {
        slashCommand += " ";
        printCommandHelpBlock(EnumChatFormatting.GREEN, "Visible Barriers Help", new String[][]{
                { slashCommand + "on", "Enables visible barriers" },
                { slashCommand + "off", "Disables visible barriers" },
                { slashCommand + "toggle", "Toggles visible barriers" },
                { slashCommand + "show", "Displays visible barriers status (enabled/disabled)" }
        });
    }
}

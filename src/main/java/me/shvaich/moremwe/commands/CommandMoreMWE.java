package me.shvaich.moremwe.commands;

import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
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
                printModHelpMessage();
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

    public static void printModHelpMessage() {
        ChatUtil.addChatMessage(
                new ChatComponentText(ChatUtil.getModTag())
                        .appendSibling(ChatUtil.getURLMessage(EnumChatFormatting.YELLOW + "Click here", MoreMWE.MOD_URL))
                        .appendText(EnumChatFormatting.GRAY + " to visit the mod's GitHub page for a list of all features, more information, or help with any issues you encounter")
        );
    }
}

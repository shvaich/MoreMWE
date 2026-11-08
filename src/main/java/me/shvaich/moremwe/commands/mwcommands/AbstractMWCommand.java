package me.shvaich.moremwe.commands.mwcommands;

import fr.alexdoru.mwe.api.enums.MWClass;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.*;

abstract class AbstractMWCommand implements IMWCommand {

    @Override
    public List<String> getCommandAliases() { return Collections.emptyList(); }

    @Override
    public boolean isMWClassAllowed(MWClass mwClass) { return true; }

    protected final void printHelp() { printHelp("/" + getCommandName()); }

    protected void printHelp(String slashCommand) {}

    protected static List<String> getListOfStringsMatchingLastWord(String[] args, String... options) {
        return getListOfStringsMatchingLastWord(args, Arrays.asList(options));
    }

    protected static List<String> getListOfStringsMatchingLastWord(String[] args, Collection<String> options) {
        final String lastWord = args[args.length-1];
        final List<String> o = new ArrayList<>();
        for (final String option : options) {
            if (CommandBase.doesStringStartWith(lastWord, option)) {
                o.add(option);
            }
        }
        return o;
    }

    protected static boolean isHelpSubcommand(String subcommand) { return "h".equalsIgnoreCase(subcommand) || "help".equalsIgnoreCase(subcommand); }

    protected static void printHelpBlock(String header, String[][] commandLines) {
        final String bar = EnumChatFormatting.RED + ChatUtil.bar();
        final IChatComponent msg = new ChatComponentText(bar + "\n" + ChatUtil.centerLine(EnumChatFormatting.GOLD + header + " Help") + "\n");

        for (final String[] line : commandLines) {
            if (line.length < 2) continue;
            final String command = line[0];
            final String desc = line[1];
            final String commandToPutOnClick = line.length > 2 ? line[2] : command;
            msg.appendText("\n").appendSibling(ChatUtil.getCommandMessage(EnumChatFormatting.YELLOW + command + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.AQUA + desc, commandToPutOnClick));
        }

        msg.appendText("\n" + bar);
        ChatUtil.addChatMessage(msg);
    }

    protected static ChatUtil.ChatChannel getChatChannelFromArgument(String arg) {
        if (StringUtil.isEmpty(arg)) return null;
        arg = StringUtil.toLowerCase(arg);
        if ("d".equals(arg) || "default".equals(arg)) return ChatUtil.ChatChannel.DEFAULT;
        for (final ChatUtil.ChatChannel chatChannel : new ChatUtil.ChatChannel[]{ ChatUtil.ChatChannel.ALL, ChatUtil.ChatChannel.PARTY, ChatUtil.ChatChannel.GUILD }) {
            final String name = StringUtil.toLowerCase(chatChannel.name());
            if (name.equals(arg)
                    || String.valueOf(name.charAt(0)).equals(arg)
                    || (name.charAt(0) + "c").equals(arg)
                    || (name.charAt(0) + "chat").equals(arg))
            {
                return chatChannel;
            }
        }
        return null;
    }

    protected static List<String> getChatChannelAutocomplete() {
        final List<String> l = new ArrayList<>();
        for (ChatUtil.ChatChannel value : ChatUtil.ChatChannel.values()) {
            l.add(StringUtil.toLowerCase(value.name()));
        }
        return l;
    }
}
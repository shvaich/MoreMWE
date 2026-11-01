package me.shvaich.moremwe.commands.mwcommands;

import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.*;

public abstract class AbstractMWCommand {

//    private final EnumSet<MWClass> allowedClasses;
//
//    protected AbstractMWCommand() {
//        this.allowedClasses = null;
//    }
//
//    protected AbstractMWCommand(MWClass... allowedClasses) {
//        this.allowedClasses = EnumSet.noneOf(MWClass.class);
//        Collections.addAll(this.allowedClasses, allowedClasses);
//    }

    public abstract String getCommandName();

    public List<String> getCommandAliases() { return Collections.emptyList(); }

    public abstract void onCommand(String[] args);

    public abstract List<String> onTabComplete(String[] args);

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
        final ChatUtil.ChatChannel[] values = ChatUtil.ChatChannel.values();
        final List<String> l = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            l.set(i, StringUtil.toLowerCase(values[i].name()));
        }
        return l;
    }
}
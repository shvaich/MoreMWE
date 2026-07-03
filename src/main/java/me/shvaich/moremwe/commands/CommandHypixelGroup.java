package me.shvaich.moremwe.commands;

import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.DelayedTask;
import me.shvaich.moremwe.utils.PlayerUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.*;

public class CommandHypixelGroup extends MyAbstractCommand {

    private final String name;

    public CommandHypixelGroup(String name) {
        this.name = name;
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList(String.valueOf(name.charAt(0)));
    }

    @Override
    protected void onCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            final String subcommand = StringUtil.toLowerCase(args[0]);
            if (isHelpSubcommand(subcommand)) {
                sendCommand("help");
                new DelayedTask(this::printHelpMessage, 5);
                return;
            }

            if ("mwu_h".equals(subcommand) || "mwu_help".equals(subcommand)) {
                printHelpMessage();
                return;
            }

            if (isBulkSubcommand(subcommand)) {
                if (args.length == 1) {
                    ChatUtil.addErrorMessage("Usage: /" + name + " bulk <player> [<player>...]");
                    return;
                }
                final int delayInc = 25;
                int count = 0;
                final Set<String> addedNames = new HashSet<>();
                final Set<String> invalidNames = new LinkedHashSet<>();
                final Set<String> duplicateNames = new LinkedHashSet<>();
                for (int i = 1; i < args.length; i++) {
                    final String arg = args[i];
                    if (!PlayerUtil.isValidUsername(arg)) {
                        if (!arg.isEmpty()) invalidNames.add(arg);
                        continue;
                    }
                    if (!addedNames.add(arg)) {
                        duplicateNames.add(arg);
                        continue;
                    }
                    new DelayedTask(() -> sendCommand(arg), (count++) * delayInc);
                }
                final String errorMsg = StringUtil.joinIgnoreEmpty("\n",
                        getErrorMsg(invalidNames, "invalid"),
                        getErrorMsg(duplicateNames, "duplicate")
                );
                if (!errorMsg.isEmpty()) {
                    if (count > 0) {
                        count--;
                        new DelayedTask(() -> ChatUtil.addChatMessage(errorMsg), count * delayInc);
                    }
                    else ChatUtil.addChatMessage(errorMsg);
                }
                return;
            }
        }
        sendCommand(args);
    }

    @Override
    protected List<String> onTabComplete(ICommandSender sender, String[] args) {
        if (args.length <= 1 || isBulkSubcommand(StringUtil.toLowerCase(args[0]))) {
            return getListOfStringsMatchingLastWord(args, PlayerUtil.getTabPlayerNames());
        }
        return Collections.emptyList();
    }

    private void printHelpMessage() {
        final String slashCommand = '/' + name;
        final IChatComponent msg = new ChatComponentText(getHelpBar() + "\n" + getHelpHeader(MoreMWE.NAME + " " + StringUtil.capitalize(name) + " Help"))
                .appendSibling(getHelpLineWithShortcut(slashCommand + " bulk <player> [<player>...]", "Repeats the command for each specified player", slashCommand + " bulk", "b"))
                .appendText(getHelpBar());

        ChatUtil.addChatMessage(msg);
    }

    private static boolean isBulkSubcommand(String s) {
        return "b".equals(s) || "bulk".equals(s);
    }

    private static String getErrorMsg(Collection<String> names, String type) {
        if (names.isEmpty()) return "";
        return ChatUtil.getModTag() + getSkippedMessage(names, type + " player names");
    }
}

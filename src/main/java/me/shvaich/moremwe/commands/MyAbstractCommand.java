package me.shvaich.moremwe.commands;

import fr.alexdoru.mwe.api.MWECommandBase;
import me.shvaich.moremwe.utils.ChatUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collection;
import java.util.List;

abstract class MyAbstractCommand extends MWECommandBase {

    /**
     * User text is right-trimmed, every space character adds an empty string to args
     * and every other character is added to the current last string in args.
     * For example:
     * <blockquote><pre>
     *     "/command " -> []
     *     "/command" -> []
     *     "/command  Hello " -> ["", "Hello"]
     * </pre></blockquote>
     */
    protected abstract void onCommand(ICommandSender sender, String[] args);

    /**
     * Only called when there is a space character after "/command".
     * Every space character from the user text adds an empty string to args
     * and every other character is added to the current last string in args.
     * For example:
     * <blockquote><pre>
     *    "/command " -> [""]
     *    "/command" -> (NOT CALLED)
     *    "/command  Hello " -> ["", "Hello", ""]
     * </pre></blockquote>
     */
    protected abstract List<String> onTabComplete(ICommandSender sender, String[] args);

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        this.onCommand(sender, args);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return this.onTabComplete(sender, args);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    protected void printCommandHelp() {
        printCommandHelp('/' + getCommandName());
    }

    protected void printCommandHelp(String slashCommand) {}

    protected final void sendCommand(String arg) {
        sendCommand(new String[]{arg});
    }

    protected final void addUsageErrorMessage(String usage) {
        ChatUtil.addErrorMessage("Usage: /" + getCommandName() + " " + usage);
    }

    protected static String getSkippedMessage(Collection<String> skippedNames, String desc) {
        return EnumChatFormatting.RED + "Skipped (" + skippedNames.size() + ") " + desc + ": " + String.join(", ", skippedNames);
    }

    protected static EnumChatFormatting getHelpBarColor() { return EnumChatFormatting.GREEN; }
}

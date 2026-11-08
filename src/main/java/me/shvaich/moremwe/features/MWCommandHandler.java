package me.shvaich.moremwe.features;

import fr.alexdoru.mwe.api.MWEApi;
import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.commands.mwcommands.IMWCommand;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.utils.PlayerUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;

import java.util.*;

public final class MWCommandHandler {
    private MWCommandHandler() {}

    public static final MWCommandHandler INSTANCE = new MWCommandHandler();

    private final Map<String, IMWCommand> commandMap = new HashMap<>();

    public void registerCommand(IMWCommand command) {
        if (commandMap.containsKey(command.getCommandName())) {
            MoreMWE.LOGGER.error("The name {} is already used by a mw-command", command.getCommandName());
            return;
        }

        commandMap.put(command.getCommandName(), command);
        final List<String> aliases = command.getCommandAliases();
        if (aliases != null) {
            for (final String alias : aliases) {
                final IMWCommand c = commandMap.get(alias);
                if (c == null || !c.getCommandName().equals(alias)) {
                    commandMap.put(alias, command);
                }
            }
        }
    }

    public boolean executeCommand(String message) {
        if (!isEnabled() || message == null || (message = message.trim()).length() < 2 || message.charAt(0) != '/') return false;
        final String[] args = message.substring(1).split(" ");
        final String commandKey = args[0];
        final IMWCommand command = commandMap.get(commandKey);
        if (isCommandEnabled(command)) {
            command.onCommand(dropFirstString(args));
            return true;
        }
        return false;
    }

    public List<String> getTabCompleteOptions(String leftOfCursor, String full) {
        if (!isEnabled() || StringUtil.isEmpty(leftOfCursor) || leftOfCursor.charAt(0) != '/') {
            return null;
        }
        final String[] args = leftOfCursor.substring(1).split(" ", -1);

        final String s = args[0];
        if (args.length == 1) {
            final List<String> l = new ArrayList<>();
            for (final String k : commandMap.keySet()) {
                if (CommandBase.doesStringStartWith(s, k)) {
                    l.add("/" + k);
                }
            }
            return l;
        }
        else {
            final IMWCommand command = commandMap.get(s);
            if (isCommandEnabled(command)) {
                return command.onTabComplete(dropFirstString(args));
            }
        }
        return null;
    }

    private static boolean isEnabled() {
        return MoreMWEConfig.enableMWCommands && MWEApi.Scoreboard.getScoreboardParser().isInMwGame();
    }

    private static boolean isCommandEnabled(IMWCommand mwCommand) {
        return mwCommand != null && mwCommand.isMWClassAllowed(PlayerUtil.getPlayerMWClass(Minecraft.getMinecraft().thePlayer));
    }

    private static String[] dropFirstString(String[] args)
    {
        final int l = args.length - 1;
        String[] t = new String[l];
        System.arraycopy(args, 1, t, 0, l);
        return t;
    }
}

package me.shvaich.moremwe.asm.hooks;

import fr.alexdoru.mwe.api.MWEApi;
import me.shvaich.moremwe.features.MWMessages;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

public class GuiChatHook_MWCommands {

    private static List<String> tabCompleteOptions;

    public static boolean isHandledInput(GuiScreen guiScreen, String msg) {
        if (msg == null || !MWEApi.Scoreboard.getScoreboardParser().isInMwGame()) return false;
        msg = msg.trim();
        if (msg.length() < 2 || msg.charAt(0) != '/') return false;
        final String[] args = msg.substring(1).split(" ");
        final String command = args[0];
        if (isHelpCommand(command)) {
            if (args.length == 1) {
                guiScreen.mc.ingameGUI.getChatGUI().addToSentMessages("/" + command);
                MWMessages.sendHelpMessage(guiScreen.mc);
                return true;
            }
            final String param = StringUtil.toLowerCase(args[1]);
            if ("r".equals(param) || "real".equals(param) || "f".equals(param) || "force".equals(param)) {
                final StringBuilder builder = new StringBuilder("/" + args[0]);
                for (int i = 2; i < args.length; i++) {
                    builder.append(' ').append(args[i]);
                }
                guiScreen.sendChatMessage(builder.toString());
                return true;
            }
        }
        return false;
    }

    public static void autoComplete(String leftOfCursor, String full) {
        tabCompleteOptions = null;
        if (StringUtil.isEmpty(leftOfCursor) || !MWEApi.Scoreboard.getScoreboardParser().isInMwGame()) return;
        if (leftOfCursor.charAt(0) != '/') return;
        final String[] args = leftOfCursor.substring(1).split(" ", -1);
        List<String> options = null;
        boolean isCommandAutocomplete = false;
        if (args.length == 1) {
            isCommandAutocomplete = true;
            options = getListOfStringsMatchingLastWord(args, "help");
        }
        else if (args.length == 2 && isHelpCommand(args[0])) {
            options = getListOfStringsMatchingLastWord(args, "force");
        }
        if (options != null && !options.isEmpty()) {
            final String prefix = isCommandAutocomplete ? "/" : "";
            for (int i = 0; i < options.size(); i++) {
                options.set(i, EnumChatFormatting.GRAY + prefix + options.get(i) + EnumChatFormatting.RESET);
            }
            tabCompleteOptions = options;
        }
    }

    public static String[] getLatestAutoComplete(String[] arr) {
        if (tabCompleteOptions == null) {
            return arr;
        }
        final Set<String> options = new LinkedHashSet<>(tabCompleteOptions);
        options.addAll(Arrays.asList(arr));
        return options.toArray(new String[0]);
    }

    private static List<String> getListOfStringsMatchingLastWord(String[] args, String... options) {
        if (args.length == 0 || options.length == 0) return Collections.emptyList();
        final String lastWord = args[args.length - 1];
        final List<String> list = new ArrayList<>(options.length);
        for (final String option : options) {
            if (option.regionMatches(true, 0, lastWord, 0, lastWord.length())) {
                list.add(option);
            }
        }
        return list;
    }

    private static boolean isHelpCommand(String str) {
        return "h".equalsIgnoreCase(str) || "help".equalsIgnoreCase(str);
    }
}
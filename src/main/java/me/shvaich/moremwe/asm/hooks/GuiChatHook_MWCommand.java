package me.shvaich.moremwe.asm.hooks;

import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.features.MWCommandHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class GuiChatHook_MWCommand {

    private static List<String> tabCompleteOptions;

    public static boolean isHandledInput(GuiScreen guiScreen, String msg) {
        if (MWCommandHandler.INSTANCE.executeCommand(msg)) {
            guiScreen.mc.ingameGUI.getChatGUI().addToSentMessages(msg);
            return true;
        }
        return false;
    }

    public static void autoComplete(String leftOfCursor, String full) {
        final List<String> o = MWCommandHandler.INSTANCE.getTabCompleteOptions(leftOfCursor, full);
        if (o == null || o.isEmpty()) {
            tabCompleteOptions = null;
        }
        else {
            o.replaceAll(s -> MoreMWEConfig.mwCommandsTabColor + s + EnumChatFormatting.RESET);
            tabCompleteOptions = o;
        }
    }

    public static String[] getLatestAutoComplete(String[] arr) {
        if (tabCompleteOptions == null || tabCompleteOptions.isEmpty()) {
            return arr;
        }
        if (arr == null || arr.length == 0) {
            return tabCompleteOptions.toArray(new String[0]);
        }
        final String[] result = new String[tabCompleteOptions.size() + arr.length];
        tabCompleteOptions.toArray(result);
        System.arraycopy(arr, 0, result, tabCompleteOptions.size(), arr.length);
        return result;
    }
}

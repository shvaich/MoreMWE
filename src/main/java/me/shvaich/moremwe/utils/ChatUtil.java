package me.shvaich.moremwe.utils;

import fr.alexdoru.mwe.api.MWEApi;
import me.shvaich.moremwe.MoreMWE;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import static net.minecraft.util.EnumChatFormatting.*;

public final class ChatUtil {

    private ChatUtil() {}

    public static String getModTag() {
        return DARK_GRAY + "[" + YELLOW + MoreMWE.NAME + DARK_GRAY + "] " + RESET;
    }

    public static String getBooleanMsg(boolean bool) {
        return bool ? GREEN + "Enabled" : RED + "Disabled";
    }

    public static void addChatMessage(String msg) {
        addChatMessage(new ChatComponentText(msg));
    }

    /** Delegates to {@link MWEApi.Chat#addChatMessage(IChatComponent)} */
    public static void addChatMessage(IChatComponent msg) {
        MWEApi.Chat.addChatMessage(msg);
    }

    public static void addErrorMessage(String msg) {
        addErrorMessage(msg, true);
    }

    public static void addErrorMessage(String msg, boolean withModTag) {
        addChatMessage((withModTag ? getModTag() : "") + RED + msg);
    }

    public static void addDebugMessage(String msg) {
        addChatMessage(AQUA + "[Debug]: " + RESET + msg);
    }

    public static void addPropertyMessage(String name, boolean value) {
        addChatMessage(getModTag() + name + DARK_GRAY + ": " + getBooleanMsg(value));
    }

    public static String bar() {
        return STRIKETHROUGH + repeatToChatWidth('-');
    }

    public static String centerLine(String msg) {
        return getSpaceToCenter(msg) + msg;
    }

    public static String getSpaceToCenter(String msg) {
        final Minecraft mc = Minecraft.getMinecraft();
        final int chatWidth = mc.ingameGUI.getChatGUI().getChatWidth();
        final int msgWidth = mc.fontRendererObj.getStringWidth(msg);
        return msgWidth >= chatWidth ? "" : getSpaceOfLength((chatWidth - msgWidth) / 2);
    }

    public static String getSpaceOfLength(int len) {
        final char space = ' ';
        final int n = len / Minecraft.getMinecraft().fontRendererObj.getCharWidth(space);
        return n < 1 ? "" : StringUtil.repeat(space, n);
    }

    public static String repeatToChatWidth(char ch) {
        final Minecraft mc = Minecraft.getMinecraft();
        return StringUtil.repeat(ch, mc.ingameGUI.getChatGUI().getChatWidth() / mc.fontRendererObj.getCharWidth(ch));
    }


    public static IChatComponent getURLMessage(String msg, String url) {
        return getActionMessage(msg, ClickEvent.Action.OPEN_URL, url, YELLOW + url);
    }

    public static IChatComponent getCommandMessage(String msg, String command) {
        return getActionMessage(msg, ClickEvent.Action.SUGGEST_COMMAND, command, GRAY + "Click to put the command in chat.");
    }

    private static IChatComponent getActionMessage(String msg, ClickEvent.Action action, String actionValue, String hoverMsg) {
        return new ChatComponentText(msg)
                .setChatStyle(new ChatStyle()
                        .setChatClickEvent(new ClickEvent(action, actionValue))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(hoverMsg)))
                );
    }


//    /** A static version of {@link net.minecraft.client.gui.GuiScreen#sendChatMessage(String, boolean)} with a little bit more control */
//    public static void sendChatMessage(String msg, boolean addToChat, boolean tryAsClientCommand) {
//        if (msg == null) return;
//        final Minecraft mc = Minecraft.getMinecraft();
//        if (mc == null || mc.thePlayer == null) {
//            addErrorMessage("Failed to send chat message : " + msg);
//            return;
//        }
//        if (addToChat) {
//            mc.ingameGUI.getChatGUI().addToSentMessages(msg);
//        }
//        if (tryAsClientCommand && ClientCommandHandler.instance.executeCommand(mc.thePlayer, msg) != 0) return;
//
//        mc.thePlayer.sendChatMessage(msg);
//    }
//
//    public static void sendChatMessage(String msg) {
//        sendChatMessage(msg, false, false);
//    }
}

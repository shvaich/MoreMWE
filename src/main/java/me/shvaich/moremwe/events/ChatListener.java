package me.shvaich.moremwe.events;

import fr.alexdoru.mwe.api.MWEApi;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public final class ChatListener {

    private static ChatListener instance;

    private final List<IChatListener> listeners = new ArrayList<>();

    private ChatListener() {}

    public static ChatListener instance() {
        ChatListener i = instance;
        if (i == null) instance = i = new ChatListener();
        return i;
    }

    public void registerListener(IChatListener listener) {
        if (listeners.contains(listener)) {
            // throw new MoreMWEException().sendDebug("Chat Listener already registered");
            return;
        }
        listeners.add(listener);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatMessage(ClientChatReceivedEvent e) {
        final String formattedMsg = e.message.getFormattedText();
        final String msg = StringUtil.removeFormattingCodes(e.message.getUnformattedText());
        final boolean isInMegaWalls = MWEApi.Scoreboard.getScoreboardParser().isInMwGame();
        for (final IChatListener listener : listeners)
            if (listener.processMessage(e, formattedMsg, msg, isInMegaWalls))
                return;
    }

    public interface IChatListener {
        /** @return {@code true} if this listener handles the message */
        boolean processMessage(ClientChatReceivedEvent e, String formattedMsg, String msg, boolean isInMegaWalls);
    }
}

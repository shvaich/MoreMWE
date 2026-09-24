package me.shvaich.moremwe.events;

import fr.alexdoru.mwe.api.MWEApi;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatListener {

    public static final ChatListener INSTANCE = new ChatListener();
    private final List<IChatListener> listeners = new ArrayList<>();

    private ChatListener() {}

    public void registerListener(IChatListener listener) {
        if (listeners.contains(listener)) throw new IllegalArgumentException();
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

package me.shvaich.moremwe.features;

import me.shvaich.moremwe.utils.ChatUtil;
import net.minecraft.client.Minecraft;

public final class MWMessages {
    private MWMessages() {}

    public static void sendHelpMessage(Minecraft mc) {
        if (mc.thePlayer == null) {
            ChatUtil.addErrorMessage("Failed to send help message (rip)");
            return;
        }

        mc.thePlayer.sendChatMessage("Help me!");
    }
}

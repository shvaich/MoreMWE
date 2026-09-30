package me.shvaich.moremwe.features;

import me.shvaich.moremwe.utils.ChatUtil;
import net.minecraft.client.Minecraft;

public final class VisibleBarriers {

    private VisibleBarriers() {}

    private static boolean isActive = false;

    public static boolean isActive() { return isActive; }

    public static void toggle() {
        isActive = !isActive;
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
        sendStatus();
    }

    public static void sendStatus() {
        ChatUtil.addPropertyMessage("Visible Barriers", isActive);
    }
}

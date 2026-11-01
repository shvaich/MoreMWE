package me.shvaich.moremwe.asm.hooks;

import me.shvaich.moremwe.features.GuiScreenAllowsPlayerKeyInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;

public class GuiScreenAllowsPlayerKeyInputHook {

    public static boolean shouldConsumeKeyInput(boolean original, GuiScreen screen) {
        return !isScreenThatAllowsPlayerKeyInput(screen) && original;
    }

    public static boolean shouldntUnpressAllKeys(Minecraft mc) {
        return isScreenThatAllowsPlayerKeyInput(mc.currentScreen);
    }

    public static void unPressAllKeysIfNeeded(GuiScreen previousScreen, GuiScreen newScreen) {
        if (isScreenThatAllowsPlayerKeyInput(previousScreen) && !isScreenThatAllowsPlayerKeyInput(newScreen)) {
            KeyBinding.unPressAllKeys();
        }
    }

    private static boolean isScreenThatAllowsPlayerKeyInput(GuiScreen screen) {
        return screen instanceof GuiScreenAllowsPlayerKeyInput && ((GuiScreenAllowsPlayerKeyInput) screen).allowsPlayerKeyInput();
    }
}

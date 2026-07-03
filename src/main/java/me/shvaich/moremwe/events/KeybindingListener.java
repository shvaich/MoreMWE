package me.shvaich.moremwe.events;

import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.features.PlayerEnergyDisplay;
import me.shvaich.moremwe.utils.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

public class KeybindingListener {

    private static final KeyBinding showCompassHUDKey = create("Compass HUD Control");
    private static final KeyBinding blockGuardKeyBinding = create("Toggle " + MoreMWEConfig.BLOCK_GUARD);
    private static final KeyBinding shovelGuardKeyBinding = create("Toggle " + MoreMWEConfig.SHOVEL_GUARD);
    private static final KeyBinding sendPlayerEnergyMessageKey = create("Send Energy Status");

    public KeybindingListener() {
        ClientRegistry.registerKeyBinding(showCompassHUDKey);
        ClientRegistry.registerKeyBinding(blockGuardKeyBinding);
        ClientRegistry.registerKeyBinding(shovelGuardKeyBinding);
        ClientRegistry.registerKeyBinding(sendPlayerEnergyMessageKey);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent e) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (sendPlayerEnergyMessageKey.isPressed()) {
            PlayerEnergyDisplay.sendEnergyMessage();
            return;
        }

        if (showCompassHUDKey.isPressed() && MoreMWEConfig.compassKeybindMode == 0) {
            final boolean newValue = !MoreMWEConfig.compassHUDPosition.isEnabled();
            MoreMWEConfig.compassHUDPosition.setEnabled(newValue);
            ChatUtil.addChatMessage(ChatUtil.getModTag() + "Compass HUD: " + ChatUtil.getBooleanMsg(newValue));
            return;
        }

        final boolean value;
        final String fieldName;
        final String guard;

        if (blockGuardKeyBinding.isPressed()) {
            value = MoreMWEConfig.areBlocksGuarded = !MoreMWEConfig.areBlocksGuarded;
            fieldName = "areBlocksGuarded";
            guard = MoreMWEConfig.BLOCK_GUARD;
        }
        else if (shovelGuardKeyBinding.isPressed()) {
            value = MoreMWEConfig.isShovelGuarded = !MoreMWEConfig.isShovelGuarded;
            fieldName = "isShovelGuarded";
            guard = MoreMWEConfig.SHOVEL_GUARD;
        }
        else return;

        ChatUtil.addChatMessage(ChatUtil.getModTag() + guard + ": " + ChatUtil.getBooleanMsg(value));
        MoreMWEConfig.instance().saveOnlyOneProperty(fieldName);
    }

    public static boolean hideCompassHUDFromKey() {
        if (MoreMWEConfig.compassKeybindMode != 1) return false;
        return showCompassHUDKey.getKeyCode() != Keyboard.KEY_NONE && !showCompassHUDKey.isKeyDown();
    }

    private static KeyBinding create(String desc, int defaultKeyCode, String category) {
        return new KeyBinding(desc, defaultKeyCode, category);
    }

    private static KeyBinding create(String desc, int defaultKeyCode) {
        return create(desc, defaultKeyCode, MoreMWE.NAME);
    }

    private static KeyBinding create(String desc) {
        return create(desc, Keyboard.KEY_NONE, MoreMWE.NAME);
    }
}

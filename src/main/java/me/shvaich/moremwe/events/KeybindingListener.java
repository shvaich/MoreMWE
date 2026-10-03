package me.shvaich.moremwe.events;

import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.features.PlayerEnergyDisplay;
import me.shvaich.moremwe.features.VisibleBarriers;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.MoreMWEException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

public final class KeybindingListener {

    private static KeybindingListener instance;

    private final KeyBinding showCompassHUDKey = create("Compass HUD Control");
    private final KeyBinding blockGuardKeyBinding = create("Toggle " + MoreMWEConfig.BLOCK_GUARD);
    private final KeyBinding shovelGuardKeyBinding = create("Toggle " + MoreMWEConfig.SHOVEL_GUARD);
    private final KeyBinding sendPlayerEnergyMessageKey = create("Send Energy Status");
    private final KeyBinding visibleBarriersKey = create("Toggle Visible Barriers");

    public KeybindingListener() {
        if (instance != null) {
            throw new MoreMWEException().sendDebug("Attempted to create another KeybindingListener instance. NOT ALLOWED!");
        }
        instance = this;
        registerKeybinds(
                showCompassHUDKey,
                blockGuardKeyBinding,
                shovelGuardKeyBinding,
                sendPlayerEnergyMessageKey,
                visibleBarriersKey
        );
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent e) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (sendPlayerEnergyMessageKey.isPressed()) {
            PlayerEnergyDisplay.sendEnergyMessage();
            return;
        }

        if (visibleBarriersKey.isPressed()) {
            VisibleBarriers.toggle();
            return;
        }

        if (showCompassHUDKey.isPressed() && MoreMWEConfig.compassKeybindMode == 0) {
            MoreMWEConfig.compassHUDPosition.setEnabled(!MoreMWEConfig.compassHUDPosition.isEnabled());
            ChatUtil.addPropertyMessage("Compass HUD", MoreMWEConfig.compassHUDPosition.isEnabled());
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

        ChatUtil.addPropertyMessage(guard, value);
        MoreMWEConfig.instance().saveOnlyOneProperty(fieldName);
    }

    public static boolean hideCompassHUDFromKey() {
        if (MoreMWEConfig.compassKeybindMode != 1) return false;
        return instance.showCompassHUDKey.getKeyCode() != Keyboard.KEY_NONE && !instance.showCompassHUDKey.isKeyDown();
    }

    private static KeyBinding create(String desc, String name) {
        return new KeyBinding(desc, Keyboard.KEY_NONE, name);
    }

    private static KeyBinding create(String desc) {
        return create(desc, MoreMWE.NAME);
    }

    private static void registerKeybinds(KeyBinding... keybinds) {
        for (final KeyBinding k : keybinds) {
            ClientRegistry.registerKeyBinding(k);
        }
    }
}

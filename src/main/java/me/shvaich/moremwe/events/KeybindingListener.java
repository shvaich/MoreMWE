package me.shvaich.moremwe.events;

import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.features.MWMapLocationHandler;
import me.shvaich.moremwe.features.PingWheelHandler;
import me.shvaich.moremwe.features.PlayerEnergyDisplay;
import me.shvaich.moremwe.features.VisibleBarriers;
import me.shvaich.moremwe.utils.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;

public final class KeybindingListener {

    private static KeybindingListener instance;

    private final MyKeyBinding[] allKeybinds;

    private final MyKeyBinding compassHUDKey;
    private final MyKeyBinding pingKey;

    private KeybindingListener() {
        this.allKeybinds = new MyKeyBinding[]{
                this.compassHUDKey = new MyKeyBinding("Compass HUD Control").withPress(mc -> {
                    if (MoreMWEConfig.compassKeybindMode == 0) {
                        MoreMWEConfig.compassHUDPosition.setEnabled(!MoreMWEConfig.compassHUDPosition.isEnabled());
                        updateProperty("Compass HUD", MoreMWEConfig.compassHUDPosition.isEnabled());
                    }
                }),

                new MyKeyBinding("Compass HUD Toggle Different Teams").withPress(mc -> {
                    updateProperty("Prefer Different Teams", MoreMWEConfig.prioritizeUniqueTeams = !MoreMWEConfig.prioritizeUniqueTeams);
                }),

                new MyKeyBinding("Toggle " + MoreMWEConfig.BLOCK_GUARD).withPress(mc -> {
                    updateProperty(MoreMWEConfig.BLOCK_GUARD, MoreMWEConfig.areBlocksGuarded = !MoreMWEConfig.areBlocksGuarded);
                }),

                new MyKeyBinding("Toggle " + MoreMWEConfig.SHOVEL_GUARD).withPress(mc -> {
                    updateProperty(MoreMWEConfig.SHOVEL_GUARD, MoreMWEConfig.isShovelGuarded = !MoreMWEConfig.isShovelGuarded);
                }),

                new MyKeyBinding("Send Energy Status").withPress(mc -> PlayerEnergyDisplay.instance().sendEnergyMessage()),

                new MyKeyBinding("Toggle Visible Barriers").withPress(mc -> VisibleBarriers.toggle()),

                new MyKeyBinding("Send Help Message").withPress(mc -> MWMapLocationHandler.instance().sendMcPlayerMessage(mc, MoreMWEConfig.mcPlayerBaseLocationMessageChatChannel, MWMapLocationHandler.McPlayerMessage.HELP)),

                new MyKeyBinding("Send Your Location Message").withPress(mc -> MWMapLocationHandler.instance().sendMcPlayerMessage(mc, MoreMWEConfig.mcPlayerBaseLocationMessageChatChannel, MWMapLocationHandler.McPlayerMessage.STATUS)),

                this.pingKey = new MyKeyBinding("Ping").withPress(mc -> PingWheelHandler.instance().schedulePress())
        };
    }

    public static KeybindingListener instance() {
        KeybindingListener i = instance;
        if (i == null) instance = i = new KeybindingListener();
        return i;
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent e) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null || mc.thePlayer == null) return;

        for (final MyKeyBinding keyBinding : allKeybinds) {
            keyBinding.onInput(mc);
        }
    }

    public boolean isCompassHUDKeyAssignedAndNotHeldDown() {
        return compassHUDKey.keyBinding.getKeyCode() != Keyboard.KEY_NONE && !compassHUDKey.keyBinding.isKeyDown();
    }

    public boolean isPingKeyDown() { return pingKey.keyBinding.isKeyDown(); }

    private static void updateProperty(String name, boolean value) {
        ChatUtil.addPropertyMessage(name, value);
        MoreMWEConfig.instance().save();
    }

    private static final class MyKeyBinding {
        public final KeyBinding keyBinding;
        private Consumer<Minecraft> onPress;
        private boolean isPressed;

        public MyKeyBinding(String desc, String category) {
            this.keyBinding = new KeyBinding(desc, Keyboard.KEY_NONE, category);
            ClientRegistry.registerKeyBinding(keyBinding);
        }

        public MyKeyBinding(String desc) { this(desc, MoreMWE.NAME); }

        public MyKeyBinding withPress(Consumer<Minecraft> onPress) { this.onPress = onPress; return this; }

        public void onInput(Minecraft mc) {
            final boolean isDown = keyBinding.isKeyDown();
            if (isDown && !isPressed) {
                if (onPress != null) onPress.accept(mc);
            }
            isPressed = isDown;
        }
    }
}

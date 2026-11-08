package me.shvaich.moremwe.features;

import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.events.KeybindingListener;
import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public final class PingWheelHandler {

    private enum State { IDLE, PRESSED, WHEEL, WAIT_RELEASE }

    private static final int HOLD_TICKS = 4;

    private static PingWheelHandler instance;

    private PingWheel currentPingWheel;
    private State currentState = State.IDLE;
    private int heldTicks;
    private boolean pressPending;

    private PingWheelHandler() {}

    public static PingWheelHandler instance() {
        PingWheelHandler i = instance;
        if (i == null) instance = i = new PingWheelHandler();
        return i;
    }

    public boolean schedulePress() {
        if (currentState == State.IDLE) {
            this.pressPending = true;
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent e) {
        if (e.button == 2 && e.buttonstate && MoreMWEConfig.pingOnMouseMiddleClick && isEnabled(Minecraft.getMinecraft())) {
            if (schedulePress()) {
                e.setCanceled(true);
            }
        }
    }

    private boolean isEnabled(Minecraft mc) {
        return mc.thePlayer != null
                && mc.theWorld != null
                && (mc.currentScreen == null || mc.currentScreen == currentPingWheel)
                && MWMapLocationHandler.instance().isActive();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        final Minecraft mc = Minecraft.getMinecraft();
        if (!isEnabled(mc))
        {
            if (currentPingWheel != null) {
                if (mc.currentScreen == currentPingWheel) {
                    mc.displayGuiScreen(null);
                }
                else currentPingWheel = null;
            }
            this.currentState = State.WAIT_RELEASE;
            this.pressPending = false;
            return;
        }

        final boolean isDown = isHeld();
        switch (currentState) {
            case WAIT_RELEASE: if (!isDown) { currentState = State.IDLE; } break;

            case IDLE: {
                if (isDown) { currentState = State.PRESSED; heldTicks = 0; }
                else if (pressPending) {
                    sendPingMessage(mc, MoreMWEConfig.defaultPingMessageType);
                }
                break;
            }

            case PRESSED: {
                if (!isDown) {
                    sendPingMessage(mc, MoreMWEConfig.defaultPingMessageType);
                    currentState = State.IDLE;
                }
                else if (++heldTicks >= HOLD_TICKS) {
                    mc.displayGuiScreen(currentPingWheel = new PingWheel());
                    currentState = State.WHEEL;
                }
                break;
            }

            case WHEEL: {
                if (currentPingWheel != mc.currentScreen) {
                    currentPingWheel = null;
                    currentState = isDown ? State.WAIT_RELEASE : State.IDLE;
                }
                else if (!isDown) {
                    mc.displayGuiScreen(null);
                }
                break;
            }
        }
        this.pressPending = false;
    }

    private static void sendPingMessage(Minecraft mc, MWMapLocationHandler.PingMessage pingMessage) {
        MWMapLocationHandler.instance().sendPingMessage(mc, MoreMWEConfig.pingBaseLocationMessageChatChannel, pingMessage);
    }

    private static boolean isHeld() {
        return KeybindingListener.instance().isPingKeyDown() || (MoreMWEConfig.pingOnMouseMiddleClick && Mouse.isButtonDown(2));
    }

    private final class PingWheel extends GuiScreen implements GuiScreenAllowsPlayerKeyInput {
        private final MWMapLocationHandler.PingMessage[] pingMessages;
        private int selectedIndex = -1;

        PingWheel() {
            // clockwise order
            this.pingMessages = new MWMapLocationHandler.PingMessage[]{
                    MWMapLocationHandler.PingMessage.STATUS,
                    MWMapLocationHandler.PingMessage.ENEMY,
                    MWMapLocationHandler.PingMessage.GO_TO,
                    MWMapLocationHandler.PingMessage.PUSH
            };
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            final int centerX = width / 2, centerY = height / 2;
            final int len = pingMessages.length;
            final double start = -Math.PI / 2;
            final double step = (Math.PI * 2) / len;

            final int elementWidth = 56, elementHeight = 24;

            final double radiusScale = 0.35D;
            final double radius = (Math.min(width - elementWidth * 2, height - elementHeight * 2) / 2.0D) * radiusScale;

            int selectedIndexIn = -1;

            final int dx = mouseX - centerX, dy = mouseY - centerY;
            final double innerRadius = MathHelper.clamp_double(radius - 10.0D, 5.0D, 20.0D);
            if (dx * dx + dy * dy >= innerRadius * innerRadius) {
                double rel = Math.atan2(dy, dx) - start + step / 2;
                rel = (rel % (Math.PI * 2) + (Math.PI * 2)) % (Math.PI * 2);
                final int index = (int)(rel / step) % len;
                if (index >= 0 && index < len) {
                    selectedIndexIn = index;
                }
            }

            final int halfWidth = elementWidth / 2;
            final int halfHeight = elementHeight / 2;
            for (int i = 0; i < len; i++) {
                final double angle = start + step * i;
                final int x = (centerX + (int) Math.round(Math.cos(angle) * (radius + halfWidth))) - halfWidth;
                final int y = (centerY + (int) Math.round(Math.sin(angle) * (radius + halfHeight))) - halfHeight;
                GuiUtil.drawRectWithBorder(x, y, x + elementWidth, y + elementHeight, 0xC01A1A1A, i == selectedIndexIn ? 0xFF4276FF : 0xFF060606);
                drawCenteredString(mc.fontRendererObj, pingMessages[i].getDisplayText(), x + halfWidth, y + halfHeight - (mc.fontRendererObj.FONT_HEIGHT / 2) + 1, -1);
            }

            GuiUtil.drawCenteredScaledText(mc, "Right Click to Cancel", centerX, centerY + 20, 0.5f, -1);
            this.selectedIndex = selectedIndexIn;
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            if (mouseButton == GuiUtil.MOUSE_RIGHT) {
                this.selectedIndex = -1;
                mc.displayGuiScreen(null);
                return;
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public boolean doesGuiPauseGame() {
            return false;
        }

        @Override
        public void onGuiClosed() {
            PingWheelHandler.this.currentState = isHeld() ? State.WAIT_RELEASE : State.IDLE;
            PingWheelHandler.this.currentPingWheel = null;
            if (selectedIndex != -1) {
                sendPingMessage(mc, pingMessages[selectedIndex]);
            }
        }
    }
}
package me.shvaich.moremwe.gui.data;

import me.shvaich.moremwe.config.gui.screens.MyRendererEditGuiScreen;
import me.shvaich.moremwe.gui.huds.CompassHUD;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HUDManager {
    private static final AbstractRenderer[] registeredRenderers;


    static {
        registeredRenderers = new AbstractRenderer[]{
                new CompassHUD()
        };
    }

    private HUDManager() {}

    public static void register() {
        if (registeredRenderers.length == 0) return;
        MinecraftForge.EVENT_BUS.register(new HUDManager());
    }

    @SubscribeEvent
    public void onRenderGUI(RenderGameOverlayEvent.Post e) {
        if (e.type != ElementType.TEXT || Minecraft.getMinecraft().currentScreen instanceof MyRendererEditGuiScreen) return;
        final long time = System.currentTimeMillis();
        final int screenWidth = e.resolution.getScaledWidth();
        final int screenHeight = e.resolution.getScaledHeight();
        for (final AbstractRenderer renderer : registeredRenderers) {
            if (renderer.isEnabled(time)) {
                final int width = renderer.getWidth();
                final int height = renderer.getHeight();
                final MyRendererPosition rendererPosition = renderer.getPosition();
                if (rendererPosition != null) rendererPosition.updateDrawPositionFromRelative(screenWidth - width, screenHeight - height); // height + 1
                renderer.render(screenWidth, screenHeight, width, height);
            }
        }
    }

    public static AbstractRenderer getRendererFromPosition(MyRendererPosition rendererPosition) {
        if (rendererPosition != null) {
            for (final AbstractRenderer renderer : registeredRenderers) {
                if (rendererPosition == renderer.getPosition())
                    return renderer;
            }
        }
        return null;
    }

    public static void renderAllDummy(int screenWidth, int screenHeight) {
        for (final AbstractRenderer renderer : registeredRenderers) {
            if (renderer.shouldRenderDummy()) {
                final int width = renderer.getDummyWidth();
                final int height = renderer.getDummyHeight();
                final MyRendererPosition rendererPosition = renderer.getPosition();
                if (rendererPosition != null) rendererPosition.updateDrawPositionFromRelative(screenWidth - width, screenHeight - height); // height + 1
                renderer.renderDummy(screenWidth, screenHeight, width, height);
            }
        }
    }
}

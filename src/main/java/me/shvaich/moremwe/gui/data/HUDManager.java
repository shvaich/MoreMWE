package me.shvaich.moremwe.gui.data;

import me.shvaich.moremwe.config.gui.screens.MyRendererEditGuiScreen;
import me.shvaich.moremwe.gui.huds.CompassHUD;
import me.shvaich.moremwe.gui.huds.ShovelGuardHUD;
import me.shvaich.moremwe.gui.huds.SpiderLeapHUD;
import me.shvaich.moremwe.utils.MoreMWEException;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class HUDManager {

    private static HUDManager instance;
    private final AbstractRenderer[] registeredRenderers;

    public HUDManager() {
        if (instance != null) {
            throw new MoreMWEException();
        }
        instance = this;
        registeredRenderers = new AbstractRenderer[]{
                new CompassHUD(),
                new SpiderLeapHUD(),
                new ShovelGuardHUD()
        };
    }

    public static HUDManager instance() { return instance; }

    @SubscribeEvent
    public void onRenderGUI(RenderGameOverlayEvent.Post e) {
        final Minecraft mc;
        if (e.type != ElementType.TEXT || (mc = Minecraft.getMinecraft()).currentScreen instanceof MyRendererEditGuiScreen) return;
        final long time = System.currentTimeMillis();
        final int screenWidth = e.resolution.getScaledWidth();
        final int screenHeight = e.resolution.getScaledHeight();
        for (final AbstractRenderer renderer : registeredRenderers) {
            if (renderer.isEnabled(mc, time)) {
                final int width = renderer.getWidth(mc);
                final int height = renderer.getHeight(mc);
                final MyRendererPosition rendererPosition = renderer.getPosition();
                if (rendererPosition != null) rendererPosition.updateDrawPositionFromRelative(screenWidth - width, screenHeight - height); // height + 1
                renderer.render(mc, screenWidth, screenHeight, width, height);
            }
        }
    }

    public AbstractRenderer getRendererFromPosition(MyRendererPosition rendererPosition) {
        if (rendererPosition != null) {
            for (final AbstractRenderer renderer : registeredRenderers) {
                if (rendererPosition == renderer.getPosition())
                    return renderer;
            }
        }
        return null;
    }

    public void renderAllDummy(Minecraft mc, AbstractRenderer editedRenderer, int screenWidth, int screenHeight) {
        for (final AbstractRenderer renderer : registeredRenderers) {
            if (renderer != editedRenderer && renderer.shouldRenderDummy()) {
                final int width = renderer.getDummyWidth(mc);
                final int height = renderer.getDummyHeight(mc);
                final MyRendererPosition rendererPosition = renderer.getPosition();
                if (rendererPosition != null) rendererPosition.updateDrawPositionFromRelative(screenWidth - width, screenHeight - height); // height + 1
                renderer.renderDummy(mc, screenWidth, screenHeight, width, height);
            }
        }
    }
}

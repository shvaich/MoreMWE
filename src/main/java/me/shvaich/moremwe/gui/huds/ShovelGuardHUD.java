package me.shvaich.moremwe.gui.huds;

import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.gui.data.AbstractRenderer;
import me.shvaich.moremwe.utils.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

public class ShovelGuardHUD extends AbstractRenderer {

    public ShovelGuardHUD() {
        super(MoreMWEConfig.shovelGuardHUDPosition);
    }

    @Override
    public boolean isEnabled(long currentTimeMillis) {
        return rendererPosition.isEnabled();
    }

    @Override
    public void render(int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        final int x = rendererPosition.getDrawX();
        final int y = rendererPosition.getDrawY();
        if (MoreMWEConfig.showShovelGuardHUDBackground) {
            drawBackground(x, y, thisWidth, thisHeight, MoreMWEConfig.shovelGuardHUDBackgroundColor);
        }
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + MoreMWEConfig.SHOVEL_GUARD + ": " + ChatUtil.getBooleanMsg(MoreMWEConfig.isShovelGuarded), x, y, 0xFFFFFFFF);
    }

    @Override
    public int getWidth() {
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(MoreMWEConfig.SHOVEL_GUARD + ": " + ChatUtil.getBooleanMsg(MoreMWEConfig.isShovelGuarded));
    }

    @Override
    public int getHeight() {
        return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT-1;
    }

    @Override
    public void renderDummy(int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        render(screenWidth, screenHeight, thisWidth, thisHeight);
    }
}

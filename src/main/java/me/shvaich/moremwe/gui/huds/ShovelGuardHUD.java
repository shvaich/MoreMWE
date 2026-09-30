package me.shvaich.moremwe.gui.huds;

import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.enums.MWClass;
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
    public boolean isEnabled(Minecraft mc, long currentTimeMillis) {
        return rendererPosition.isEnabled()
                && mc.thePlayer != null
                && MWEApi.Scoreboard.getScoreboardParser().isInMwGame()
                && MWEApi.Player.getPlayerInfo(mc.thePlayer).getMWClass() == MWClass.MOLEMAN;
    }

    @Override
    public void render(Minecraft mc, int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        final int x = rendererPosition.getDrawX();
        final int y = rendererPosition.getDrawY();
        if (MoreMWEConfig.showShovelGuardHUDBackground) {
            drawBackground(x, y, thisWidth, thisHeight, MoreMWEConfig.shovelGuardHUDBackgroundColor);
        }
        mc.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + MoreMWEConfig.SHOVEL_GUARD + ": " + ChatUtil.getBooleanMsg(MoreMWEConfig.isShovelGuarded), x, y, 0xFFFFFFFF);
    }

    @Override
    public int getWidth(Minecraft mc) {
        return mc.fontRendererObj.getStringWidth(MoreMWEConfig.SHOVEL_GUARD + ": " + ChatUtil.getBooleanMsg(MoreMWEConfig.isShovelGuarded));
    }

    @Override
    public int getHeight(Minecraft mc) {
        return mc.fontRendererObj.FONT_HEIGHT-1;
    }

    @Override
    public void renderDummy(Minecraft mc, int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        render(mc, screenWidth, screenHeight, thisWidth, thisHeight);
    }
}

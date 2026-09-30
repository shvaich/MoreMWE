package me.shvaich.moremwe.gui.huds;

import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.enums.MWClass;
import fr.alexdoru.mwe.api.events.MegaWallsGameEvent;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.events.ChatListener;
import me.shvaich.moremwe.gui.data.AbstractRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpiderLeapHUD extends AbstractRenderer implements ChatListener.IChatListener {

    private final Pattern primaryLeapPattern = Pattern.compile("Your primary Leap skill switched to (Arrow|Arced) mode\\.?", Pattern.CASE_INSENSITIVE);
    private Boolean isArrowThePrimaryLeap;

    public SpiderLeapHUD() {
        super(MoreMWEConfig.spiderLeapHUDPosition);
        ChatListener.INSTANCE.registerListener(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public boolean isEnabled(Minecraft mc, long currentTimeMillis) {
        return rendererPosition.isEnabled()
                && isArrowThePrimaryLeap != null
                && MWEApi.Scoreboard.getScoreboardParser().isInMwGame()
                && isSpider(mc.thePlayer)
                && (!MoreMWEConfig.hideSpiderLeapHUDDuringPrepPhase || !MWEApi.Scoreboard.getScoreboardParser().isPrepPhase())
                && (!MoreMWEConfig.onlyShowSpiderHUDWhenHoldingItems || isSpiderItem(mc.thePlayer.getHeldItem()));
    }

    @Override
    public void render(Minecraft mc, int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        renderLeap(mc, thisWidth, thisHeight, mc.thePlayer != null && mc.thePlayer.isSneaking());
    }

    @Override
    public int getWidth(Minecraft mc) {
        return getDisplayWidth(mc, mc.thePlayer != null && mc.thePlayer.isSneaking());
    }

    @Override
    public int getHeight(Minecraft mc) {
        return mc.fontRendererObj.FONT_HEIGHT-1;
    }

    @Override
    public void renderDummy(Minecraft mc, int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        renderLeap(mc, thisWidth, thisHeight, false);
    }

    @Override
    public int getDummyWidth(Minecraft mc) {
        return getDisplayWidth(mc, false);
    }

    private void renderLeap(Minecraft mc, int thisWidth, int thisHeight, boolean isShifting) {
        final int x = rendererPosition.getDrawX();
        final int y = rendererPosition.getDrawY();
        final boolean showArrow = (isArrowThePrimaryLeap == null || isArrowThePrimaryLeap) != isShifting;
        if (MoreMWEConfig.showSpiderLeapHUDBackground) {
            drawBackground(x, y, thisWidth, thisHeight, MoreMWEConfig.spiderLeapHUDBackgroundColor);
        }
        mc.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + "Leap Mode: "
                + ((showArrow != isShifting) ? EnumChatFormatting.GREEN + "Arrow" : EnumChatFormatting.AQUA + "Arced")
                + (isShifting ? (EnumChatFormatting.GRAY + " (Sneak)") : ""), x, y, 0xFFFFFFFF);
    }

    private int getDisplayWidth(Minecraft mc, boolean isShifting) {
        final boolean showArrow = (isArrowThePrimaryLeap == null || isArrowThePrimaryLeap) != isShifting;
        return mc.fontRendererObj.getStringWidth("Leap Mode: " + (showArrow ? "Arrow" : "Arced") + (isShifting ? " (Sneak)" : ""));
    }

    @Override
    public boolean processMessage(ClientChatReceivedEvent e, String formattedMsg, String msg, boolean isInMegaWalls) {
        if (e.type == 0 && isInMegaWalls && isSpider(Minecraft.getMinecraft().thePlayer)) {
            final Matcher matcher = primaryLeapPattern.matcher(msg);
            if (matcher.matches()) {
                final String primaryLeapType = matcher.group(1);
                final boolean isArrowLeap = "arrow".equalsIgnoreCase(primaryLeapType);
                if (isArrowLeap || "arced".equalsIgnoreCase(primaryLeapType)) {
                    this.isArrowThePrimaryLeap = isArrowLeap;
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onMwGameStart(MegaWallsGameEvent e) {
        if (e.type == MegaWallsGameEvent.Type.GAME_START) {
            this.isArrowThePrimaryLeap = isSpider(Minecraft.getMinecraft().thePlayer) ? true : null;
        }
        else if (e.type == MegaWallsGameEvent.Type.GAME_END) {
            this.isArrowThePrimaryLeap = null;
        }
    }

    private static boolean isSpider(EntityPlayer player) {
        return player != null && MWEApi.Player.getPlayerInfo(player).getMWClass() == MWClass.SPIDER;
    }

    private static boolean isSpiderItem(ItemStack stack) {
        final Item item;
        return stack != null && ((item = stack.getItem()) instanceof ItemSword || item instanceof ItemBow || item instanceof ItemSpade);
    }
}

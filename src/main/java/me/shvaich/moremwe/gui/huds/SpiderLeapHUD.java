package me.shvaich.moremwe.gui.huds;

import fr.alexdoru.mwe.api.MWEApi;
import fr.alexdoru.mwe.api.enums.MWClass;
import fr.alexdoru.mwe.api.events.MegaWallsGameEvent;
import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.events.ChatListener;
import me.shvaich.moremwe.gui.data.AbstractRenderer;
import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
    public boolean isEnabled(long currentTimeMillis) {
        return rendererPosition.isEnabled()
                && isArrowThePrimaryLeap != null
                && MWEApi.Scoreboard.getScoreboardParser().isInMwGame()
                && isSpider()
                && (!MoreMWEConfig.hideSpiderLeapHUDDuringPrepPhase || !MWEApi.Scoreboard.getScoreboardParser().isPrepPhase())
                && isEnabledFromHeldItem();
    }

    @Override
    public void render(int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        final Minecraft mc = Minecraft.getMinecraft();
        renderLeap(mc, thisWidth, thisHeight, mc.thePlayer != null && mc.thePlayer.isSneaking());
    }

    @Override
    public int getWidth() {
        final Minecraft mc = Minecraft.getMinecraft();
        return getDisplayWidth(mc, mc.thePlayer != null && mc.thePlayer.isSneaking());
    }

    @Override
    public int getHeight() {
        return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT-1;
    }

    @Override
    public void renderDummy(int screenWidth, int screenHeight, int thisWidth, int thisHeight) {
        renderLeap(Minecraft.getMinecraft(), thisWidth, thisHeight, GuiScreen.isShiftKeyDown());
    }

    @Override
    public int getDummyWidth() {
        return getDisplayWidth(Minecraft.getMinecraft(), GuiScreen.isShiftKeyDown());
    }

    private void renderLeap(Minecraft mc, int thisWidth, int thisHeight, boolean isShifting) {
        final int x = rendererPosition.getDrawX();
        final int y = rendererPosition.getDrawY();
        final boolean showArrow = (isArrowThePrimaryLeap == null || isArrowThePrimaryLeap) != isShifting;
        if (MoreMWEConfig.showSpiderLeapHUDBackground) {
            final int pad = 2;
            GuiUtil.drawRect(x - pad, y - pad, x + thisWidth + pad, y + thisHeight + pad, MoreMWEConfig.spiderLeapHUDBackgroundColor);
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
        if (e.type == 0 && isInMegaWalls && isSpider()) {
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
            this.isArrowThePrimaryLeap = isSpider() ? true : null;
        }
        else if (e.type == MegaWallsGameEvent.Type.GAME_END) {
            this.isArrowThePrimaryLeap = null;
        }
    }

    private static boolean isSpider() {
        final Minecraft mc;
        return (mc = Minecraft.getMinecraft()).thePlayer != null
                && MWEApi.Player.getPlayerInfo(mc.thePlayer).getMWClass() == MWClass.SPIDER;
    }

    private static boolean isEnabledFromHeldItem() {
        if (!MoreMWEConfig.onlyShowSpiderHUDWhenHoldingItems) return true;
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return false;
        final ItemStack stack = mc.thePlayer.getHeldItem();
        if (stack == null) return false;
        final Item item = stack.getItem();
        return item instanceof ItemSword || item instanceof ItemBow || item instanceof ItemSpade;
    }
}

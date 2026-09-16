package me.shvaich.moremwe.config.gui.elements.components;

import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.GuiUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class MyGuiButton {

    public int xPosition, yPosition;
    public int width, height;
    private boolean isHovered;

    public String displayString;
    private ResourceLocation texture;
    private int texturePadding;

    public MyGuiButton(int width, int height, String text) {
        this.width = width;
        this.height = height;
        this.displayString = text == null ? "" : text;
    }

    public MyGuiButton(int width, int height) {
        this(width, height, "");
    }

    public MyGuiButton(int size) { this(size, size); }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, boolean canButtonBeHovered) {
        final int right = xPosition + width;
        final int bottom = yPosition + height;
        this.isHovered = canButtonBeHovered && GuiUtil.isMouseInRect(mouseX, mouseY, xPosition, yPosition, right, bottom);
        GuiUtil.drawRectWithBorder(xPosition, yPosition, right, bottom, GuiUtil.getColorWithHover(ModResources.BUTTON_BACKGROUND, isHovered), GuiUtil.getColorWithHover(ModResources.BUTTON_BORDER, isHovered));
        if (texture != null) {
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.color(1f, 1f, 1f, 1f);
            mc.getTextureManager().bindTexture(texture);
            final int textureWidth = width - texturePadding - texturePadding;
            final int textureHeight = height - texturePadding - texturePadding;
            GuiUtil.drawFullTextureWithCustomSize(xPosition + texturePadding, yPosition + texturePadding, textureWidth, textureHeight);
            GlStateManager.disableBlend();
        }

        if (!StringUtil.isEmpty(displayString)) {
            final FontRenderer fontRenderer = mc.fontRendererObj;
            fontRenderer.drawStringWithShadow(displayString, xPosition + (width - fontRenderer.getStringWidth(displayString)) / 2, yPosition + (height - fontRenderer.FONT_HEIGHT + 1) / 2, isHovered ? 0xFFFFFFA0 : ModResources.WHITE);
        }
    }

    public final void drawButton(Minecraft mc, int mouseX, int mouseY) {
        drawButton(mc, mouseX, mouseY, true);
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        final boolean isMouseOver = GuiUtil.isMouseInRect(mouseX, mouseY, xPosition, yPosition, xPosition + width, yPosition + height);
        if (isMouseOver) {
            GuiUtil.playButtonPressSound(mc);
        }
        return isMouseOver;
    }

    public boolean isHovered() { return isHovered; }

    public MyGuiButton setTexture(ResourceLocation textureIn, int texturePaddingIn) {
        this.texture = textureIn;
        this.texturePadding = texturePaddingIn;
        return this;
    }

    public MyGuiButton setTexture(ResourceLocation textureIn) { return setTexture(textureIn, 2); }
}

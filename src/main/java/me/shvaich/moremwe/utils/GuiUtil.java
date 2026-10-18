package me.shvaich.moremwe.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class GuiUtil {
    private GuiUtil() {}

    public static final int MOUSE_LEFT = 0;
    public static final int MOUSE_RIGHT = 1;
    public static final int DIGIT_AND_SIGN_WIDTH = 6;

    public static final class Rect {
        public int LEFT, TOP, RIGHT, BOTTOM;

        public int getWidth() { return RIGHT - LEFT; }
        public int getHeight() { return BOTTOM - TOP; }
        public boolean isMouseIn(int mouseX, int mouseY) { return isMouseInRect(mouseX, mouseY, LEFT, TOP, RIGHT, BOTTOM); }

        public void draw(int color) { drawRect(LEFT, TOP, RIGHT, BOTTOM, color); }
        //public void drawBorder(int size, int color) { drawRectBorder(LEFT, TOP, RIGHT, BOTTOM, size, color); }
        public void drawWithBorder(int color, int borderColor) { drawRectWithBorder(LEFT, TOP, RIGHT, BOTTOM, color, borderColor); }
    }

    /// Gui.drawRect(left, top, right, bottom) - left & top: inclusive, right & bottom: exclusive;

    public static void drawHorizontalLine(int startX, int endX, int y, int size, int color) {
        Gui.drawRect(startX, y, endX, y + size, color);
    }

    public static void drawHorizontalLine(int startX, int endX, int y, int color) {
        drawHorizontalLine(startX, endX, y, 1 , color);
    }

    public static void drawVerticalLine(int x, int startY, int endY, int size, int color) {
        Gui.drawRect(x, startY, x + size, endY, color);
    }

    public static void drawVerticalLine(int x, int startY, int endY, int color) {
        drawVerticalLine(x, startY, endY, 1, color);
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }

    public static void drawRectBorder(int left, int top, int right, int bottom, int size, int color) {
        drawVerticalLine(left, top, bottom, size, color);
        drawVerticalLine(right-size, top, bottom, size, color);
        drawHorizontalLine(left, right, top, size, color);
        drawHorizontalLine(left, right, bottom - size, size, color);
    }

    public static void drawRectWithBorder(int left, int top, int right, int bottom, int borderSize, int color, int borderColor) {
        Gui.drawRect(left+borderSize, top+borderSize, right-borderSize, bottom-borderSize, color);
        drawRectBorder(left, top, right, bottom, borderSize, borderColor);
//        drawVerticalLine(left, top, bottom, borderSize, borderColor);
//        drawVerticalLine(right-borderSize, top, bottom, borderSize, borderColor);
//        drawHorizontalLine(left, right, top, borderSize, borderColor);
//        drawHorizontalLine(left, right, bottom-borderSize, borderSize, borderColor);
    }

    public static void drawRectWithBorder(int left, int top, int right, int bottom, int color, int borderColor) {
        drawRectWithBorder(left, top, right, bottom, 1, color, borderColor);
    }

    public static void drawFullTextureWithCustomSize(int left, int top, int drawWidth, int drawHeight) {
        Gui.drawModalRectWithCustomSizedTexture(left, top, 0, 0, drawWidth, drawHeight, drawWidth, drawHeight);
    }

    public static void drawSkinHead(Minecraft mc, ResourceLocation skin, int x, int y) { drawSkinHead(mc, skin, x, y, 8, true); }

    public static void drawSkinHead(Minecraft mc, ResourceLocation skin, int x, int y, int size, boolean renderHatLayer) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        mc.getTextureManager().bindTexture(skin);
        // face
        Gui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, size, size, 64, 64);
        if (renderHatLayer) {
            // hat
            Gui.drawScaledCustomSizeModalRect(x, y, 40, 8, 8, 8, size, size, 64, 64);
        }
        // GlStateManager.disableAlpha();
        // GlStateManager.disableBlend();
    }

    public static boolean isMouseInRect(int mouseX, int mouseY, int left, int top, int right, int bottom) {
        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    public static void playButtonPressSound(Minecraft mc) {
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public static void openScreen(final GuiScreen screen) {
        new DelayedTask(() -> Minecraft.getMinecraft().displayGuiScreen(screen));
    }

    public static int getColorWithHover(int color, boolean isHovered) {
        return isHovered ? ColorUtil.brighter(color, 0.12F) : color;
    }

    /** Only works correctly for n in range [-999, 999] */
    public static int getNumberWidth(int n, boolean withPositiveSign, boolean withZeroSign) {
        if (n == 0) return ((withPositiveSign && withZeroSign) ? 2 : 1) * DIGIT_AND_SIGN_WIDTH;
        int sign = 0;
        if (n < 0) {
            n = -n;
            sign = 1;
        }
        else if (withPositiveSign) sign = 1;
        return (sign + (n < 10 ? 1 : (n < 100 ? 2 : 3))) * DIGIT_AND_SIGN_WIDTH;
    }

    /**
     * Equivalent to {@code getNumberWidth(n, false, false)}.
     * @see #getNumberWidth(int, boolean, boolean)
     */
    public static int getNumberWidth(int n) {
        return getNumberWidth(n, false, false);
    }

    /**
     * Begins the stencil clear rect.
     * Call {@link #endClearRect()} after rendering if this returned {@code true}.
     * @return {@code true} if this operation worked
     */
    public static boolean beginClearRect(Minecraft mc, int left, int top, int right, int bottom) {
        if (!mc.getFramebuffer().isStencilEnabled() && !mc.getFramebuffer().enableStencil()) return false;
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);

        GlStateManager.colorMask(false, false, false, false);

        Gui.drawRect(left, top, right, bottom, 0xFFFFFFFF);

        GlStateManager.colorMask(true, true, true, true);

        GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF); // skip hole
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);

        return true;
        // GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    /** See {@link #beginClearRect(Minecraft, int, int, int, int)} */
    public static void endClearRect() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }


    public static void drawDirectionArrow(int x, int y, float rotation, int color) {
        GlStateManager.pushMatrix();
        {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();

            GlStateManager.color(
                    ColorUtil.getRed(color) / 255.0F,
                    ColorUtil.getGreen(color) / 255.0F,
                    ColorUtil.getBlue(color) / 255.0F,
                    1.0F
            );

            final float halfWidth = 2.5F; // ; 5 / 2.0F
            final float halfHeight = 3.0F; // ; 6 / 2.0F

            GL11.glLineWidth(2.0F);
            //GlStateManager.translate(x + 4.0F, y + 4.0F, 0.0F);
            GlStateManager.translate(x + halfWidth, y + halfHeight, 0.0F);
            GlStateManager.rotate(rotation, 0.0F, 0.0F, 1.0F);
            GL11.glBegin(GL11.GL_LINE_STRIP);
            //GL11.glVertex2f(-2.5F, 2.5F);
            //GL11.glVertex2f(0.0F, -3.5F);
            //GL11.glVertex2f(2.5F, 2.5F);
            GL11.glVertex2f(-halfWidth, halfHeight);
            GL11.glVertex2f(0.0F, -halfHeight);
            GL11.glVertex2f(halfWidth, halfHeight);
            GL11.glEnd();

            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
        }
        GlStateManager.popMatrix();
    }
}
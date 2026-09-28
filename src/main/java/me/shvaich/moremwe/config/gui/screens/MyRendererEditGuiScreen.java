package me.shvaich.moremwe.config.gui.screens;

import fr.alexdoru.configlib.api.ColorPalette;
import me.shvaich.moremwe.config.gui.elements.components.MyGuiButton;
import me.shvaich.moremwe.gui.data.AbstractRenderer;
import me.shvaich.moremwe.gui.data.HUDManager;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.gui.data.MyRendererPosition;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Collections;

public class MyRendererEditGuiScreen extends GuiScreen {
    private static final int BUTTON_SIZE = 16;

    private static boolean drawBackground = true;
    private static boolean drawCenterLines = true;

    private final GuiScreen parentScreen;
    private final AbstractRenderer renderer;
    private final MyRendererPosition rendererPosition;
    private final MyGuiButton resetToDefaultButton;
    private final MyGuiButton undoChangesButton;
    private final MyGuiButton drawBackgroundButton;
    private final MyGuiButton drawCenterLinesButton;

    private final double originalRelativeX, originalRelativeY;
    private boolean isFirstResize = true;
    private int prevScreenWidth, prevScreenHeight;
    private int prevRendererWidth, prevRendererHeight;
    private boolean isDragging;
    private int prevMouseX, prevMouseY;

    public MyRendererEditGuiScreen(GuiScreen parentScreen, AbstractRenderer renderer) {
        this.parentScreen = parentScreen;
        this.renderer = renderer;
        this.rendererPosition = renderer.getPosition();
        this.originalRelativeX = rendererPosition.getRelativeX();
        this.originalRelativeY = rendererPosition.getRelativeY();

        this.resetToDefaultButton = new MyGuiButton(BUTTON_SIZE).setTexture(ModResources.RELOAD_ICON);

        this.undoChangesButton = new MyGuiButton(BUTTON_SIZE).setTexture(ModResources.UNDO_ICON);

        this.drawBackgroundButton = new MyGuiButton(BUTTON_SIZE) {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY, boolean canButtonBeHovered) {
                super.drawButton(mc, mouseX, mouseY, canButtonBeHovered);
                final int pad = 3;
                final int backgroundColor = drawBackground ? 0xA0101010 : 0x20FFFFFF;
                final int borderColor = drawBackground ? 0xFF141414 : 0xFF8A8A8A;
                GuiUtil.drawRectWithBorder(xPosition + pad, yPosition + pad, xPosition + width - pad, yPosition + height - pad, backgroundColor, borderColor);
            }
        };

        this.drawCenterLinesButton = new MyGuiButton(BUTTON_SIZE) {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY, boolean canButtonBeHovered) {
                super.drawButton(mc, mouseX, mouseY, canButtonBeHovered);
                final int pad = 4;
                GuiUtil.drawVerticalLine(xPosition + this.width / 2, yPosition + pad, yPosition + this.height - pad + 1, ModResources.WHITE);
                GuiUtil.drawHorizontalLine(xPosition + pad, xPosition + this.width - pad + 1, yPosition + this.height / 2, ModResources.WHITE);
                final int boxSize = 2;
                final int boxRight = xPosition + BUTTON_SIZE - boxSize;
                final int boxBottom = yPosition + BUTTON_SIZE - boxSize;
                GuiUtil.drawRect(boxRight - boxSize, boxBottom - boxSize, boxRight, boxBottom, drawCenterLines ? ColorPalette.VanillaColors.GREEN : ColorPalette.VanillaColors.RED);
            }
        };
    }

    @Override
    public void initGui() {
        final int rendererWidth = renderer.getDummyWidth();
        final int rendererHeight = renderer.getDummyHeight();
        if (!isFirstResize) {
            rendererPosition.savePositionToRelative(prevScreenWidth - prevRendererWidth, prevScreenHeight - prevRendererHeight); // (height) + 1
        }
        rendererPosition.updateDrawPositionFromRelative(this.width - rendererWidth, this.height - rendererHeight); // (height) + 1
        this.isFirstResize = false;
        this.prevScreenWidth = this.width;
        this.prevScreenHeight = this.height;
        this.prevRendererWidth = rendererWidth;
        this.prevRendererHeight = rendererHeight;
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        renderCrosshair();
        HUDManager.getInstance().renderAllDummy(renderer, this.width, this.height);
        if (drawBackground) {
            drawGradientRect(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        }
        final int dummyWidth = renderer.getDummyWidth();
        final int dummyHeight = renderer.getDummyHeight();
        final int relativeScreenWidth = this.width - dummyWidth;
        final int relativeScreenHeight = this.height - dummyHeight; // (height) + 1
        if (isDragging) {
            final int posX = MathHelper.clamp_int(rendererPosition.getDrawX() + mouseX - prevMouseX, 0, relativeScreenWidth);
            final int posY = MathHelper.clamp_int(rendererPosition.getDrawY() + mouseY - prevMouseY, 0, relativeScreenHeight);
            rendererPosition.setDrawPosition(posX, posY);
        }
        renderer.renderDummy(this.width, this.height, dummyWidth, dummyHeight);
        if (!isDragging) {
            final int buttonX = this.width - BUTTON_SIZE - 2;
            int buttonY = 2;

            resetToDefaultButton.xPosition = buttonX;
            resetToDefaultButton.yPosition = buttonY;
            resetToDefaultButton.drawButton(mc, mouseX, mouseY);
            buttonY += resetToDefaultButton.height + 2;

            undoChangesButton.xPosition = buttonX;
            undoChangesButton.yPosition = buttonY;
            undoChangesButton.drawButton(mc, mouseX, mouseY);
            buttonY += undoChangesButton.height + 2;

            drawBackgroundButton.xPosition = buttonX;
            drawBackgroundButton.yPosition = buttonY;
            drawBackgroundButton.drawButton(mc, mouseX, mouseY);
            buttonY += drawBackgroundButton.height + 2;

            drawCenterLinesButton.xPosition = buttonX;
            drawCenterLinesButton.yPosition = buttonY;
            drawCenterLinesButton.drawButton(mc, mouseX, mouseY);
            //buttonY += drawCenterLinesButton.height + 2;

            String hoveringText = null;
            if (resetToDefaultButton.isHovered()) {
                hoveringText = "Reset to Default Position";
            }
            else if (undoChangesButton.isHovered()) {
                hoveringText = "Undo Changes";
            }
            else if (drawBackgroundButton.isHovered()) {
                hoveringText = "Draw Screen Background: " + ChatUtil.getBooleanMsg(drawBackground);
            }
            else if (drawCenterLinesButton.isHovered()) {
                hoveringText = "Show crosshair guidelines: " + ChatUtil.getBooleanMsg(drawCenterLines);
            }

            if (hoveringText != null) {
                drawHoveringText(Collections.singletonList(hoveringText), mouseX, mouseY + 14);
            }
        }
        this.prevMouseX = mouseX;
        this.prevMouseY = mouseY;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            if (!isDragging) {
                final boolean clickedOnResetToDefault = resetToDefaultButton.mousePressed(mc, mouseX, mouseY);
                if (clickedOnResetToDefault || undoChangesButton.mousePressed(mc, mouseX, mouseY)) {
                    if (clickedOnResetToDefault) rendererPosition.resetToDefaultPosition();
                    else rendererPosition.setRelativePosition(originalRelativeX, originalRelativeY);
                    rendererPosition.updateDrawPositionFromRelative(this.width - renderer.getDummyWidth(), this.height - renderer.getDummyHeight()); // (height) + 1
                    return;
                }
                if (drawBackgroundButton.mousePressed(mc, mouseX, mouseY)) {
                    drawBackground = !drawBackground;
                    return;
                }
                if (drawCenterLinesButton.mousePressed(mc, mouseX, mouseY)) {
                    drawCenterLines = !drawCenterLines;
                    return;
                }
            }
            this.isDragging = true;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == GuiUtil.MOUSE_LEFT && isDragging) {
            this.isDragging = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            GuiUtil.openScreen(parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        rendererPosition.savePositionToRelative(this.width - renderer.getDummyWidth(), this.height - renderer.getDummyHeight()); // (height) + 1
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void renderCrosshair() {
        mc.getTextureManager().bindTexture(Gui.icons);
        drawTexturedModalRect(this.width / 2 - 7, this.height / 2 - 7, 0, 0, 16, 16);
        if (drawCenterLines) {
            final int color = 0xFFAAAAAA;
            GuiUtil.drawVerticalLine(this.width / 2, 0, this.height, color);
            GuiUtil.drawHorizontalLine(0, this.width, this.height / 2, color);
        }
    }

    /**
     * Usage Example:
     * <blockquote><pre>
     *     if (Minecraft.getMinecraft().currentScreen instanceof MyRendererEditGuiScreen)
     *          ((MyRendererEditGuiScreen) Minecraft.getMinecraft().currentScreen).getEditedRenderer();
     * </pre></blockquote>
     * @return The currently edited renderer
     */
    public AbstractRenderer getEditedRenderer() {
        return renderer;
    }
}

package me.shvaich.moremwe.config.gui.elements;

import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.moremwe.config.gui.elements.base.IOverlayHost;
import me.shvaich.moremwe.config.gui.elements.components.McColorPanel;
import me.shvaich.moremwe.config.gui.elements.components.MyGuiButton;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Collections;
import java.util.List;

public class ConfigMinecraftColorButton extends ConfigGuiButton implements IOverlayHost {

    private final MyGuiButton button;
    private McColorPanel colorPanel;
    private MyGuiButton doneButton, resetButton, undoButton;
    private final GuiUtil.Rect overlayRect = new GuiUtil.Rect();

    private boolean isOverlayOpen;
    private final int defaultColorIndex;
    private int lastSavedColorIndex;
    private int selectedColorIndex;
    private int selectedColor;

    public ConfigMinecraftColorButton(MyConfigGuiScreen screen, ConfigFieldContainer fieldData, EnumChatFormatting defaultColor) throws IllegalAccessException {
        super(screen, fieldData);
        final EnumChatFormatting color = (EnumChatFormatting) fieldData.getValue();
        this.button = new MyGuiButton(0, 20, McColorPanel.getMcColorDisplayText(color));
        this.selectedColor = McColorPanel.getMcColorAsARGB(mc, color);
        this.defaultColorIndex = defaultColor.getColorIndex();
        this.lastSavedColorIndex = this.selectedColorIndex = color.getColorIndex();
    }

    @Override
    public void onResize(int elementRectWidth) {
        int maxWidth = 0;
        for (final EnumChatFormatting c : EnumChatFormatting.values()) {
            if (c.isColor()) {
                maxWidth = Math.max(maxWidth, 9 + mc.fontRendererObj.getStringWidth(McColorPanel.getMcColorDisplayText(c)));
            }
        }
        button.width = maxWidth;
        this.rightSideContentWidth = button.width + BUTTON_RIGHT_MARGIN + 1 + button.height; // button.height == color_box_size
        this.rightSideContentHeight = button.height;
        super.onResize(elementRectWidth);
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY, boolean canMouseBeOverElement) {
        super.draw(x, y, mouseX, mouseY, canMouseBeOverElement);
        final int top = y + getCenterYOffset(button.height);
        final int colorBoxSize = button.height;
        GuiUtil.drawRectWithBorder(contentLeft, top, contentLeft + colorBoxSize, top + colorBoxSize, selectedColor, ModResources.BLACK);
        button.xPosition = contentLeft + colorBoxSize + 1;
        button.yPosition = top;
        button.drawButton(mc, mouseX, mouseY, canMouseBeOverElement);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT && button.mousePressed(mc, mouseX, mouseY)) {
            if (!isOverlayOpen()) {
                this.colorPanel = new McColorPanel(true);
                this.doneButton = new MyGuiButton(getMainButtonWidth(), 14, "Done");
                this.resetButton = new MyGuiButton(doneButton.height).setTexture(ModResources.RELOAD_ICON);
                this.undoButton = new MyGuiButton(doneButton.height).setTexture(ModResources.UNDO_ICON);
                this.isOverlayOpen = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isOverlayOpen() {
        return isOverlayOpen;
    }

    @Override
    public void closeOverlay() {
        this.isOverlayOpen = false;
        this.lastSavedColorIndex = this.selectedColorIndex;
        this.colorPanel = null;
        this.doneButton = this.resetButton = this.undoButton = null;
    }

    @Override
    public boolean isMouseOverOverlay(int mouseX, int mouseY) {
        return overlayRect.isMouseIn(mouseX, mouseY);
    }

    @Override
    public void drawOverlay(int mouseX, int mouseY) {
        final GuiUtil.Rect configRect = screen.getConfigRect();
        final int pad = 8;
        final int headerFooterMargin = 6;
        final int panelHeight = colorPanel.getHeight();
        final int overlayWidth = pad + colorPanel.getWidth() + pad;
        final int overlayHeight = pad + mc.fontRendererObj.FONT_HEIGHT - 1 + headerFooterMargin + panelHeight + headerFooterMargin + doneButton.height + pad;
        overlayRect.LEFT = configRect.LEFT + (configRect.getWidth() - overlayWidth) / 2;
        overlayRect.TOP = configRect.TOP + (configRect.getHeight() - overlayHeight) / 2;
        overlayRect.RIGHT = overlayRect.LEFT + overlayWidth;
        overlayRect.BOTTOM = overlayRect.TOP + overlayHeight;
        overlayRect.drawWithBorder(ModResources.ELEMENT_BACKGROUND, ModResources.ELEMENT_BORDER);

        int y = overlayRect.TOP + pad;

        final String header = getName();
        final int headerX = overlayRect.LEFT + (overlayWidth - mc.fontRendererObj.getStringWidth(header)) / 2;
        mc.fontRendererObj.drawStringWithShadow(header, headerX, y, -1);
        y += mc.fontRendererObj.FONT_HEIGHT - 1 + headerFooterMargin;

        colorPanel.draw(mc, overlayRect.LEFT + pad, y, mouseX, mouseY);
        y += panelHeight + headerFooterMargin;

        final int buttonGap = 6;
        final int totalButtonsWidth = undoButton.width + buttonGap + doneButton.width + buttonGap + resetButton.width;

        undoButton.xPosition = overlayRect.LEFT + (overlayWidth - totalButtonsWidth) / 2;
        doneButton.xPosition = undoButton.xPosition + undoButton.width + buttonGap;
        resetButton.xPosition = doneButton.xPosition + doneButton.width + buttonGap;
        undoButton.yPosition = doneButton.yPosition = resetButton.yPosition = y;
        undoButton.drawButton(mc, mouseX, mouseY);
        doneButton.drawButton(mc, mouseX, mouseY);
        resetButton.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public List<String> getOverlayHoveringTextLines() {
        if (undoButton.isHovered()) {
            return Collections.singletonList("Undo Changes");
        }
        else if (resetButton.isHovered()) {
            return Collections.singletonList("Reset to Default");
        }
        else if (colorPanel.getHoveredIndex() != -1) {
            return Collections.singletonList(colorPanel.getColorDisplayTextAt(colorPanel.getHoveredIndex()));
        }
        return null;
    }

    @Override
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (isMouseOverOverlay(mouseX, mouseY)) {
            final int clickedColorIndex;
            if ((clickedColorIndex = colorPanel.mousePressed(mc, mouseX, mouseY)) != -1) {
                handleOverlayColorClick(clickedColorIndex);
            }
            else if (doneButton.mousePressed(mc, mouseX, mouseY)) {
                closeOverlay();
            }
            else if (undoButton.mousePressed(mc, mouseX, mouseY)) {
                handleOverlayColorClick(lastSavedColorIndex);
            }
            else if (resetButton.mousePressed(mc, mouseX, mouseY)) {
                handleOverlayColorClick(defaultColorIndex);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleOverlayMouseInput() {
        if (Mouse.getEventDWheel() != 0) {
            return overlayRect.isMouseIn(screen.calculateMouseX(), screen.calculateMouseY());
        }
        return false;
    }

    @Override
    public boolean overlayKeyTyped(char typedChar, int keyCode) {
        return keyCode != Keyboard.KEY_ESCAPE;
    }

    @Override
    public boolean closeOnConfigMouseWheelScroll() { return false; }

    private void handleOverlayColorClick(int clickedIndex) throws IllegalAccessException {
        fieldData.setValue(colorPanel.getEnumColorAt(clickedIndex));
        this.selectedColorIndex = clickedIndex;
        this.selectedColor = colorPanel.getColorARGBAt(mc, clickedIndex);
        button.displayString = colorPanel.getColorDisplayTextAt(clickedIndex);
    }
}

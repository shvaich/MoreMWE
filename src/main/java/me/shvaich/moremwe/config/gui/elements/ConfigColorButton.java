package me.shvaich.moremwe.config.gui.elements;

import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.moremwe.config.gui.elements.components.MyGuiButton;
import me.shvaich.moremwe.config.gui.screens.MyColorEditGuiScreen;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.ColorUtil;
import me.shvaich.moremwe.utils.GuiUtil;

public class ConfigColorButton extends ConfigGuiButton {

    private final int COLOR_BOX_SIZE;
    private final int defaultColor;
    private final MyGuiButton button;
    private int color;

    public final boolean allowsTransparency;

    public ConfigColorButton(MyConfigGuiScreen screen, ConfigFieldContainer fieldData, int defaultColor) throws IllegalAccessException {
        super(screen, fieldData);
        this.color = (int) fieldData.getValue();
        this.defaultColor = defaultColor;
        this.allowsTransparency = fieldData.getAnnotation().allowsTransparency() || !ColorUtil.isOpaque(this.defaultColor);
        this.button = new MyGuiButton(getMainButtonWidth(), 20, "Change");
        this.COLOR_BOX_SIZE = button.height;
        this.rightSideContentWidth = button.width + BUTTON_RIGHT_MARGIN + COLOR_BOX_SIZE + 1;
        this.rightSideContentHeight = button.height; // Math.max(button.height, COLOR_BOX_SIZE);
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY, boolean canMouseBeOverElement) {
        super.draw(x, y, mouseX, mouseY, canMouseBeOverElement);
        final int drawY = y + getCenterYOffset(rightSideContentHeight);
        GuiUtil.drawRectWithBorder(contentLeft, drawY, contentLeft + COLOR_BOX_SIZE, drawY + COLOR_BOX_SIZE, ColorUtil.toOpaque(this.color), ModResources.BLACK);
        button.xPosition = contentLeft + COLOR_BOX_SIZE + 1;
        button.yPosition = drawY;
        button.drawButton(mc, mouseX, mouseY, canMouseBeOverElement);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT && button.mousePressed(mc, mouseX, mouseY)) {
            GuiUtil.openScreen(new MyColorEditGuiScreen(screen, this));
            return true;
        }
        return false;
    }

    public void setColor(int color) { this.color = color; }
    public int getColor() { return this.color; }

    public int getDefaultColor() { return this.defaultColor; }

    public void saveColorToField() throws IllegalAccessException {
        this.fieldData.setValue(this.color);
    }
}

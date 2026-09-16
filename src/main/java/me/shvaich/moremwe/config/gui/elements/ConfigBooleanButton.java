package me.shvaich.moremwe.config.gui.elements;

import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.moremwe.config.gui.elements.components.MyGuiButton;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.GuiUtil;

public class ConfigBooleanButton extends ConfigGuiButton {
    protected MyGuiButton button;
    protected boolean isOn;

    public ConfigBooleanButton(MyConfigGuiScreen screen, ConfigFieldContainer fieldData) throws IllegalAccessException {
        this(screen, fieldData, true);
    }

    protected ConfigBooleanButton(MyConfigGuiScreen screen, ConfigFieldContainer fieldData, boolean initialize) throws IllegalAccessException {
        super(screen, fieldData, initialize);
    }

    @Override
    protected void initialize() throws IllegalAccessException {
        super.initialize();
        this.isOn = getBoolean();
        this.button = new MyGuiButton(getMainButtonWidth(), 20, ChatUtil.getBooleanMsg(isOn));
        this.rightSideContentWidth = button.width + BUTTON_RIGHT_MARGIN;
        this.rightSideContentHeight = button.height;
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY, boolean canMouseBeOverElement) {
        super.draw(x, y, mouseX, mouseY, canMouseBeOverElement);
        button.xPosition = contentLeft;
        button.yPosition = y + getCenterYOffset(rightSideContentHeight);
        button.drawButton(mc, mouseX, mouseY, canMouseBeOverElement);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT && button.mousePressed(mc, mouseX, mouseY)) {
            toggleBoolean();
            this.isOn = getBoolean();
            this.button.displayString = ChatUtil.getBooleanMsg(isOn);
            if (fieldData.hasDependants())
                screen.updateDependantsDisplay(fieldData);
            return true;
        }
        return false;
    }

    protected boolean getBoolean() throws IllegalAccessException {
        return fieldData.getBoolean();
    }

    protected void toggleBoolean() throws IllegalAccessException {
        fieldData.setValue(!getBoolean());
    }
}

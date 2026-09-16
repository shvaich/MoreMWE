package me.shvaich.moremwe.config.gui.elements;

import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.moremwe.config.gui.elements.components.MyGuiButton;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.config.gui.screens.MyRendererEditGuiScreen;
import me.shvaich.moremwe.gui.data.AbstractRenderer;
import me.shvaich.moremwe.gui.data.HUDManager;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.gui.data.MyRendererPosition;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.GuiUtil;

import java.util.Collections;
import java.util.List;

public class ConfigHUDButton extends ConfigGuiButton {

    private final MyRendererPosition rendererPosition;
    private final MyGuiButton enabledButton;
    private final MyGuiButton editButton;

    public ConfigHUDButton(MyConfigGuiScreen screen, ConfigFieldContainer fieldData) throws IllegalAccessException {
        super(screen, fieldData);
        this.rendererPosition = (MyRendererPosition) fieldData.getValue();
        this.enabledButton = new MyGuiButton(getMainButtonWidth(), 20, ChatUtil.getBooleanMsg(rendererPosition.isEnabled()));
        this.editButton = new MyGuiButton(enabledButton.height).setTexture(ModResources.MOVE_ICON);
        this.rightSideContentWidth = enabledButton.width + 1 + editButton.width + BUTTON_RIGHT_MARGIN;
        this.rightSideContentHeight = Math.max(enabledButton.height, editButton.height);
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY, boolean canMouseBeOverElement) {
        super.draw(x, y, mouseX, mouseY, canMouseBeOverElement);
        editButton.xPosition = contentLeft;
        editButton.yPosition = y + getCenterYOffset(rightSideContentHeight);
        editButton.drawButton(mc, mouseX, mouseY, canMouseBeOverElement);

        enabledButton.xPosition = editButton.xPosition + editButton.width + 1;
        enabledButton.yPosition = editButton.yPosition;
        enabledButton.drawButton(mc, mouseX, mouseY, canMouseBeOverElement);
    }

    @Override
    public List<String> getHoveringTextLines(int mouseX, int mouseY) {
        return editButton.isHovered() ? Collections.singletonList("Edit HUD") : super.getHoveringTextLines(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            if (enabledButton.mousePressed(mc, mouseX, mouseY)) {
                final boolean previousValue = rendererPosition.isEnabled();
                rendererPosition.setEnabled(!previousValue);
                fieldData.invokeAction(previousValue);
                enabledButton.displayString = ChatUtil.getBooleanMsg(rendererPosition.isEnabled());
                if (fieldData.hasDependants())
                    screen.updateDependantsDisplay(fieldData);
                return true;
            }
            if (editButton.mousePressed(mc, mouseX, mouseY)) {
                final AbstractRenderer renderer = HUDManager.getRendererFromPosition(rendererPosition);
                if (renderer != null) {
                    GuiUtil.openScreen(new MyRendererEditGuiScreen(screen, renderer));
                }
                else { throw new RuntimeException("No HUD associated to: " + fieldData.getName()); }
                return true;
            }
        }
        return false;
    }
}

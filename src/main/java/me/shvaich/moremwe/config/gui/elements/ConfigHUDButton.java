package me.shvaich.moremwe.config.gui.elements;

import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.config.gui.screens.MyRendererEditGuiScreen;
import me.shvaich.moremwe.gui.data.AbstractRenderer;
import me.shvaich.moremwe.gui.data.HUDManager;
import me.shvaich.moremwe.gui.data.MyRendererPosition;
import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.client.gui.GuiButton;

public class ConfigHUDButton extends ConfigGuiButton {

    private final MyRendererPosition rendererPosition;
    private final GuiButton enabledButton;
    private final GuiButton editButton;

    public ConfigHUDButton(MyConfigGuiScreen screen, ConfigFieldContainer fieldData) throws IllegalAccessException {
        super(screen, fieldData);
        this.rendererPosition = (MyRendererPosition) fieldData.getValue();
        this.enabledButton = getMcGuiButton(getMcButtonText(rendererPosition.isEnabled()));
        this.editButton = getMcGuiButton(getMcButtonText("Position"));
    }

    @Override
    protected int getRightSideContentWidth() {
        return enabledButton.width + BUTTON_RIGHT_MARGIN;
    }

    @Override
    protected int getRightSideContentHeight() {
        return enabledButton.height + 1 + editButton.height;
    }

    @Override
    public void updateDisplayFromDependency() {
        enabledButton.displayString = getMcButtonText(rendererPosition.isEnabled());
        editButton.displayString = getMcButtonText("Position");
    }

    @Override
    protected boolean showDependencyIndicatorFromMouse(int mouseX, int mouseY) {
        return enabledButton.isMouseOver() || editButton.isMouseOver();
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        super.draw(x, y, mouseX, mouseY);
        enabledButton.xPosition = contentLeft;
        enabledButton.yPosition = y + (drawHeight - getRightSideContentHeight()) / 2;
        enabledButton.drawButton(mc, mouseX, mouseY);

        editButton.xPosition = enabledButton.xPosition;
        editButton.yPosition = enabledButton.yPosition + enabledButton.height + 1;
        editButton.drawButton(mc, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            if (enabledButton.mousePressed(mc, mouseX, mouseY)) {
                final boolean previousValue = rendererPosition.isEnabled();
                rendererPosition.setEnabled(!previousValue);
                fieldData.invokeAction(previousValue);
                enabledButton.displayString = getMcButtonText(rendererPosition.isEnabled());
                if (fieldData.hasDependants())
                    screen.updateDependantsDisplay(fieldData);
                enabledButton.playPressSound(mc.getSoundHandler());
                return true;
            }
            if (editButton.mousePressed(mc, mouseX, mouseY)) {
                editButton.playPressSound(mc.getSoundHandler());
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

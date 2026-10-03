package me.shvaich.moremwe.config.gui.elements.custom;

import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.elements.ConfigBooleanButton;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.features.GuardableBlock;
import me.shvaich.moremwe.utils.GuiUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class ConfigGuardableBlockButton extends ConfigBooleanButton {

    private static final int BLOCK_ICON_SIZE = 16;

    private final GuardableBlock block;
    private final ItemStack blockItem;
    private int posX, posY;

    public ConfigGuardableBlockButton(MyConfigGuiScreen screen, ConfigFieldContainer fieldData, GuardableBlock block) throws IllegalAccessException {
        super(screen, fieldData, false);
        this.block = block;
        this.blockItem = new ItemStack(block.mainBlock);
        initialize();
    }

    @Override
    public String getName() {
        return block.displayName;
    }

    @Override
    protected String getComment() {
        return "";
    }

    @Override
    protected int getLeftPadding() {
        return BLOCK_ICON_SIZE + 2 + super.getLeftPadding();
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY, boolean canMouseBeOverElement) {
        this.posX = x;
        this.posY = y;
        super.draw(x, y, mouseX, mouseY, canMouseBeOverElement);
        final int iconY = y + getCenterYOffset(BLOCK_ICON_SIZE);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(blockItem, x + 4, iconY);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public List<String> getHoveringTextLines(int mouseX, int mouseY) {
        if (StringUtil.isEmpty(block.specialText)) return super.getHoveringTextLines(mouseX, mouseY);
        final int left = posX + 4;
        final int top = posY + getCenterYOffset(BLOCK_ICON_SIZE);
        return GuiUtil.isMouseInRect(mouseX, mouseY, left, top, left + BLOCK_ICON_SIZE, top + BLOCK_ICON_SIZE) ? Collections.singletonList(block.specialText) : super.getHoveringTextLines(mouseX, mouseY);
    }

    @Override
    protected boolean getBoolean() {
        return MoreMWEConfig.guardedBlocks.contains(block.key);
    }

    @Override
    protected void toggleBoolean() {
        if (!MoreMWEConfig.guardedBlocks.remove(block.key))
            MoreMWEConfig.guardedBlocks.add(block.key);
    }
}

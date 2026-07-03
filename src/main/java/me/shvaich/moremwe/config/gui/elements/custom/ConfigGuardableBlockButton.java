package me.shvaich.moremwe.config.gui.elements.custom;

import me.shvaich.moremwe.config.MoreMWEConfig;
import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.elements.ConfigBooleanButton;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.features.GuardableBlock;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class ConfigGuardableBlockButton extends ConfigBooleanButton {

    private final GuardableBlock block;
    private final ItemStack blockItem;

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
        return block.comment;
    }

    @Override
    protected int getLeftPadding() {
        return 18 + super.getLeftPadding();
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        super.draw(x, y, mouseX, mouseY);
        final int iconY = y + (drawHeight - 16) / 2;
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(blockItem, x + 4, iconY);
        RenderHelper.disableStandardItemLighting();
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

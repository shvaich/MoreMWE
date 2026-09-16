package me.shvaich.moremwe.config.gui.elements;

import fr.alexdoru.configlib.api.ColorPalette;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.client.Minecraft;

public class ConfigSubcategoryHeader extends ConfigGuiElement {
    private final String categoryName;
    private final String subcategoryName;

    public ConfigSubcategoryHeader(String categoryName, String subcategoryName) {
        this.categoryName = categoryName;
        this.subcategoryName = subcategoryName;
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY, boolean canMouseBeOverElement) {
        final Minecraft mc = Minecraft.getMinecraft();
        drawSelf(mc, x, y, x);
    }

    protected void drawSelf(Minecraft mc, int x, int y, int lineStartX) {
        final int textWidth = mc.fontRendererObj.getStringWidth(subcategoryName);
        final int textX = x + (elementRectWidth - textWidth) / 2;
        final int lineY = y + mc.fontRendererObj.FONT_HEIGHT / 2;
        GuiUtil.drawHorizontalLine(lineStartX, textX - 4, lineY, ModResources.ELEMENT_BORDER);
        GuiUtil.drawHorizontalLine(textX + textWidth + 4, x + elementRectWidth, lineY, ModResources.ELEMENT_BORDER);
        mc.fontRendererObj.drawStringWithShadow(subcategoryName, textX, y, ModResources.GRAYISH);
    }

    @Override
    public int getHeight() {
        return Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        return false;
    }

    @Override
    public String getCategory() {
        return categoryName;
    }

    @Override
    public String getSubcategory() {
        return ""; //subcategoryName;
    }

    @Override
    public int getTopMargin() {
        return 8;
    }

    @Override
    public int getBottomMargin() {
        return 4;
    }

    public static final class WithCategory extends ConfigSubcategoryHeader {
        public WithCategory(String categoryName, String subcategoryName) {
            super(categoryName, subcategoryName);
        }

        @Override
        protected void drawSelf(Minecraft mc, int x, int y, int lineStartX) {
            lineStartX = mc.fontRendererObj.drawStringWithShadow(getCategory(), x, y, ColorPalette.VanillaColors.GOLD);
            super.drawSelf(mc, x, y, lineStartX + 3);
        }
    }
}

package me.shvaich.moremwe.config.gui.elements.components;

import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.ColorUtil;
import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

public final class McColorPanel {

    private static final int BUTTON_SIZE = 16;
    private static final int BUTTON_GAP = 4;

    private final ColorButton[] colorButtons = new ColorButton[16];
    private int rows, cols;
    private int hoveredIndex = -1;

    public McColorPanel(boolean isHorizontal) {
        for (final EnumChatFormatting c : EnumChatFormatting.values()) {
            if (c.isColor()) {
                colorButtons[c.getColorIndex()] = new ColorButton(c);
            }
        }
        setHorizontal(isHorizontal);
    }

    public void draw(Minecraft mc, int x, int y, int mouseX, int mouseY) {
        final int len = colorButtons.length;
        final int fullSize = BUTTON_SIZE + BUTTON_GAP;

        int hoveredIndexIn = -1;
        for (int i = 0; i < len; i++) {
            final ColorButton button = colorButtons[i];
            final int row = i / this.cols;
            final int col = i % this.cols;
            button.width = button.height = BUTTON_SIZE;
            button.xPosition = x + col * fullSize;
            button.yPosition = y + row * fullSize;
            button.drawButton(mc, mouseX, mouseY);
            if (button.isHovered()) {
                hoveredIndexIn = i;
            }
        }
        this.hoveredIndex = hoveredIndexIn;
    }

    public int mousePressed(Minecraft mc, int mouseX, int mouseY) {
        for (int i = 0; i < colorButtons.length; i++) {
            if (colorButtons[i].mousePressed(mc, mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }

    public McColorPanel setHorizontal(boolean isHorizontal) {
        this.rows = isHorizontal ? 2 : colorButtons.length / 2;
        this.cols = colorButtons.length / rows;
        return this;
    }

    public int getWidth() { return cols * (BUTTON_SIZE + BUTTON_GAP) - BUTTON_GAP; }
    public int getHeight() { return rows * (BUTTON_SIZE + BUTTON_GAP) - BUTTON_GAP; }

    public int getHoveredIndex() { return hoveredIndex; }

    public EnumChatFormatting getEnumColorAt(int index) { return colorButtons[index].enumColor; }
    public int getColorARGBAt(Minecraft mc, int index) { return getMcColorAsARGB(mc, getEnumColorAt(index)); }
    public String getColorDisplayTextAt(int index) { return colorButtons[index].displayString; }

    public static int getMcColorAsARGB(Minecraft mc, EnumChatFormatting color) {
        return ColorUtil.toOpaque(mc.fontRendererObj.getColorCode(color.toString().charAt(1)));
    }

    public static String getMcColorDisplayText(EnumChatFormatting color) { return color + color.name().replace('_', ' '); }

    private static final class ColorButton extends MyGuiButton {
        public final EnumChatFormatting enumColor;

        public ColorButton(EnumChatFormatting color) {
            super(0, 0, getMcColorDisplayText(color));
            this.enumColor = color;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, boolean canButtonBeHovered) {
            final int right = xPosition + width;
            final int bottom = yPosition + height;
            this.isHovered = canButtonBeHovered && GuiUtil.isMouseInRect(mouseX, mouseY, xPosition, yPosition, right, bottom);
            GuiUtil.drawRectWithBorder(xPosition, yPosition, right, bottom, getMcColorAsARGB(mc, enumColor), isHovered ? ModResources.WHITE : ModResources.BLACK);
        }
    }
}

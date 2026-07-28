package me.shvaich.moremwe.config.gui.elements.base;

import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.GuiUtil;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ConfigGuiButton extends ConfigGuiElement {

    protected static final int PADDING = 8;
    protected static final int BUTTON_RIGHT_MARGIN = 20;

    private static final int COMMENT_TOP_MARGIN = 4;

    private final List<String> commentLines = new ArrayList<>();
    protected boolean hasComment;

    protected final MyConfigGuiScreen screen;
    protected final Minecraft mc = Minecraft.getMinecraft();
    protected final ConfigFieldContainer fieldData;

    /** The distance between the left-most position of the content (button) and the right side of the rect */
    protected int rightSideContentWidth;

    /** The entire height of the right side content (buttons, slider, etc...) */
    protected int rightSideContentHeight;

    protected int contentLeft;
    private int boxHeight;

    public ConfigGuiButton(MyConfigGuiScreen screen, ConfigFieldContainer fieldData) throws IllegalAccessException {
        this(screen, fieldData, true);
    }

    protected ConfigGuiButton(MyConfigGuiScreen screen, ConfigFieldContainer fieldData, boolean initialize) throws IllegalAccessException {
        this.screen = screen;
        this.fieldData = fieldData;
        if (initialize) { initialize(); }
    }

    protected void initialize() throws IllegalAccessException {
        this.hasComment = !getComment().isEmpty();
    }

    /** Called when this needs to update its display from its dependency */
    public abstract void updateDisplayFromDependency();

    protected abstract boolean showDependencyIndicatorFromMouse(int mouseX, int mouseY);

    public String getName() { return fieldData.getName(); }

    protected String getComment() { return fieldData.getAnnotation().comment(); }

    /** {@link #rightSideContentWidth} must be correctly set before this is called */
    @Override
    public void onResize(int elementRectWidth) {
        super.onResize(elementRectWidth);
        if (hasComment) {
            final int wrapWidth = elementRectWidth - getLeftPadding() - rightSideContentWidth - 16; // 20
            commentLines.clear();
            commentLines.addAll(getCommentLines(mc, getComment(), wrapWidth));
        }
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        final int right = x + elementRectWidth;
        this.boxHeight = getHeight();
        this.contentLeft = right - rightSideContentWidth;
        final int textX = x + getLeftPadding();
        final int textY = y + (hasComment ? PADDING : ((boxHeight - mc.fontRendererObj.FONT_HEIGHT) / 2));
        GuiUtil.drawRectWithBorder(x, y, right, y + boxHeight, ModResources.ELEMENT_BACKGROUND, ModResources.ELEMENT_BORDER);
        mc.fontRendererObj.drawStringWithShadow(getName(), textX, textY, ModResources.WHITE);
        if (hasComment) {
            int drawY = textY + mc.fontRendererObj.FONT_HEIGHT + COMMENT_TOP_MARGIN;
            for (final String comment : commentLines) {
                mc.fontRendererObj.drawStringWithShadow(comment, textX, drawY, ModResources.COMMENT_COLOR);
                drawY += mc.fontRendererObj.FONT_HEIGHT;
            }
        }
    }

    @Override
    public int getHeight() {
        int textHeight = mc.fontRendererObj.FONT_HEIGHT;
        if (hasComment)
            textHeight += COMMENT_TOP_MARGIN + mc.fontRendererObj.FONT_HEIGHT * commentLines.size();

        return PADDING + Math.max(textHeight, rightSideContentHeight) + PADDING; // - 1
    }

    @Override
    public List<String> getHoveringTextLines(int mouseX, int mouseY) {
        if (hasDependency() && screen.showElementsAsInactiveIfDependencyDisabled() && showDependencyIndicatorFromMouse(mouseX, mouseY)) {
            final ConfigFieldContainer disabledDependency = getDisabledDependency();
            if (disabledDependency != null) {
                String dependsOnIdentifier = EnumChatFormatting.WHITE + disabledDependency.getName() + EnumChatFormatting.GRAY;
                if (screen.isInSearchMode() || !getCategory().equals(disabledDependency.getCategory()))
                    dependsOnIdentifier = EnumChatFormatting.GOLD + disabledDependency.getCategory() + EnumChatFormatting.GRAY + "'s " + dependsOnIdentifier;

                return Collections.singletonList(EnumChatFormatting.GRAY + "Ignored while " + dependsOnIdentifier + " is " + EnumChatFormatting.RED + "disabled");
            }
        }
        return null;
    }

    @Override
    public String getCategory() { return fieldData.getCategory(); }

    @Override
    public String getSubcategory() { return fieldData.getSubcategory(); }

    @Override
    public boolean matchSearch(String search) {
        return StringUtil.toLowerCase(getCategory()).contains(search) ||
            StringUtil.defaultIfEmpty(StringUtil.toLowerCase(getSubcategory()), "general").contains(search) ||
            StringUtil.toLowerCase(getName()).contains(search);
    }

    protected final int getCenterYOffset(int height) { return (boxHeight - height) / 2; }

    protected int getLeftPadding() { return PADDING; }

    protected final GuiButton getMcGuiButton(String text) {
        return getMcGuiButton(getMainButtonWidth(), 20, text);
    }

    protected final GuiButton getMcGuiButton(int width, int height, String text) {
        return new GuiButton(-1, 0, 0, width, height, text) {
            @Override
            protected int getHoverState(boolean mouseOver) {
                final int superHoverState = super.getHoverState(mouseOver);
                if (superHoverState == 2 && !screen.canMouseVisuallyBeOverElement()) {
                    this.packedFGColour = 0xE0E0E0;
                    return 1;
                }
                this.packedFGColour = 0;
                return superHoverState;
            }
        };
    }

    protected final int getMainButtonWidth() {
        return mc.fontRendererObj.getStringWidth("Disabled") + 9;
    }

    protected final String getMcButtonText(boolean bool) {
        if (hasDependency() && screen.showElementsAsInactiveIfDependencyDisabled())
            return ChatUtil.getBooleanMsg(bool, !hasDisabledDependency());
        return ChatUtil.getBooleanMsg(bool);
    }

    protected final String getMcButtonText(String str) {
        if (showAsInactive()) {
            return EnumChatFormatting.GRAY + str;
        }
        return str;
    }

    public boolean hasDependency() { return fieldData.getDependsOn() != null; }

    public boolean doesDependOn(ConfigFieldContainer dependency) {
        ConfigFieldContainer dependsOn = fieldData.getDependsOn();
        while (dependsOn != null) {
            if (dependsOn == dependency) return true;
            dependsOn = dependsOn.getDependsOn();
        }
        return false;
    }

    public boolean hasDisabledDependency() {
        return getDisabledDependency() != null;
    }

    private ConfigFieldContainer getDisabledDependency() {
        ConfigFieldContainer dependency = fieldData.getDependsOn();
        try {
            while (dependency != null) {
                if (!dependency.getBoolean()) return dependency;
                dependency = dependency.getDependsOn();
            }
        }
        catch (Exception e) { throw new RuntimeException("Failed to get dependency boolean", e); }
        return null;
    }

    protected boolean showAsInactive() {
        return screen.showElementsAsInactiveIfDependencyDisabled() && hasDisabledDependency();
    }
}
package me.shvaich.moremwe.config.gui.elements;

import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiButton;
import me.shvaich.moremwe.config.gui.elements.base.IOverlayHost;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.GuiUtil;
import me.shvaich.moremwe.utils.MoreMWEException;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Objects;


public abstract class ConfigSelectorButton extends ConfigGuiButton implements IOverlayHost {

    private static final int EXPANDED_BORDER_SIZE = 2;

    private final String[] options;
    private Scrollbar scrollbar;
    private int selectedOptionIndex;
    private boolean isExpanded;

    private int buttonWidth, buttonHeight;
    private int buttonLeft, buttonTop, buttonRight, buttonBottom;
    private int dropdownLeft, dropdownTop, dropdownRight, dropdownBottom;

    public static final class ForInteger extends ConfigSelectorButton {
        public ForInteger(MyConfigGuiScreen screen, ConfigFieldContainer field) throws IllegalAccessException {
            super(screen, field, field.getAnnotation().options(), (int) field.getValue());
        }

        @Override
        protected void setValue(int clickedIndex) throws IllegalAccessException {
            fieldData.setValue(clickedIndex);
        }
    }

    public static final class ForEnum extends ConfigSelectorButton {
        private final Enum<?>[] universe;

        public ForEnum(MyConfigGuiScreen screen, ConfigFieldContainer field, Enum<?>[] universe) throws IllegalAccessException {
            super(screen, field, getOptions(universe), ((Enum<?>) field.getValue()).ordinal());
            this.universe = universe;
        }

        @Override
        protected void setValue(int clickedIndex) throws IllegalAccessException {
            fieldData.setValue(universe[clickedIndex]);
        }

        private static String[] getOptions(Enum<?>[] universe) {
            final int len = universe.length;
            final String[] options = new String[len];
            for (int i = 0; i < len; i++) {
                StringBuilder builder = null;
                for (final String str : universe[i].name().split("_")) {
                    if (str.isEmpty()) continue;
                    (builder == null ? (builder = new StringBuilder()) : builder.append(' '))
                            .append(StringUtil.capitalize(StringUtil.toLowerCase(str)));
                }
                options[i] = Objects.requireNonNull(builder).toString();
            }
            return options;
        }
    }

    private ConfigSelectorButton(MyConfigGuiScreen screen, ConfigFieldContainer field, String[] displayOptions, int selectedIndex) throws IllegalAccessException {
        super(screen, field);
        if (displayOptions.length == 0)
            throw new MoreMWEException().sendDebug("Config Selector cannot have 0 options");

        this.options = displayOptions;

        if (selectedIndex < 0 || selectedIndex >= options.length)
            throw new MoreMWEException().sendDebug("Config Selector value is out of options bounds");

        this.selectedOptionIndex = selectedIndex;
    }

    protected abstract void setValue(int clickedIndex) throws IllegalAccessException;

    @Override
    public void onResize(int elementRectWidth) {
        String longestString = "";
        for (final String option : this.options)
            if (longestString.length() < option.length())
                longestString = option;

        this.buttonWidth = Math.max((int)(mc.fontRendererObj.getStringWidth(longestString) * 0.75f) + 24, getMainButtonWidth());
        this.buttonHeight = mc.fontRendererObj.FONT_HEIGHT + 6;
        this.rightSideContentWidth = BUTTON_RIGHT_MARGIN + buttonWidth;
        this.rightSideContentHeight = buttonHeight;
        super.onResize(elementRectWidth);
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY, boolean canMouseBeOverElement) {
        super.draw(x, y, mouseX, mouseY, canMouseBeOverElement);
        this.buttonLeft = contentLeft;
        this.buttonTop = y + getCenterYOffset(rightSideContentHeight);
        this.buttonRight = this.buttonLeft + this.buttonWidth;
        this.buttonBottom = this.buttonTop + this.buttonHeight;
        final boolean isMouseOverButton = canMouseBeOverElement && GuiUtil.isMouseInRect(mouseX, mouseY, buttonLeft, buttonTop, buttonRight, buttonBottom);
        final int backgroundColor = (isMouseOverButton || isExpanded) ? 0xFF444444 : 0xFF282828;
        GuiUtil.drawRect(buttonLeft, buttonTop, buttonRight, buttonBottom, backgroundColor);
        final float arrowImageColorScale = isMouseOverButton ? 1 : 0.7f;
        mc.getTextureManager().bindTexture(isExpanded ? ModResources.ARROW_UP : ModResources.ARROW_DOWN);
        GlStateManager.color(arrowImageColorScale, arrowImageColorScale, arrowImageColorScale, 1);
        GuiUtil.drawFullTextureWithCustomSize(buttonRight - 12, buttonTop + (buttonHeight - 5) / 2, 7, 4);
        if (arrowImageColorScale != 1) GlStateManager.color(1, 1, 1, 1);
        final int textLeft = buttonLeft + 4;
        drawScaledText(options[selectedOptionIndex], textLeft, buttonTop, isMouseOverButton ? ModResources.WHITE : ModResources.GRAYISH);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT && GuiUtil.isMouseInRect(mouseX, mouseY, buttonLeft, buttonTop, buttonRight, buttonBottom)) {
            if (isOverlayOpen()) {
                closeOverlay();
            }
            else {
                // should always create a scrollbar, if it should be rendered or not should be decided in rendering code. not here
                this.dropdownTop = buttonBottom + EXPANDED_BORDER_SIZE;
                int maxBottom = screen.height - EXPANDED_BORDER_SIZE;
                // bad fix to a possible issue, im lazy
                if (dropdownTop >= maxBottom) {
                    this.scrollbar = null;
                    this.dropdownBottom = dropdownTop;
                }
                else {  // normal code
                    final int allElementsHeight = options.length * buttonHeight;
                    maxBottom = Math.min(maxBottom, dropdownTop + (5 * buttonHeight));
                    this.scrollbar = Scrollbar.create(allElementsHeight, dropdownTop, maxBottom);
                    this.dropdownBottom = scrollbar == null ? (dropdownTop + allElementsHeight) : scrollbar.getTrackBottom();
                }
                this.isExpanded = true;
            }
            GuiUtil.playButtonPressSound(mc);
            return true;
        }
        return false;
    }

    @Override
    public boolean isOverlayOpen() {
        return isExpanded;
    }

    @Override
    public void closeOverlay() {
        this.isExpanded = false;
        this.scrollbar = null;
    }

    @Override
    public boolean isMouseOverOverlay(int mouseX, int mouseY) {
        return GuiUtil.isMouseInRect(mouseX, mouseY, buttonLeft, buttonBottom, buttonRight, dropdownBottom + EXPANDED_BORDER_SIZE);
    }

    @Override
    public void drawOverlay(int mouseX, int mouseY) {
        if (dropdownTop >= dropdownBottom) return;
        this.dropdownLeft = buttonLeft + EXPANDED_BORDER_SIZE;
        this.dropdownRight = buttonRight - EXPANDED_BORDER_SIZE;
        GuiUtil.drawRectWithBorder(buttonLeft, buttonBottom, buttonRight, dropdownBottom + EXPANDED_BORDER_SIZE, EXPANDED_BORDER_SIZE, 0xFF282828, 0xFF444444);
        final boolean hasScrollbar = scrollbar != null;
        if (hasScrollbar) {
            scrollbar.smoothScroll();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(
                    (int)(dropdownLeft * screen.SCREEN_WIDTH_SCALE),
                    (int)(mc.displayHeight - (dropdownBottom * screen.SCREEN_HEIGHT_SCALE)),
                    (int)((dropdownRight - dropdownLeft) * screen.SCREEN_WIDTH_SCALE),
                    (int)((dropdownBottom - dropdownTop) * screen.SCREEN_HEIGHT_SCALE)
            );
        }
        final int hoveredIndex = getDropdownIndexFromMouse(mouseX, mouseY);
        int textY = dropdownTop - (hasScrollbar ? scrollbar.getScroll() : 0);
        for (int i = 0; i < options.length; ++i) {
            final int textBottom = textY + buttonHeight;
            if (!hasScrollbar || (textY <= dropdownBottom && textBottom >= dropdownTop)) {
                int color = ModResources.GRAYISH;
                if (i == hoveredIndex) {
                    color = ModResources.WHITE;
                    GuiUtil.drawRect(dropdownLeft, textY, dropdownRight, textBottom, 0xFF343434);
                }
                if (i == selectedOptionIndex) {
                    final float checkmarkColorScale = i == hoveredIndex ? 1 : 0.7f;
                    mc.getTextureManager().bindTexture(ModResources.CHECKMARK);
                    GlStateManager.color(checkmarkColorScale, checkmarkColorScale, checkmarkColorScale, 1);
                    GuiUtil.drawFullTextureWithCustomSize(dropdownRight - 10, textY + (buttonHeight - 5) / 2, 6, 5);
                    if (checkmarkColorScale != 1) GlStateManager.color(1, 1, 1, 1);
                }
                drawScaledText(options[i], buttonLeft + 5, textY, color);
            }
            textY = textBottom;
        }
        if (hasScrollbar) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            scrollbar.draw(dropdownRight, buttonRight, mouseY);
        }
    }

    @Override
    public boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException {
        if (mouseButton == GuiUtil.MOUSE_LEFT) {
            if (isMouseOverOverlay(mouseX, mouseY)) {
                final int clickedIndex = getDropdownIndexFromMouse(mouseX, mouseY);
                if (clickedIndex != -1) {
                    if (clickedIndex != selectedOptionIndex) {
                        setValue(clickedIndex);
                        this.selectedOptionIndex = clickedIndex;
                    }
                    closeOverlay();
                }
                else if (scrollbar == null || !scrollbar.mouseClicked(mouseX, mouseY, mouseButton)) {
                    closeOverlay();
                }
                GuiUtil.playButtonPressSound(mc);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleasedOverlay(int mouseX, int mouseY, int mouseButton) {
        if (scrollbar != null) scrollbar.mouseReleased(mouseX, mouseY, mouseButton);
        return false;
    }

    @Override
    public boolean handleOverlayMouseInput() {
        if (scrollbar != null) {
            final int eventDWheel = Mouse.getEventDWheel();
            if (eventDWheel != 0) {
                final int mouseX = screen.calculateMouseX();
                final int mouseY = screen.calculateMouseY();
                if (GuiUtil.isMouseInRect(mouseX, mouseY, dropdownLeft, dropdownTop, dropdownRight, dropdownBottom)) {
                    scrollbar.updateFromMouseScroll(eventDWheel);
                    return true;
                }
            }
        }
        return false;
    }

    private void drawScaledText(String text, int left, int top, int color) {
        GlStateManager.pushMatrix();
        {
            final float scale = 0.75f;
            GlStateManager.translate(left, top + ((buttonHeight - mc.fontRendererObj.FONT_HEIGHT*scale) / 2f), 0);
            GlStateManager.scale(scale, scale, scale);
            mc.fontRendererObj.drawStringWithShadow(text, 0, 0, color);
        }
        GlStateManager.popMatrix();
    }

    private int getDropdownIndexFromMouse(int mouseX, int mouseY) {
        if (GuiUtil.isMouseInRect(mouseX, mouseY, dropdownLeft, dropdownTop, dropdownRight, dropdownBottom)) {
            final int scrollIn = scrollbar != null ? scrollbar.getScroll() : 0;
            final int clickedOptionIndex = (mouseY - dropdownTop + scrollIn) / buttonHeight;
            if (clickedOptionIndex >= 0 && clickedOptionIndex < options.length)
                return clickedOptionIndex;
        }
        return -1;
    }
}
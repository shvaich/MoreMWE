package me.shvaich.moremwe.config.gui.screens;

import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.data.AbstractConfig;
import me.shvaich.moremwe.config.data.ConfigCategoryContainer;
import me.shvaich.moremwe.config.data.ConfigFieldContainer;
import me.shvaich.moremwe.config.gui.elements.*;
import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.moremwe.config.gui.elements.base.IOverlayHost;
import me.shvaich.moremwe.gui.data.ModResources;
import me.shvaich.moremwe.utils.GuiUtil;
import me.shvaich.moremwe.utils.StringUtil;
import me.shvaich.moremwe.utils.DelayedTask;
import me.shvaich.moremwe.utils.MoreMWEException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;

public class MyConfigGuiScreen extends GuiScreen {
    private static final int MAX_WIDTH = 640;
    private static final int MAX_HEIGHT = 480;
    private static final int BORDER_SIZE = 2;
    private static final int HEADER_HEIGHT = 20;
    private static final int MENU_VERTICAL_PADDING = 6;
    private static final int MENU_HORIZONTAL_PADDING = 8;
    private static final int CONFIG_VERTICAL_PADDING = 6;
    private static final int CONFIG_HORIZONTAL_PADDING = 8;

    private final AbstractConfig configHandler;
    private final List<ConfigCategoryMenuEntry> categoryMenuEntries = new ArrayList<>();
    private final List<ConfigGuiElement> configElements = new ArrayList<>();
    private final List<ConfigGuiElement> renderedConfigElements = new ArrayList<>();
    private final List<ConfigGuiElement> everyElementThatShouldBeRendered = new ArrayList<>();
    private final Searchbar searchbar;
    private DelayedTask searchbarTask;

    private boolean hidePropertyIfHasDisabledDependency;
    private String selectedCategory;

    private int BORDER_LEFT, BORDER_TOP, BORDER_RIGHT;
    private int INNER_LEFT, INNER_RIGHT, INNER_BOTTOM;
    private int MENU_LEFT, MENU_TOP, MENU_RIGHT, MENU_BOTTOM;
    private int CONFIG_LEFT, CONFIG_TOP, CONFIG_RIGHT, CONFIG_BOTTOM;

    private Scrollbar configScrollbar;
    private Scrollbar menuScrollbar;
    private IOverlayHost openOverlay;
    private ConfigSliderButton activeSlider;

    public double SCREEN_WIDTH_SCALE, SCREEN_HEIGHT_SCALE;

    public MyConfigGuiScreen(AbstractConfig configHandler, List<ConfigCategoryContainer> configCategories, List<ConfigFieldContainer> configFields, LinkedHashMap<String, LinkedHashMap<String, List<String>>> configStructure, boolean hidePropertyIfHasDisabledDependencyIn) throws IllegalAccessException {
        this.configHandler = configHandler;
        this.hidePropertyIfHasDisabledDependency = hidePropertyIfHasDisabledDependencyIn;
        final Map<String, ConfigCategoryContainer> categoryContainersMap = new HashMap<>();
        for (final ConfigCategoryContainer configCategoryContainer : configCategories)
            categoryContainersMap.put(configCategoryContainer.getCategoryName(), configCategoryContainer);

        final Map<String, Map<String, ConfigGuiElement[]>> configButtonsMap = new HashMap<>();
        for (final ConfigFieldContainer configFieldContainer : configFields) {
            final ConfigGuiElement[] configButtons = configFieldContainer.getConfigButtons(this);
            if (configButtons != null) {
                configButtonsMap.computeIfAbsent(configFieldContainer.getCategory(), t -> new HashMap<>())
                        .put(configFieldContainer.getName(), configButtons);
            }
        }

        for (final Map.Entry<String, LinkedHashMap<String, List<String>>> entry : configStructure.entrySet()) {
            final String categoryName = entry.getKey();
            final Map<String, ConfigGuiElement[]> categoryConfigButtonsMap = configButtonsMap.get(categoryName);
            if (categoryConfigButtonsMap == null) continue;
            boolean needToAddCategory = true;
            int addedSubcategoriesCount = 0;
            for (final Map.Entry<String, List<String>> subcategoryEntry : entry.getValue().entrySet()) {
                final String subcategoryName = subcategoryEntry.getKey();
                boolean needToAddSubcategory = true;
                for (final String configName : subcategoryEntry.getValue()) {
                    final ConfigGuiElement[] configButtons = categoryConfigButtonsMap.get(configName);
                    if (configButtons == null) continue;
                    if (needToAddCategory) {
                        final ConfigCategoryContainer categoryContainer = categoryContainersMap.get(categoryName);
                        this.categoryMenuEntries.add(new ConfigCategoryMenuEntry(this, categoryName));
                        if (categoryContainer != null) this.configElements.add(new ConfigCategoryHeader(categoryContainer));
                        else this.configElements.add(new ConfigCategoryHeader(categoryName));
                        needToAddCategory = false;
                    }
                    if (needToAddSubcategory) {
                        final String displayname = subcategoryName.isEmpty() ? (addedSubcategoriesCount > 0 ? "General" : null) : subcategoryName;
                        if (displayname != null) {
                            this.configElements.add(new ConfigSubcategoryHeader(categoryName, displayname));
                            ++addedSubcategoriesCount;
                        }
                        needToAddSubcategory = false;
                    }
                    if (configButtons.length == 1) configElements.add(configButtons[0]);
                    else configElements.addAll(Arrays.asList(configButtons));
                }
            }
        }

        if (configElements.isEmpty()) {
            this.searchbar = null;
            renderedConfigElements.add(new ConfigSubcategoryHeader("", "There are no settings!"));
        }
        else {
            this.searchbar = new Searchbar();
            setSelectedCategory(configElements.get(0).getCategory());
        }
    }

    @Override
    public void initGui() {
        final int DRAW_WIDTH = Math.min(width - 20, MAX_WIDTH);
        final int DRAW_HEIGHT = Math.min(height - 20, MAX_HEIGHT);

        BORDER_LEFT = (width - DRAW_WIDTH) / 2;
        BORDER_TOP = (height - DRAW_HEIGHT) / 2;
        BORDER_RIGHT = BORDER_LEFT + DRAW_WIDTH;
        final int BORDER_BOTTOM = BORDER_TOP + DRAW_HEIGHT;

        INNER_LEFT = BORDER_LEFT + BORDER_SIZE;
        INNER_RIGHT = BORDER_RIGHT - BORDER_SIZE;
        INNER_BOTTOM = BORDER_BOTTOM - BORDER_SIZE;

        MENU_LEFT = INNER_LEFT;
        MENU_TOP = BORDER_TOP + HEADER_HEIGHT;
        MENU_RIGHT = MENU_LEFT + Math.min(DRAW_WIDTH / 5, 120);
        //MENU_BOTTOM = INNER_BOTTOM;
        int categoryMenuEntriesHeight = 0;
        final int categoryMenuElementRectWidth = MENU_RIGHT - (MENU_LEFT + MENU_HORIZONTAL_PADDING);
        for (final ConfigCategoryMenuEntry categoryMenuEntry : categoryMenuEntries) {
            categoryMenuEntry.onResize(categoryMenuElementRectWidth);
            categoryMenuEntriesHeight += categoryMenuEntry.getHeight() + categoryMenuEntry.getBottomMargin();
        }
        if (!categoryMenuEntries.isEmpty()) {
            categoryMenuEntriesHeight += MENU_VERTICAL_PADDING * 2 - categoryMenuEntries.get(categoryMenuEntries.size()-1).getBottomMargin();
        }
        this.menuScrollbar = Scrollbar.create(categoryMenuEntriesHeight, MENU_TOP, INNER_BOTTOM, menuScrollbar);
        MENU_BOTTOM = this.menuScrollbar == null ? (MENU_TOP + categoryMenuEntriesHeight) : this.menuScrollbar.getTrackBottom();

        CONFIG_LEFT = MENU_RIGHT + BORDER_SIZE;
        CONFIG_TOP = MENU_TOP;
        CONFIG_RIGHT = INNER_RIGHT;
        //CONFIG_BOTTOM = INNER_BOTTOM;

        final int configElementRectWidth = getConfigElementsRectWidth();
        for (final ConfigGuiElement configGuiElement : configElements)
            configGuiElement.onResize(configElementRectWidth);

        if (isInSearchMode()) {
            for (final ConfigGuiElement element : everyElementThatShouldBeRendered)
                if (element instanceof ConfigSubcategoryHeader)
                    element.onResize(configElementRectWidth);
        }

        if (configElements.isEmpty()) {
            renderedConfigElements.get(0).onResize(configElementRectWidth);
        }

        int renderedElementsHeight = 0;
        for (final ConfigGuiElement element : renderedConfigElements)
            renderedElementsHeight += getEntireElementHeight(element);

        // sets CONFIG_BOTTOM
        updateConfigScrollbar(renderedElementsHeight, this.configScrollbar);

        if (searchbar != null)
            searchbar.onResize(mc, BORDER_TOP + (HEADER_HEIGHT - searchbar.getHeight()) / 2, CONFIG_RIGHT - CONFIG_HORIZONTAL_PADDING);


        final ScaledResolution res = new ScaledResolution(mc);
        this.SCREEN_WIDTH_SCALE = mc.displayWidth / res.getScaledWidth_double();
        this.SCREEN_HEIGHT_SCALE = mc.displayHeight / res.getScaledHeight_double();

        closeOpenOverlay();
        clearActiveSlider();
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        final boolean hasMenuScrollbar = menuScrollbar != null;
        final boolean hasConfigScrollbar = configScrollbar != null;

        if (hasMenuScrollbar) this.menuScrollbar.smoothScroll();
        if (hasConfigScrollbar) this.configScrollbar.smoothScroll();

        final int headerBottom = BORDER_TOP + HEADER_HEIGHT;
        drawDefaultBackground();

        drawRect(INNER_LEFT, BORDER_TOP, INNER_RIGHT, headerBottom, ModResources.CONFIG_HEADER_BACKGROUND);
        GuiUtil.drawVerticalLine(BORDER_LEFT, BORDER_TOP, headerBottom, BORDER_SIZE, ModResources.CONFIG_HEADER_BORDER);
        GuiUtil.drawVerticalLine(MENU_RIGHT, BORDER_TOP, headerBottom, BORDER_SIZE, ModResources.CONFIG_HEADER_BORDER);
        GuiUtil.drawVerticalLine(INNER_RIGHT, BORDER_TOP, headerBottom, BORDER_SIZE, ModResources.CONFIG_HEADER_BORDER);

        drawRect(INNER_LEFT, headerBottom, INNER_RIGHT, INNER_BOTTOM, ModResources.CONFIG_BACKGROUND);
        GuiUtil.drawVerticalLine(BORDER_LEFT, headerBottom, INNER_BOTTOM, BORDER_SIZE, ModResources.CONFIG_BORDER);
        GuiUtil.drawVerticalLine(MENU_RIGHT, headerBottom, INNER_BOTTOM, BORDER_SIZE, ModResources.CONFIG_BORDER);
        GuiUtil.drawVerticalLine(INNER_RIGHT, headerBottom, INNER_BOTTOM, BORDER_SIZE, ModResources.CONFIG_BORDER);
        GuiUtil.drawHorizontalLine(BORDER_LEFT, BORDER_RIGHT, INNER_BOTTOM, BORDER_SIZE, ModResources.CONFIG_BORDER);

        final int menuDrawX = MENU_LEFT + MENU_HORIZONTAL_PADDING;
        fontRendererObj.drawString(MoreMWE.NAME + " - " + MoreMWE.VERSION, menuDrawX, BORDER_TOP + ((HEADER_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2) + 1, ModResources.WHITE);

        final boolean isMouseOverOverlay = openOverlay != null && openOverlay.isMouseOverOverlay(mouseX, mouseY);

        if (searchbar != null) searchbar.draw(mouseX, mouseY);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GL11.glScissor(
                (int) (MENU_LEFT * SCREEN_WIDTH_SCALE),
                (int) (mc.displayHeight - (MENU_BOTTOM * SCREEN_HEIGHT_SCALE)),
                (int) ((MENU_RIGHT - MENU_LEFT) * SCREEN_WIDTH_SCALE),
                (int) ((MENU_BOTTOM - MENU_TOP) * SCREEN_HEIGHT_SCALE));

        final boolean canMouseBeOverCategoryEntry = !isMouseOverOverlay && GuiUtil.isMouseInRect(mouseX, mouseY, MENU_LEFT, MENU_TOP, MENU_RIGHT, MENU_BOTTOM);
        int menuDrawY = MENU_TOP + MENU_VERTICAL_PADDING - (hasMenuScrollbar ? menuScrollbar.getScroll() : 0);
//        if (!categoryMenuEntries.isEmpty())
//            menuDrawY -= categoryMenuEntries.get(0).getTopMargin();
        for (final ConfigCategoryMenuEntry categoryMenuEntry : categoryMenuEntries) {
            if (menuDrawY <= MENU_BOTTOM && (menuDrawY + categoryMenuEntry.getHeight()) >= MENU_TOP)
                categoryMenuEntry.draw(menuDrawX, menuDrawY, mouseX, mouseY, canMouseBeOverCategoryEntry);
            menuDrawY += categoryMenuEntry.getHeight() + categoryMenuEntry.getBottomMargin();
        }

        GL11.glScissor(
                (int) (CONFIG_LEFT * SCREEN_WIDTH_SCALE),
                (int) (mc.displayHeight - (CONFIG_BOTTOM * SCREEN_HEIGHT_SCALE)),
                (int) ((CONFIG_RIGHT - CONFIG_LEFT) * SCREEN_WIDTH_SCALE),
                (int) ((CONFIG_BOTTOM - CONFIG_TOP) * SCREEN_HEIGHT_SCALE));

        final boolean canMouseVisuallyBeOverElement = !isMouseOverOverlay && GuiUtil.isMouseInRect(mouseX, mouseY, CONFIG_LEFT, CONFIG_TOP, CONFIG_RIGHT, CONFIG_BOTTOM);
        List<String> hoveringTextLines = null;
        final int drawX = CONFIG_LEFT + CONFIG_HORIZONTAL_PADDING;
        int drawY = CONFIG_TOP + CONFIG_VERTICAL_PADDING - (hasConfigScrollbar ? configScrollbar.getScroll() : 0);

        int ignoreTopMarginCount = 1;
        if (renderedConfigElements.size() > 1 && renderedConfigElements.get(0) instanceof ConfigCategoryHeader) {
            ignoreTopMarginCount++;
        }
        for (final ConfigGuiElement element : renderedConfigElements) {
            int tY = drawY;
            if (ignoreTopMarginCount > 0) ignoreTopMarginCount--;
            else tY += element.getTopMargin();
            final int elementHeight = element.getHeight();
            if (tY <= CONFIG_BOTTOM && (tY + elementHeight) >= CONFIG_TOP) {
                element.draw(drawX, tY, mouseX, mouseY, canMouseVisuallyBeOverElement);
                if (canMouseVisuallyBeOverElement && (hoveringTextLines == null || hoveringTextLines.isEmpty()))
                    hoveringTextLines = element.getHoveringTextLines(mouseX, mouseY);
            }
            drawY = tY + elementHeight + element.getBottomMargin();
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (hasMenuScrollbar) menuScrollbar.draw(MENU_RIGHT, CONFIG_LEFT, mouseY);
        if (hasConfigScrollbar) configScrollbar.draw(CONFIG_RIGHT, BORDER_RIGHT, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (openOverlay != null) {
            openOverlay.drawOverlay(mouseX, mouseY);
            if (hoveringTextLines == null || hoveringTextLines.isEmpty()) {
                hoveringTextLines = openOverlay.getOverlayHoveringTextLines();
            }
        }

        if (hoveringTextLines != null) drawHoveringText(hoveringTextLines, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        IOverlayHost overlayToClose = null;
        boolean needToUnfocusSearchbar = searchbar != null && searchbar.isFocused();
        if (activeSlider != null) activeSlider.release(); // not best approach but still the best way to avoid bugs
        scan: {  // sorry
            if (openOverlay != null) {
                try {
                    if (openOverlay.mouseClickedOverlay(mouseX, mouseY, mouseButton)) {
                        if (!openOverlay.isOverlayOpen()) this.openOverlay = null;
                        break scan;
                    }
                }
                catch (Exception e) { throw new MoreMWEException(e); }
                if (openOverlay.isOverlayOpen()) {
                    overlayToClose = openOverlay;
                }
                this.openOverlay = null;
            }

            if (GuiUtil.isMouseInRect(mouseX, mouseY, MENU_LEFT, MENU_TOP, MENU_RIGHT, MENU_BOTTOM)) {
                for (final ConfigCategoryMenuEntry categoryMenuEntry : categoryMenuEntries)
                    if (categoryMenuEntry.mouseClicked(mouseX, mouseY, mouseButton))
                        break scan;
            }
            else if (GuiUtil.isMouseInRect(mouseX, mouseY, CONFIG_LEFT, CONFIG_TOP, CONFIG_RIGHT, CONFIG_BOTTOM)) {
                try {
                    int drawY = CONFIG_TOP + CONFIG_VERTICAL_PADDING - (configScrollbar != null ? configScrollbar.getScroll() : 0);
                    int ignoreTopMarginCount = 1;
                    if (renderedConfigElements.size() > 1 && renderedConfigElements.get(0) instanceof ConfigCategoryHeader) {
                        ignoreTopMarginCount++;
                    }
                    for (final ConfigGuiElement configGuiElement : renderedConfigElements) {
                        int tY = drawY;
                        if (ignoreTopMarginCount > 0) ignoreTopMarginCount--;
                        else tY += configGuiElement.getTopMargin();
                        final int elementHeight = configGuiElement.getHeight();
                        if ((tY <= CONFIG_BOTTOM && (tY + elementHeight) >= CONFIG_TOP)
                                && configGuiElement.mouseClicked(mouseX, mouseY, mouseButton)
                        ) {
                            if (configGuiElement instanceof IOverlayHost) {
                                final IOverlayHost overlayHost = (IOverlayHost) configGuiElement;
                                if (overlayHost.isOverlayOpen()) { this.openOverlay = overlayHost; }
                                //else if (overlayHost == overlayToClose) { overlayToClose = null; }
                            }
                            else if (configGuiElement instanceof ConfigSliderButton) {
                                this.activeSlider = (ConfigSliderButton) configGuiElement;
                            }
                            break scan;
                        }
                        drawY = tY + elementHeight + configGuiElement.getBottomMargin();
                    }
                }
                catch (Exception e) {
                    throw new MoreMWEException(e);
                }
            }
            else if ((menuScrollbar != null && menuScrollbar.mouseClicked(mouseX, mouseY, mouseButton)) ||
                    (configScrollbar != null && configScrollbar.mouseClicked(mouseX, mouseY, mouseButton))) {
                this.activeSlider = null;
                break scan;
            }
            else if (searchbar != null && searchbar.mouseClicked(mouseX, mouseY, mouseButton)) {
                needToUnfocusSearchbar = false;
                if (!searchbar.isExpanded() && isInSearchMode())
                    updateRenderedElementsFromSearch("");
                break scan;
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (needToUnfocusSearchbar) {
            searchbar.unfocus();
        }

        if (overlayToClose != null && overlayToClose != openOverlay) {
            overlayToClose.closeOverlay();
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (openOverlay != null) {
            final boolean handledByOverlay = openOverlay.mouseReleasedOverlay(mouseX, mouseY, state);
            if (!openOverlay.isOverlayOpen()) this.openOverlay = null;
            if (handledByOverlay) return;
        }
        if (activeSlider != null && activeSlider.mouseReleased(mouseX, mouseY, state)) {
            return; // maybe not?
        }
        if (menuScrollbar != null) menuScrollbar.mouseReleased(mouseX, mouseY, state);
        if (configScrollbar != null) configScrollbar.mouseReleased(mouseX, mouseY, state);
        //if (currentlyExpandedSelector != null) currentlyExpandedSelector.mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (openOverlay != null) {
            final boolean handledByOverlay = openOverlay.overlayKeyTyped(typedChar, keyCode);
            if (!openOverlay.isOverlayOpen()) this.openOverlay = null;
            if (handledByOverlay) return;
        }
        // prioritize search's left and right key inputs
        if (searchbar != null) {
            final String previousSearch = searchbar.getText();
            if (searchbar.keyTyped(typedChar, keyCode)) {
                if (!previousSearch.equals(searchbar.getText())) {
                    if (searchbarTask != null) searchbarTask.stop();
                    this.searchbarTask = new DelayedTask(() -> {
                        updateRenderedElementsFromSearch(searchbar.getText());
                        this.searchbarTask = null;
                    }, 8);
                }
                return;
            }
        }
        if (activeSlider != null && (keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT)) {
            activeSlider.updateSliderFromIncrement(keyCode == Keyboard.KEY_LEFT ? -1 : 1);
            GuiUtil.playButtonPressSound(mc);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (openOverlay != null) {
            final boolean handledByOverlay = openOverlay.handleOverlayMouseInput();
            if (!openOverlay.isOverlayOpen()) this.openOverlay = null;
            if (handledByOverlay) return;
        }
        final int i = Mouse.getEventDWheel();
        if (i == 0) return;
        if (menuScrollbar != null) {
            final int mouseX = (int) (Mouse.getEventX() / SCREEN_WIDTH_SCALE);
            final int mouseY = (int) (this.height - Mouse.getEventY() / SCREEN_HEIGHT_SCALE - 1);
            if (GuiUtil.isMouseInRect(mouseX, mouseY, MENU_LEFT, MENU_TOP, MENU_RIGHT, MENU_BOTTOM)) {
                menuScrollbar.updateFromMouseScroll(i);
                return;
            }
        }
        if (configScrollbar != null) {
            configScrollbar.updateFromMouseScroll(i);
            closeOpenOverlay();
            clearActiveSlider();
        }
    }

    @Override
    public void onGuiClosed() {
        if (this.searchbarTask != null) {
            searchbarTask.stop();
            this.searchbarTask = null;
        }
        //closeOpenOverlay(); // might be needed later on
        clearActiveSlider(); // saves slider value (potentially unsaved if screen closed without mouseRelease)
        this.configHandler.save();
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void onDependencyDisplayTypeChange(boolean newType) {
        if (this.hidePropertyIfHasDisabledDependency == newType) return;
        this.hidePropertyIfHasDisabledDependency = newType;
        if (hidePropertyIfHasDisabledDependency) {
            resizeRenderedElements(everyElementThatShouldBeRendered);
        }
        else if (renderedConfigElements.size() != everyElementThatShouldBeRendered.size()) {
            renderedConfigElements.clear();
            renderedConfigElements.addAll(everyElementThatShouldBeRendered);
            int renderedElementsHeight = 0;
            for (final ConfigGuiElement element : renderedConfigElements) {
                renderedElementsHeight += getEntireElementHeight(element);
            }
            updateConfigScrollbar(renderedElementsHeight, configScrollbar);
        }
    }

    public void updateDependantsDisplay(ConfigFieldContainer dependency) {
        if (!hidePropertyIfHasDisabledDependency) return;
        try {
            if (!dependency.getBoolean()) {
                resizeRenderedElements(renderedConfigElements);
                return;
            }
            if (renderedConfigElements.size() == everyElementThatShouldBeRendered.size()) return;
            resizeRenderedElements(everyElementThatShouldBeRendered);
        }
        catch (Exception e) { throw new MoreMWEException(e); }
    }

    public void setSelectedCategory(String selectedCategory) {
        if (selectedCategory == null || selectedCategory.equals(this.selectedCategory)) return;
        this.selectedCategory = selectedCategory;
        closeOpenOverlay(); // doesn't hurt
        clearActiveSlider(); // better safe than sorry
        int renderedElementsHeight = 0;
        this.everyElementThatShouldBeRendered.clear();
        for (final ConfigGuiElement configGuiElement : configElements) {
            if (configGuiElement.getCategory().equals(selectedCategory)) {
                this.everyElementThatShouldBeRendered.add(configGuiElement);
                renderedElementsHeight += getEntireElementHeight(configGuiElement);
            }
        }

        if (hidePropertyIfHasDisabledDependency) {
            this.configScrollbar = null;
            resizeRenderedElements(everyElementThatShouldBeRendered);
        }
        else {
            this.renderedConfigElements.clear();
            this.renderedConfigElements.addAll(this.everyElementThatShouldBeRendered);
            updateConfigScrollbar(renderedElementsHeight, null);
        }
    }

    private void updateRenderedElementsFromSearch(String search) {
        search = StringUtil.toLowerCase(search);
        String lastSubcategoryName = "";
        int renderedElementsHeight = 0;
        final int configElementsRectWidth = getConfigElementsRectWidth();
        this.selectedCategory = null;
        closeOpenOverlay();
        clearActiveSlider();
        everyElementThatShouldBeRendered.clear();
        for (final ConfigGuiElement element : configElements) {
            if (element.matchSearch(search)) {
                final String parentsKey = element.getCategory() + ',' + element.getSubcategory();
                if (!lastSubcategoryName.equals(parentsKey)) {
                    final ConfigGuiElement subcategoryHeader = new ConfigSubcategoryHeader.WithCategory(element.getCategory(), StringUtil.defaultIfEmpty(element.getSubcategory(), "General"));
                    subcategoryHeader.onResize(configElementsRectWidth);
                    renderedElementsHeight += getEntireElementHeight(subcategoryHeader);
                    everyElementThatShouldBeRendered.add(subcategoryHeader);
                    lastSubcategoryName = parentsKey;
                }
                renderedElementsHeight += getEntireElementHeight(element);
                everyElementThatShouldBeRendered.add(element);
            }
        }

        if (everyElementThatShouldBeRendered.isEmpty()) {
            final ConfigGuiElement header = new ConfigSubcategoryHeader(null, "Nothing was found!");
            header.onResize(configElementsRectWidth);
            renderedElementsHeight = getEntireElementHeight(header);
            everyElementThatShouldBeRendered.add(header);
        }
        else if (hidePropertyIfHasDisabledDependency) {
            this.configScrollbar = null;
            resizeRenderedElements(everyElementThatShouldBeRendered);
            return;
        }
        renderedConfigElements.clear();
        renderedConfigElements.addAll(everyElementThatShouldBeRendered);
        updateConfigScrollbar(renderedElementsHeight, null);
    }

    private void updateConfigScrollbar(int renderedElementsHeight, Scrollbar previousConfigScrollbar) {
        if (!renderedConfigElements.isEmpty()) {
            int emptyElementsMargin = renderedConfigElements.get(0).getTopMargin() + renderedConfigElements.get(renderedConfigElements.size()-1).getBottomMargin();
            if (renderedConfigElements.size() > 1 && renderedConfigElements.get(0) instanceof ConfigCategoryHeader) {
                emptyElementsMargin += renderedConfigElements.get(1).getTopMargin();
            }
            renderedElementsHeight += CONFIG_VERTICAL_PADDING * 2 - emptyElementsMargin;
        }
        this.configScrollbar = Scrollbar.create(renderedElementsHeight, CONFIG_TOP, INNER_BOTTOM, previousConfigScrollbar);
        this.CONFIG_BOTTOM = this.configScrollbar == null ? (CONFIG_TOP + renderedElementsHeight) : configScrollbar.getTrackBottom();
    }

    private int getConfigElementsRectWidth() { return CONFIG_RIGHT - CONFIG_HORIZONTAL_PADDING - (CONFIG_LEFT + CONFIG_HORIZONTAL_PADDING); }
    private int getEntireElementHeight(ConfigGuiElement element) {
        return element.getTopMargin() + element.getHeight() + element.getBottomMargin();
    }

    private void resizeRenderedElements(List<ConfigGuiElement> resizeFrom) {
        int renderedElementsHeight = 0;
        ConfigGuiElement subcategoryHeader = null;
        final List<ConfigGuiElement> filtered = new ArrayList<>(resizeFrom.size());
        for (final ConfigGuiElement element : resizeFrom) {
            if (element.isHidden()) continue;
            if (element instanceof ConfigSubcategoryHeader) {
                subcategoryHeader = element;
                continue;
            }
            if (subcategoryHeader != null) {
                filtered.add(subcategoryHeader);
                renderedElementsHeight += getEntireElementHeight(subcategoryHeader);
                subcategoryHeader = null;
            }
            filtered.add(element);
            renderedElementsHeight += getEntireElementHeight(element);
        }
        this.renderedConfigElements.clear();
        this.renderedConfigElements.addAll(filtered);
        updateConfigScrollbar(renderedElementsHeight, configScrollbar);
    }

    /** Releases the active slider; saves its value to field and clears it. (set to null) */
    private void clearActiveSlider() {
        if (activeSlider != null) {
            activeSlider.release();
            this.activeSlider = null;
        }
    }

    private void closeOpenOverlay() {
        if (openOverlay != null) {
            openOverlay.closeOverlay();
            this.openOverlay = null;
        }
    }

    public String getSelectedCategory() { return selectedCategory; }

    public boolean isInSearchMode() { return searchbar != null && selectedCategory == null; }
}
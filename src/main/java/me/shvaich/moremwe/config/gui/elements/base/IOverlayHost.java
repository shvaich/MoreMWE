package me.shvaich.moremwe.config.gui.elements.base;

import java.util.List;

public interface IOverlayHost {

    boolean isOverlayOpen();

    void closeOverlay();

    boolean isMouseOverOverlay(int mouseX, int mouseY);

    void drawOverlay(int mouseX, int mouseY);

    default List<String> getOverlayHoveringTextLines() { return null; }

    /** @return {@code true} if the overlay handles the mouse click */
    boolean mouseClickedOverlay(int mouseX, int mouseY, int mouseButton) throws IllegalAccessException;

    /** @return {@code true} if the overlay handles the mouse release */
    default boolean mouseReleasedOverlay(int mouseX, int mouseY, int mouseButton) { return false; }

    /** @return {@code true} if the overlay handles the mouse input */
    default boolean handleOverlayMouseInput() { return false; }

    /** @return {@code true} if the overlay handles the key typed */
    default boolean overlayKeyTyped(char typedChar, int keyCode) { return false; }
}

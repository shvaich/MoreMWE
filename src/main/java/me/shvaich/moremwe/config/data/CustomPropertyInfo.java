package me.shvaich.moremwe.config.data;

import me.shvaich.moremwe.config.gui.elements.base.ConfigGuiElement;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;

public abstract class CustomPropertyInfo {
    public abstract ConfigGuiElement[] getConfigGuiButtons(MyConfigGuiScreen screen, ConfigFieldContainer fieldData);
}

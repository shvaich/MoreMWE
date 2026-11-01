package me.shvaich.moremwe.features;

public interface GuiScreenAllowsPlayerKeyInput {
    default boolean allowsPlayerKeyInput() { return true; }
}

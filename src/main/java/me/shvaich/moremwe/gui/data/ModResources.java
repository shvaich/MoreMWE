package me.shvaich.moremwe.gui.data;

import me.shvaich.moremwe.MoreMWE;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;

public class ModResources {
    public static final int WHITE = Color.WHITE.getRGB();
    public static final int BLACK = Color.BLACK.getRGB();

    public static final int GRAYISH = new Color(0xFFB2B2B2).getRGB();

    public static final int COMMENT_COLOR = new Color(0xFF999999).getRGB();

    public static final int SELECTED_CATEGORY_COLOR = new Color(0xFF4A84E6).getRGB();

    public static final int CONFIG_BACKGROUND = new Color(0xFF242424).getRGB();
    public static final int CONFIG_BORDER = new Color(0xFF363636).getRGB();

    public static final int CONFIG_HEADER_BACKGROUND = new Color(0xFF2A2A2A).getRGB(); // -14408668
    public static final int CONFIG_HEADER_BORDER = new Color(0xFF424242).getRGB();

    public static final int SCROLLBAR_COLOR = new Color(0xFF747474).getRGB();

    public static final int ELEMENT_BACKGROUND = new Color(0xFF2F2F2F).getRGB();
    public static final int ELEMENT_BORDER = new Color(0xFF525252).getRGB();

    public static final int BUTTON_BACKGROUND = new Color(0xFF3A3A3A).getRGB();
    public static final int BUTTON_BORDER = new Color(0xFF5F5F5F).getRGB();

    public static final ResourceLocation SEARCH_ICON = new ResourceLocation(MoreMWE.MOD_ID, "search.png");
    public static final ResourceLocation CANCEL_ICON = new ResourceLocation(MoreMWE.MOD_ID, "cancel.png");
    public static final ResourceLocation CHECKMARK = new ResourceLocation(MoreMWE.MOD_ID, "checkmark.png");
    public static final ResourceLocation ARROW_UP = new ResourceLocation(MoreMWE.MOD_ID, "arrow_up.png");
    public static final ResourceLocation ARROW_DOWN = new ResourceLocation(MoreMWE.MOD_ID, "arrow_down.png");
    public static final ResourceLocation RELOAD_ICON = new ResourceLocation(MoreMWE.MOD_ID, "reload.png");
    public static final ResourceLocation UNDO_ICON = new ResourceLocation(MoreMWE.MOD_ID, "undo.png");
    public static final ResourceLocation MOVE_ICON = new ResourceLocation(MoreMWE.MOD_ID, "move.png");
}

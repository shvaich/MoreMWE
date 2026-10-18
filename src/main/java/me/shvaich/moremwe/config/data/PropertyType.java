package me.shvaich.moremwe.config.data;

import me.shvaich.moremwe.gui.data.MyRendererPosition;
import net.minecraft.util.EnumChatFormatting;

import java.lang.reflect.Field;

public enum PropertyType {

    SWITCH(Boolean.TYPE),
    COLOR(Integer.TYPE),
    SELECTOR(Integer.TYPE),
    SLIDER(Integer.TYPE),
    NUMBER(Integer.TYPE),
    HUD_POSITION(MyRendererPosition.class),
    MINECRAFT_COLOR(EnumChatFormatting.class),
    ENUM_SELECTOR { // in future this shouldn't exist. (SELECTOR should support enums)
        @Override
        public boolean isFieldValid(Field field) {
            return field.getType().isEnum();
        }
    },
    CUSTOM {
        @Override
        public boolean isFieldValid(Field field) {
            final ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
            return annotation.customPropertyClass() != CustomPropertyInfo.class;
        }
    };

    private final Class<?> type;

    PropertyType() { this.type = null; }

    PropertyType(Class<?> type) {
        this.type = type;
    }

    public boolean isFieldValid(Field field) {
        return field.getType() == this.type;
    }
}

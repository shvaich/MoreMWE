package me.shvaich.moremwe.gui.data;

import me.shvaich.moremwe.utils.GuiUtil;
import net.minecraft.client.Minecraft;

public abstract class AbstractRenderer {
    protected final MyRendererPosition rendererPosition;

    public AbstractRenderer(MyRendererPosition rendererPosition) {
        this.rendererPosition = rendererPosition;
    }

    public MyRendererPosition getPosition() {
        return rendererPosition;
    }

    public abstract boolean isEnabled(Minecraft mc, long currentTimeMillis);

    public abstract void render(Minecraft mc, int screenWidth, int screenHeight, int thisWidth, int thisHeight);

    public abstract int getWidth(Minecraft mc);

    public abstract int getHeight(Minecraft mc);

    public boolean shouldRenderDummy() { return rendererPosition.isEnabled(); }

    public abstract void renderDummy(Minecraft mc, int screenWidth, int screenHeight, int thisWidth, int thisHeight);

    public int getDummyWidth(Minecraft mc) { return getWidth(mc); }

    public int getDummyHeight(Minecraft mc) { return getHeight(mc); }

    protected void drawBackground(int x, int y, int thisWidth, int thisHeight, int backgroundColor, int padding) {
        GuiUtil.drawRect(x - padding, y - padding, x + thisWidth + padding, y + thisHeight + padding, backgroundColor);
    }

    protected void drawBackground(int x, int y, int thisWidth, int thisHeight, int backgroundColor) { drawBackground(x, y, thisWidth, thisHeight, backgroundColor, 2); }
}

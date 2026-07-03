package me.shvaich.moremwe.gui.data;

public abstract class AbstractRenderer {
    protected final MyRendererPosition rendererPosition;

    public AbstractRenderer(MyRendererPosition rendererPosition) {
        this.rendererPosition = rendererPosition;
    }

    public MyRendererPosition getPosition() {
        return rendererPosition;
    }

    public abstract boolean isEnabled(long currentTimeMillis);

    public abstract void render(int screenWidth, int screenHeight, int thisWidth, int thisHeight);

    public abstract int getWidth();

    public abstract int getHeight();

    public boolean shouldRenderDummy() { return rendererPosition.isEnabled(); }

    public abstract void renderDummy(int screenWidth, int screenHeight, int thisWidth, int thisHeight);

    public int getDummyWidth() { return getWidth(); }

    public int getDummyHeight() { return getHeight(); }
}

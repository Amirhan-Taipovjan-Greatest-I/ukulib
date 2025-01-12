package net.uku3lig.ukulib.utils;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.util.Identifier;

/**
 * A simple button with an icon. Works best when the button is square.
 */
public class IconButton extends TexturedButtonWidget {
    /**
     * Makes a new button with an icon.
     * @param x The x position of the button
     * @param y The y position of the button
     * @param width The width of the button
     * @param height The height of the button
     * @param u The x position of the icon in the texture
     * @param v The y position of the icon in the texture
     * @param hoveredVOffset The offset of the icon when hovered
     * @param texture The texture of the icon
     * @param textureWidth The width of the texture
     * @param textureHeight The height of the texture
     * @param pressAction The action to perform when the button is pressed
     */
    public IconButton(int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight, PressAction pressAction) {
        super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction);
    }

    @Override
    public void renderButton(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.drawNineSlicedTexture(WIDGETS_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
        super.renderButton(drawContext, mouseX, mouseY, delta);
    }

    /**
     * PressableWidget#getTextureY()
     */
    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isSelected()) {
            i = 2;
        }

        return 46 + i * 20;
    }
}

package com.demonicrous.furusato.core.asm;

import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import org.lwjgl.opengl.GL11;

/** Draws a softly animated progress bar in the shared Forge splash context. */
public final class FurusatoSplashRenderer {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 8;
    private static ProgressBar activeBar;
    private static float displayedProgress;

    private FurusatoSplashRenderer() {
    }

    public static void draw(ProgressBar bar) {
        if (bar != activeBar) {
            activeBar = bar;
            displayedProgress = 0.0F;
        }

        float target = Math.min(1.0F, (bar.getStep() + 1.0F) / (bar.getSteps() + 1.0F));
        displayedProgress += (target - displayedProgress) * 0.09F;
        if (Math.abs(target - displayedProgress) < 0.001F) {
            displayedProgress = target;
        }

        drawBox(0, 0, WIDTH, HEIGHT, 0xFF5F6964);
        drawBox(1, 1, WIDTH - 2, HEIGHT - 2, 0xFFD8DDD8);
        int fillWidth = Math.max(0, Math.min(WIDTH - 2,
                Math.round((WIDTH - 2) * displayedProgress)));
        drawBox(1, 1, fillWidth, HEIGHT - 2, 0xFF71957F);
    }

    private static void drawBox(int x, int y, int width, int height, int color) {
        float alpha = ((color >>> 24) & 255) / 255.0F;
        float red = ((color >>> 16) & 255) / 255.0F;
        float green = ((color >>> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
    }
}

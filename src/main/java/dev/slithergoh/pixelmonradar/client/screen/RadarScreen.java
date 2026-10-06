package dev.slithergoh.pixelmonradar.client.screen;

import dev.slithergoh.pixelmonradar.client.RadarSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RadarScreen extends Screen {
    public RadarScreen() { super(Component.literal("Pixelmon Radar")); }

    @Override protected void init() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(Component.literal("HUD: " + onOff(RadarSettings.hudEnabled)), b -> {
            RadarSettings.hudEnabled = !RadarSettings.hudEnabled;
            b.setMessage(Component.literal("HUD: " + onOff(RadarSettings.hudEnabled)));
        }).bounds(cx - 100, 55, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Shiny only: " + onOff(RadarSettings.shinyOnly)), b -> {
            RadarSettings.shinyOnly = !RadarSettings.shinyOnly;
            b.setMessage(Component.literal("Shiny only: " + onOff(RadarSettings.shinyOnly)));
        }).bounds(cx - 100, 80, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Rare only: " + onOff(RadarSettings.rareOnly)), b -> {
            RadarSettings.rareOnly = !RadarSettings.rareOnly;
            b.setMessage(Component.literal("Rare only: " + onOff(RadarSettings.rareOnly)));
        }).bounds(cx - 100, 105, 200, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Pokemon range: " + RadarSettings.pokemonRange), b -> {
            RadarSettings.pokemonRange = RadarSettings.pokemonRange >= 256 ? 32 : RadarSettings.pokemonRange + 32;
            b.setMessage(Component.literal("Pokemon range: " + RadarSettings.pokemonRange));
        }).bounds(cx - 100, 130, 200, 20).build());
    }

    private static String onOff(boolean v) { return v ? "ON" : "OFF"; }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 25, 0xFFFFFF);
        g.drawCenteredString(font, "Core controls - Pokemon list/filters are added next", width / 2, 165, 0xAAAAAA);
    }
    @Override public boolean isPauseScreen() { return false; }
}

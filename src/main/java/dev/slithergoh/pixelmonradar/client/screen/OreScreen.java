package dev.slithergoh.pixelmonradar.client.screen;

import dev.slithergoh.pixelmonradar.client.RadarSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class OreScreen extends Screen {
    public OreScreen() { super(Component.literal("Ore Scanner")); }
    @Override protected void init() {
        int cx = width / 2;
        addRenderableWidget(Button.builder(Component.literal("Through-wall highlights: " + onOff(RadarSettings.oreEspEnabled)), b -> {
            RadarSettings.oreEspEnabled = !RadarSettings.oreEspEnabled;
            b.setMessage(Component.literal("Through-wall highlights: " + onOff(RadarSettings.oreEspEnabled)));
        }).bounds(cx - 110, 55, 220, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Scan range: " + RadarSettings.oreRange), b -> {
            RadarSettings.oreRange = RadarSettings.oreRange >= 64 ? 8 : RadarSettings.oreRange + 8;
            b.setMessage(Component.literal("Scan range: " + RadarSettings.oreRange));
        }).bounds(cx - 110, 80, 220, 20).build());
    }
    private static String onOff(boolean v) { return v ? "ON" : "OFF"; }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        super.render(g, mx, my, pt);
        g.drawCenteredString(font, title, width / 2, 25, 0xFFFFFF);
        g.drawCenteredString(font, "Registry ore selector is added after this client foundation compiles", width / 2, 120, 0xAAAAAA);
    }
    @Override public boolean isPauseScreen() { return false; }
}

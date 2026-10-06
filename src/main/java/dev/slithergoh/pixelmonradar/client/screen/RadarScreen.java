package dev.slithergoh.pixelmonradar.client.screen;

import dev.slithergoh.pixelmonradar.client.PokemonInfo;
import dev.slithergoh.pixelmonradar.client.PokemonScanner;
import dev.slithergoh.pixelmonradar.client.RadarSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RadarScreen extends Screen {
    private EditBox search;
    private List<PokemonInfo> visible = List.of();
    public RadarScreen() { super(Component.literal("Pixelmon Radar")); }

    @Override protected void init() {
        int left = width / 2 - 190;
        search = new EditBox(font, left, 42, 210, 20, Component.literal("Search Pokemon"));
        search.setHint(Component.literal("Search Pokemon..."));
        search.setValue(RadarSettings.search);
        search.setResponder(s -> RadarSettings.search = s.trim());
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.literal("Sort: " + RadarSettings.sortMode), b -> {
            RadarSettings.SortMode[] modes = RadarSettings.SortMode.values();
            RadarSettings.sortMode = modes[(RadarSettings.sortMode.ordinal() + 1) % modes.length];
            b.setMessage(Component.literal("Sort: " + RadarSettings.sortMode));
        }).bounds(left + 220, 42, 160, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Shiny: " + onOff(RadarSettings.shinyOnly)), b -> {
            RadarSettings.shinyOnly = !RadarSettings.shinyOnly;
            b.setMessage(Component.literal("Shiny: " + onOff(RadarSettings.shinyOnly)));
        }).bounds(left, 67, 115, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Rare: " + onOff(RadarSettings.rareOnly)), b -> {
            RadarSettings.rareOnly = !RadarSettings.rareOnly;
            b.setMessage(Component.literal("Rare: " + onOff(RadarSettings.rareOnly)));
        }).bounds(left + 120, 67, 115, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Range: " + RadarSettings.pokemonRange), b -> {
            RadarSettings.pokemonRange = RadarSettings.pokemonRange >= 256 ? 32 : RadarSettings.pokemonRange + 32;
            b.setMessage(Component.literal("Range: " + RadarSettings.pokemonRange));
        }).bounds(left + 240, 67, 140, 20).build());
    }

    private static String onOff(boolean v) { return v ? "ON" : "OFF"; }
    private void refresh() {
        ArrayList<PokemonInfo> list = new ArrayList<>(PokemonScanner.scan());
        String q = RadarSettings.search.toLowerCase();
        if (!q.isBlank()) list.removeIf(p -> !p.name().toLowerCase().contains(q));
        Comparator<PokemonInfo> c = switch (RadarSettings.sortMode) {
            case LEVEL -> Comparator.comparingInt(PokemonInfo::level).reversed();
            case NAME -> Comparator.comparing(PokemonInfo::name, String.CASE_INSENSITIVE_ORDER);
            case RARITY -> Comparator.comparing(PokemonInfo::rare).reversed().thenComparingDouble(PokemonInfo::distance);
            default -> Comparator.comparingDouble(PokemonInfo::distance);
        };
        list.sort(c);
        visible = list;
    }

    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        refresh();
        int left = width / 2 - 190;
        g.fill(left - 8, 18, left + 388, Math.min(height - 18, 112 + visible.size() * 19), 0xB0121218);
        g.drawCenteredString(font, title, width / 2, 24, 0xFFFFFF);
        super.render(g, mx, my, pt);
        int y = 99;
        int max = Math.min(visible.size(), Math.max(1, (height - 125) / 19));
        for (int i = 0; i < max; i++) {
            PokemonInfo p = visible.get(i);
            boolean tracked = p.entityId() == RadarSettings.trackedEntityId;
            int bg = tracked ? 0x80408040 : 0x50303038;
            g.fill(left, y - 2, left + 380, y + 15, bg);
            int color = p.shiny() ? 0x55FFFF : p.rare() ? 0xFFD75A : 0xFFFFFF;
            String text = p.name() + "  Lv." + p.level() + "  " + Math.round(p.distance()) + "m  [" + p.pos().getX()+","+p.pos().getY()+","+p.pos().getZ()+"]" + p.badges();
            g.drawString(font, text, left + 5, y + 2, color, true);
            y += 19;
        }
        g.drawString(font, "Click a Pokemon row to track it", left, height - 15, 0xAAAAAA, false);
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        int left = width / 2 - 190;
        int y = 99;
        int max = Math.min(visible.size(), Math.max(1, (height - 125) / 19));
        if (mx >= left && mx <= left + 380) {
            for (int i = 0; i < max; i++, y += 19) {
                if (my >= y - 2 && my <= y + 15) {
                    int id = visible.get(i).entityId();
                    RadarSettings.trackedEntityId = RadarSettings.trackedEntityId == id ? -1 : id;
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
    @Override public boolean isPauseScreen() { return false; }
}

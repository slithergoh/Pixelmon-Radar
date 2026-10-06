package dev.slithergoh.pixelmonradar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;

@EventBusSubscriber(modid = "pixelmonradar", value = Dist.CLIENT)
public final class RadarHud {
    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        if (!RadarSettings.hudEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;
        List<PokemonInfo> pokemon = PokemonScanner.scan();
        GuiGraphics g = event.getGuiGraphics();
        int x = 7, y = 7;
        int shown = Math.min(10, pokemon.size());
        int h = 18 + shown * 12;
        g.fill(x - 3, y - 3, x + 255, y + h, 0xA0101010);
        g.drawString(mc.font, "Pixelmon Radar  [" + pokemon.size() + "]", x, y, 0xFFFFFF, true);
        y += 15;
        for (int i = 0; i < shown; i++) {
            PokemonInfo p = pokemon.get(i);
            int color = p.shiny() ? 0x55FFFF : p.rare() ? 0xFFD75A : 0xE8E8E8;
            String line = p.name() + " Lv." + p.level() + "  " + Math.round(p.distance()) + "m" + p.badges();
            g.drawString(mc.font, line, x, y, color, true);
            y += 12;
        }
    }
}

package dev.slithergoh.pixelmonradar.client;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PokemonScanner {
    private PokemonScanner() {}

    public static List<PokemonInfo> scan() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return List.of();
        double r = RadarSettings.pokemonRange;
        AABB box = mc.player.getBoundingBox().inflate(r);
        List<PokemonInfo> out = new ArrayList<>();
        for (PixelmonEntity entity : mc.level.getEntitiesOfClass(PixelmonEntity.class, box)) {
            Pokemon p = entity.getPokemon();
            if (p == null) continue;
            boolean shiny = p.isShiny();
            boolean legendary = p.isLegendary(true);
            boolean mythical = p.isMythical();
            boolean ub = p.isUltraBeast();
            boolean paradox = p.isParadox();
            boolean boss = entity.isBossPokemon();
            PokemonInfo info = new PokemonInfo(entity.getId(), entity.getLocalizedName(), p.getPokemonLevel(),
                    mc.player.distanceTo(entity), entity.blockPosition(), shiny, legendary, mythical, ub, paradox,
                    boss, String.valueOf(p.getGender()), p.getFormName(), p.getPaletteName(),
                    boss && entity.getBossTier() != null ? String.valueOf(entity.getBossTier()) : "");
            if (RadarSettings.shinyOnly && !shiny) continue;
            if (RadarSettings.rareOnly && !info.rare()) continue;
            out.add(info);
        }
        out.sort(Comparator.comparingDouble(PokemonInfo::distance));
        return out;
    }
}

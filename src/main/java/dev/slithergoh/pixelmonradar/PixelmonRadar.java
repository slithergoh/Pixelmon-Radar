package dev.slithergoh.pixelmonradar;

import dev.slithergoh.pixelmonradar.client.RadarSettings;
import net.neoforged.fml.common.Mod;

@Mod(PixelmonRadar.MOD_ID)
public final class PixelmonRadar { public static final String MOD_ID="pixelmonradar"; public PixelmonRadar(){RadarSettings.load();Runtime.getRuntime().addShutdownHook(new Thread(RadarSettings::save,"PixelmonRadar-Save"));} }

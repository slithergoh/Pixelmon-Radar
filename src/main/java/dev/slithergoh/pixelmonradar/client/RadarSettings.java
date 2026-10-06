package dev.slithergoh.pixelmonradar.client;

import java.util.LinkedHashSet;
import java.util.Set;

public final class RadarSettings {
    private RadarSettings() {}
    public static boolean hudEnabled = true;
    public static boolean oreEspEnabled = false;
    public static int pokemonRange = 128;
    public static int oreRange = 24;
    public static boolean shinyOnly = false;
    public static boolean rareOnly = false;
    public static final Set<String> selectedOres = new LinkedHashSet<>();
}

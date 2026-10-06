package dev.slithergoh.pixelmonradar.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public final class RadarSettings {
 private RadarSettings(){} private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create(); private static final Path FILE=FMLPaths.CONFIGDIR.get().resolve("pixelmonradar.json");
 public static boolean hudEnabled=true,oreEspEnabled=false,shinyOnly=false,rareOnly=false,alertsEnabled=true,alertSound=true;
 public static int pokemonRange=128,oreRange=24,trackedEntityId=-1; public static String search=""; public static SortMode sortMode=SortMode.DISTANCE; public static final Set<String> selectedOres=new LinkedHashSet<>();
 public enum SortMode{DISTANCE,LEVEL,NAME,RARITY}
 private record Data(boolean hudEnabled,boolean oreEspEnabled,int pokemonRange,int oreRange,boolean shinyOnly,boolean rareOnly,boolean alertsEnabled,boolean alertSound,SortMode sortMode,Set<String> selectedOres){}
 public static void load(){try{if(!Files.exists(FILE))return;Data d=GSON.fromJson(Files.readString(FILE),Data.class);if(d==null)return;hudEnabled=d.hudEnabled;oreEspEnabled=d.oreEspEnabled;pokemonRange=d.pokemonRange;oreRange=d.oreRange;shinyOnly=d.shinyOnly;rareOnly=d.rareOnly;alertsEnabled=d.alertsEnabled;alertSound=d.alertSound;sortMode=d.sortMode==null?SortMode.DISTANCE:d.sortMode;selectedOres.clear();if(d.selectedOres!=null)selectedOres.addAll(d.selectedOres);}catch(Exception ignored){}}
 public static void save(){try{Files.createDirectories(FILE.getParent());Files.writeString(FILE,GSON.toJson(new Data(hudEnabled,oreEspEnabled,pokemonRange,oreRange,shinyOnly,rareOnly,alertsEnabled,alertSound,sortMode,new LinkedHashSet<>(selectedOres))));}catch(Exception ignored){}}
}

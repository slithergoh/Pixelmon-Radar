package dev.slithergoh.atlascobblemon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;
public final class AtlasSettings {
 private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
 private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("atlas-cobblemon.json");
 public boolean partyHud=true,hud=true,esp=false,alerts=true,alertSound=true,hideOwned=true,shinyOnly=false,rareOnly=false,legendaryOnly=false,mythicalOnly=false,showOwned=false;
 public String sortMode="DISTANCE";\n public int pokemonRange=128,blockRange=20,maxMarkers=24,hudX=7,hudY=7,trackerX=7,trackerY=160,partyX=7,partyY=215,trackedId=-1;
 public Set<String> targets=new LinkedHashSet<>(),blocks=new LinkedHashSet<>();
 public static AtlasSettings INSTANCE=new AtlasSettings();
 public static void load(){try{if(Files.exists(FILE)){AtlasSettings s=JSON.fromJson(Files.readString(FILE),AtlasSettings.class);if(s!=null)INSTANCE=s;}}catch(Exception ignored){} if(INSTANCE.targets==null)INSTANCE.targets=new LinkedHashSet<>();if(INSTANCE.blocks==null)INSTANCE.blocks=new LinkedHashSet<>();}
 public static void save(){try{Files.createDirectories(FILE.getParent());Files.writeString(FILE,JSON.toJson(INSTANCE));}catch(Exception ignored){}}
}

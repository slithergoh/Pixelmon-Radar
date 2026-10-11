package dev.slithergoh.atlascobblemon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import java.lang.reflect.Method;
import java.util.*;
public final class CobblemonScanner {
 public record Pokemon(int id,String name,int level,boolean shiny,boolean legendary,boolean mythical,boolean owned,double distance,BlockPos pos) {
  public boolean rare(){return shiny||legendary||mythical;}
 }
 private static final Set<String> LEGENDARY=Set.of("articuno","zapdos","moltres","mewtwo","raikou","entei","suicune","lugia","hooh","regirock","regice","registeel","latias","latios","kyogre","groudon","rayquaza","uxie","mesprit","azelf","dialga","palkia","giratina","heatran","regigigas","cresselia","cobalion","terrakion","virizion","tornadus","thundurus","reshiram","zekrom","landorus","kyurem","xerneas","yveltal","zygarde","type_null","silvally","tapukoko","tapulele","tapubulu","tapufini","cosmog","cosmoem","solgaleo","lunala","necrozma","zacian","zamazenta","eternatus","kubfu","urshifu","regieleki","regidrago","glastrier","spectrier","calyrex","enamorus","wochien","chienpao","tinglu","chiyu","koraidon","miraidon","okidogi","munkidori","fezandipiti","ogerpon","terapagos");
 private static final Set<String> MYTHICAL=Set.of("mew","celebi","jirachi","deoxys","phione","manaphy","darkrai","shaymin","arceus","victini","keldeo","meloetta","genesect","diancie","hoopa","volcanion","magearna","marshadow","zeraora","meltan","melmetal","zarude","pecharunt");
 private CobblemonScanner(){}
 public static List<Pokemon> scan(boolean wildOnly){
  MinecraftClient mc=MinecraftClient.getInstance();if(mc.world==null||mc.player==null)return List.of();
  var settings=AtlasSettings.INSTANCE;double range=settings.pokemonRange;
  ArrayList<Pokemon> out=new ArrayList<>();
  for(Entity entity:mc.world.getOtherEntities(mc.player,mc.player.getBoundingBox().expand(range),e->e.getClass().getName().equals("com.cobblemon.mod.common.entity.pokemon.PokemonEntity"))){
   Object p=call(entity,"getPokemon");if(p==null)continue;
   Object species=call(p,"getSpecies");
   String name=string(call(species,"getName"),entity.getName().getString());
   int level=number(call(p,"getLevel"),1);
   boolean shiny=bool(call(p,"getShiny"))||bool(call(p,"isShiny"));
   boolean owned=call(p,"getOwnerUUID")!=null||call(p,"getOwnerUuid")!=null||call(p,"getOwner")!=null;
   // Cobblemon wild entities can also have an owner UUID on the entity itself.
   owned|=call(entity,"getOwnerUUID")!=null||call(entity,"getOwnerUuid")!=null||call(entity,"getOwner")!=null;
   if(wildOnly&&owned)continue;
   if(!wildOnly&&settings.hideOwned&&owned)continue;
   String key=name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","");
   boolean legendary=LEGENDARY.contains(key),mythical=MYTHICAL.contains(key);
   if(!wildOnly&&settings.shinyOnly&&!shiny)continue;
   if(!wildOnly&&settings.rareOnly&&!shiny&&!legendary&&!mythical)continue;
   if(!wildOnly&&settings.legendaryOnly&&!legendary)continue;
   if(!wildOnly&&settings.mythicalOnly&&!mythical)continue;
   out.add(new Pokemon(entity.getId(),name,level,shiny,legendary,mythical,owned,mc.player.distanceTo(entity),entity.getBlockPos()));
  }
  out.sort(Comparator.comparingDouble(Pokemon::distance));return out;
 }
 private static Object call(Object obj,String name){if(obj==null)return null;try{Method m=obj.getClass().getMethod(name);return m.invoke(obj);}catch(ReflectiveOperationException|LinkageError e){return null;}}
 private static String string(Object v,String fallback){return v==null?fallback:String.valueOf(v);}
 private static int number(Object v,int fallback){return v instanceof Number n?n.intValue():fallback;}
 private static boolean bool(Object v){return Boolean.TRUE.equals(v);}
}

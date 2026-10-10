package dev.slithergoh.pixelmonradar.client;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import net.minecraft.resources.ResourceLocation;
import java.lang.reflect.*;
import java.util.*;
/** Species browser backed by Pixelmon's loaded species registry, not nearby entities. */
public final class SpeciesCatalog {
 private SpeciesCatalog(){}
 public record Entry(String name,String key,ResourceLocation sprite){}
 private static List<Entry> all;private static final Map<String,Object> speciesByName=new HashMap<>();private static final Map<String,ResourceLocation> spriteCache=new HashMap<>();private static final Set<String> missingSprites=new HashSet<>();
 public static List<Entry> all(){
  if(all!=null)return all;
  List<Entry> out=new ArrayList<>();
  try{
   Class<?> registry=Class.forName("com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies");
   Object species=registry.getMethod("getAll").invoke(null);
   Iterable<?> entries=species instanceof Iterable<?> iterable?iterable:species instanceof Map<?,?> map?map.values():List.of();for(Object s:entries){
    String name=String.valueOf(s.getClass().getMethod("getName").invoke(s));
    if(name.isBlank())continue;
    ResourceLocation sprite=null;
    out.add(new Entry(name,name.toLowerCase(Locale.ROOT),sprite));speciesByName.put(name.toLowerCase(Locale.ROOT),s);
   }
  }catch(ReflectiveOperationException|LinkageError ignored){}
  out.sort(Comparator.comparing(Entry::name,String.CASE_INSENSITIVE_ORDER));
  // Retry if registry is still loading rather than caching an empty catalog.
  if(!out.isEmpty())all=List.copyOf(out);
  return out;
 }
 public static ResourceLocation sprite(Entry entry){if(spriteCache.containsKey(entry.key()))return spriteCache.get(entry.key());if(missingSprites.contains(entry.key()))return null;ResourceLocation value=sprite(speciesByName.get(entry.key()));if(value!=null)spriteCache.put(entry.key(),value);else missingSprites.add(entry.key());return value;}
 private static ResourceLocation sprite(Object species){
  if(!(species instanceof Species s))return null;
  try{
   Pokemon pokemon=PokemonFactory.create(s);
   return pokemon==null?null:pokemon.getSprite();
  }catch(RuntimeException|LinkageError ex){return null;}
 }
 public static List<Entry> search(String text){
  String q=text.trim().toLowerCase(Locale.ROOT);
  List<Entry> result=new ArrayList<>();
  for(Entry e:all())if(q.isEmpty()||e.key().contains(q)){result.add(e);}
  return result;
 }
}
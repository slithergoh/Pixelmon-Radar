package dev.slithergoh.pixelmonradar.client;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.resources.ResourceLocation;
import java.lang.reflect.*;
import java.util.*;
/** Species browser backed by Pixelmon's loaded species registry, not nearby entities. */
public final class SpeciesCatalog {
 private SpeciesCatalog(){}
 public record Entry(String name,String key,ResourceLocation sprite){}
 private static List<Entry> all;
 public static List<Entry> all(){
  if(all!=null)return all;
  List<Entry> out=new ArrayList<>();
  try{
   Class<?> registry=Class.forName("com.pixelmonmod.pixelmon.api.pokemon.species.PokemonSpecies");
   Object species=registry.getMethod("getAll").invoke(null);
   if(species instanceof Iterable<?> entries)for(Object s:entries){
    String name=String.valueOf(s.getClass().getMethod("getName").invoke(s));
    if(name.isBlank())continue;
    ResourceLocation sprite=sprite(s);
    out.add(new Entry(name,name.toLowerCase(Locale.ROOT),sprite));
   }
  }catch(ReflectiveOperationException|LinkageError ignored){}
  out.sort(Comparator.comparing(Entry::name,String.CASE_INSENSITIVE_ORDER));
  // Retry if registry is still loading rather than caching an empty catalog.
  if(!out.isEmpty())all=List.copyOf(out);
  return out;
 }
 private static ResourceLocation sprite(Object species){
  try{
   // Use the sprite supplied by a normal Pixelmon Pokemon instance.
   for(Method factory:species.getClass().getMethods()){
    if(factory.getParameterCount()==0&&Pokemon.class.isAssignableFrom(factory.getReturnType())
       &&(factory.getName().equals("create")||factory.getName().equals("createPokemon"))){
     Object pokemon=factory.invoke(species);
     if(pokemon instanceof Pokemon p)return p.getSprite();
    }
   }
  }catch(ReflectiveOperationException|RuntimeException ignored){}
  return null;
 }
 public static List<Entry> search(String text){
  String q=text.trim().toLowerCase(Locale.ROOT);
  List<Entry> result=new ArrayList<>();
  for(Entry e:all())if(q.isEmpty()||e.key().contains(q)){result.add(e);if(result.size()>=300)break;}
  return result;
 }
}
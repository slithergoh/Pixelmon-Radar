package dev.slithergoh.atlascobblemon;
import java.lang.reflect.*;
import java.util.*;
/** Reads Cobblemon's runtime species registry without depending on a specific pack patch version. */
public final class SpeciesIndex {
 private static List<String> names=List.of();
 private static long lastAttempt;
 private SpeciesIndex(){}
 public static List<String> all(){
  if(!names.isEmpty())return names;
  if(System.currentTimeMillis()-lastAttempt<15000)return names;
  lastAttempt=System.currentTimeMillis();
  TreeSet<String> out=new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
  try{
   Class<?> cls=Class.forName("com.cobblemon.mod.common.pokemon.Species");
   Object registry=field(cls,"SPECIES");if(registry instanceof Map<?,?> m)for(Object v:m.values())add(out,v);
  }catch(Throwable ignored){}
  try{
   Class<?> cls=Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonSpecies");
   Object instance=field(cls,"INSTANCE");if(instance==null)instance=field(cls,"Companion");if(instance==null)instance=field(cls,"INSTANCE");
   for(String method:List.of("getSpecies","getAll","getSpeciesByIdentifier")){
    Object value=invoke(instance,method);if(value==null)try{value=cls.getMethod(method).invoke(null);}catch(Exception ignored){}
    if(value instanceof Map<?,?> map)for(Object v:map.values())add(out,v);
    else if(value instanceof Iterable<?> list)for(Object v:list)add(out,v);
   }
  }catch(Throwable ignored){}
  if(!out.isEmpty())names=List.copyOf(out);
  return names;
 }
 public static List<String> search(String query){
  String q=query.toLowerCase(Locale.ROOT).trim();
  TreeSet<String> result=new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
  result.addAll(all());
  for(var p:AtlasCobblemonClient.visible)result.add(p.name());
  result.addAll(AtlasSettings.INSTANCE.targets);
  return result.stream().filter(s->s.toLowerCase(Locale.ROOT).contains(q)).toList();
 }
 private static void add(Set<String> out,Object obj){Object name=invoke(obj,"getName");if(name instanceof String s&&!s.isBlank())out.add(s);else{Object id=invoke(obj,"getResourceIdentifier");if(id!=null)out.add(String.valueOf(id).replaceFirst("^.*:",""));}}
 private static Object field(Class<?> cls,String name){try{Field f=cls.getField(name);return f.get(null);}catch(Exception ignored){return null;}}
 private static Object invoke(Object obj,String method){if(obj==null)return null;try{return obj.getClass().getMethod(method).invoke(obj);}catch(Exception ignored){return null;}}
}

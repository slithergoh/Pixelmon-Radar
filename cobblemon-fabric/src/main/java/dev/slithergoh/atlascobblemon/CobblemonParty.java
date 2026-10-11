package dev.slithergoh.atlascobblemon;
import java.lang.reflect.*;
import java.util.*;
/** Read-only view of the Cobblemon client party storage. Never mutates the party. */
public final class CobblemonParty {
 public record Slot(String name,int level,boolean shiny,int hp,int maxHp){}
 private CobblemonParty(){}
 public static List<Slot> slots(){
  ArrayList<Slot> out=new ArrayList<>();
  try {
   Class<?> client=Class.forName("com.cobblemon.mod.common.client.CobblemonClient");
   Object singleton=staticField(client,"INSTANCE");if(singleton==null)singleton=staticField(client,"Companion");
   Object storage=call(singleton,"getStorage");if(storage==null)storage=staticField(client,"storage");
   Object party=call(storage,"getMyParty");
   if(party==null)return List.of();
   Object entries=call(party,"getSlots");
   if(!(entries instanceof Iterable<?> list))return List.of();
   for(Object p:list){
    if(p==null)continue;
    Object species=call(p,"getSpecies");Object n=call(species,"getName");
    String name=n==null?"Pokémon":String.valueOf(n);
    int level=num(call(p,"getLevel"),1);
    boolean shiny=Boolean.TRUE.equals(call(p,"getShiny"));
    int hp=num(call(p,"getCurrentHealth"),num(call(p,"getHealth"),0));
    int maxHp=num(call(p,"getMaxHealth"),0);
    out.add(new Slot(name,level,shiny,hp,maxHp));
   }
  }catch(ReflectiveOperationException|LinkageError ignored){}
  return List.copyOf(out);
 }
 private static int num(Object v,int d){return v instanceof Number n?n.intValue():d;}
 private static Object staticField(Class<?> c,String name){try{return c.getField(name).get(null);}catch(Exception ignored){return null;}}
 private static Object call(Object o,String name){if(o==null)return null;try{return o.getClass().getMethod(name).invoke(o);}catch(Exception ignored){return null;}}
}

package dev.slithergoh.pixelmonradar.client;
import com.pixelmonmod.pixelmon.api.extensions.PixelmonPlayerExtension;
import net.minecraft.client.Minecraft;
import java.util.*;
public final class PartySnapshot{
 private PartySnapshot(){}
 public record Member(String name,int level,int hp,int maxHp,boolean fainted,boolean shiny,net.minecraft.resources.ResourceLocation sprite){}
 private static List<Member>cached=List.of();private static long tick=Long.MIN_VALUE;
 public static List<Member>get(){Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return List.of();long t=mc.level.getGameTime();if(tick==t)return cached;ArrayList<Member>out=new ArrayList<>();try{var party=((PixelmonPlayerExtension)mc.player).getPartyNow();if(party!=null)for(var p:party.getAll()){if(p==null)continue;out.add(new Member(p.getDisplayName().getString(),p.getPokemonLevel(),p.getHealth(),p.getMaxHealth(),p.getHealth()<=0,p.isShiny(),p.getSprite()));}}catch(Exception ignored){}cached=List.copyOf(out);tick=t;return cached;}
}
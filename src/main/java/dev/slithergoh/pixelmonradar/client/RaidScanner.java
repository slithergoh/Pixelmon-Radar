package dev.slithergoh.pixelmonradar.client;
import com.pixelmonmod.pixelmon.entities.DenEntity;
import net.minecraft.client.Minecraft;
import java.util.*;
public final class RaidScanner{
 private RaidScanner(){}
 public record RaidInfo(int entityId,String name,int stars,double distance,net.minecraft.core.BlockPos pos,net.minecraft.resources.ResourceLocation sprite){}
 private static volatile List<RaidInfo>cached=List.of();private static long tick=Long.MIN_VALUE;
 public static List<RaidInfo>scan(){Minecraft mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return List.of();long t=mc.level.getGameTime();if(tick==t)return cached;ArrayList<RaidInfo>out=new ArrayList<>();double r=Math.max(128,RadarSettings.pokemonRange*2.0);for(DenEntity den:mc.level.getEntitiesOfClass(DenEntity.class,mc.player.getBoundingBox().inflate(r))){var data=den.getData();if(data.isEmpty()||data.get().getPokemon()==null)continue;var p=data.get().getPokemon();out.add(new RaidInfo(den.getId(),p.getDisplayName().getString(),data.get().getStars(),mc.player.distanceTo(den),den.blockPosition(),p.getSprite()));}out.sort(Comparator.comparingInt(RaidInfo::stars).reversed().thenComparingDouble(RaidInfo::distance));cached=List.copyOf(out);tick=t;return cached;}
}
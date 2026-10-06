package dev.slithergoh.pixelmonradar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.HashSet;
import java.util.Set;

public final class RareAlerts {
 private RareAlerts(){}
 private static final Set<Integer> seen=new HashSet<>(); private static int ticks;
 public static void tick(){
  if(++ticks%20!=0) return; Minecraft mc=Minecraft.getInstance(); if(mc.player==null) return;
  for(PokemonInfo p:PokemonScanner.scan()) if(p.rare() && seen.add(p.entityId())){
   mc.player.displayClientMessage(Component.literal("[Radar] "+p.name()+p.badges()+" - "+Math.round(p.distance())+"m"),false);
   mc.level.playLocalSound(mc.player.getX(),mc.player.getY(),mc.player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER,0.7f,1.3f,false);
  }
 }
}

package dev.slithergoh.pixelmonradar.client;

import net.minecraft.core.BlockPos;import net.minecraft.resources.ResourceLocation;
public record PokemonInfo(int entityId,String name,int level,double distance,BlockPos pos,ResourceLocation sprite,boolean shiny,boolean legendary,boolean mythical,boolean ultraBeast,boolean paradox,boolean boss,String gender,String form,String palette,String bossTier,boolean dexSeen,boolean dexCaught){public boolean rare(){return shiny||legendary||mythical||ultraBeast||paradox||boss;}public String badges(){StringBuilder s=new StringBuilder();if(shiny)s.append(" SHINY");if(legendary)s.append(" LEGENDARY");if(mythical)s.append(" MYTHICAL");if(ultraBeast)s.append(" UB");if(paradox)s.append(" PARADOX");if(boss)s.append(" BOSS");return s.toString();}}

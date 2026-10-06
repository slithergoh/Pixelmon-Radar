package dev.slithergoh.pixelmonradar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import java.util.List;

@EventBusSubscriber(modid="pixelmonradar",value=Dist.CLIENT)
public final class RadarHud {
 @SubscribeEvent public static void render(RenderGuiEvent.Post e){if(!RadarSettings.hudEnabled)return;Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.screen!=null)return;List<PokemonInfo> list=PokemonScanner.scan();GuiGraphics g=e.getGuiGraphics();int x=7,y=7,shown=Math.min(10,list.size());g.fill(x-3,y-3,x+285,y+18+shown*12,0xA0101010);g.drawString(mc.font,"Pixelmon Radar  ["+list.size()+"]",x,y,0xFFFFFF,true);y+=15;for(int i=0;i<shown;i++){PokemonInfo p=list.get(i);int color=p.shiny()?0x55FFFF:p.rare()?0xFFD75A:0xE8E8E8;g.drawString(mc.font,p.name()+" Lv."+p.level()+"  "+Math.round(p.distance())+"m"+p.badges(),x,y,color,true);y+=12;}
  if(RadarSettings.trackedEntityId!=-1){for(PokemonInfo p:list)if(p.entityId()==RadarSettings.trackedEntityId){double dx=p.pos().getX()+.5-mc.player.getX(),dz=p.pos().getZ()+.5-mc.player.getZ();double target=Math.toDegrees(Math.atan2(-dx,dz));double rel=wrap(target-mc.player.getYRot());String arrow=arrow(rel);String s="TRACK  "+arrow+"  "+p.name()+"  "+Math.round(p.distance())+"m  XYZ "+p.pos().getX()+" "+p.pos().getY()+" "+p.pos().getZ();int w=mc.font.width(s)+12;int cx=g.guiWidth()/2;g.fill(cx-w/2,6,cx+w/2,25,0xB0202028);g.drawCenteredString(mc.font,s,cx,11,p.shiny()?0x55FFFF:0xFFFFFF);break;}}
 }
 private static double wrap(double a){while(a<=-180)a+=360;while(a>180)a-=360;return a;}
 private static String arrow(double a){if(a>-22.5&&a<=22.5)return "↑";if(a<=-157.5||a>157.5)return "↓";if(a>22.5&&a<=67.5)return "↖";if(a>67.5&&a<=112.5)return "←";if(a>112.5&&a<=157.5)return "↙";if(a<-22.5&&a>=-67.5)return "↗";if(a<-67.5&&a>=-112.5)return "→";return "↘";}
}

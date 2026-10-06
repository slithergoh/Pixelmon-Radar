package dev.slithergoh.pixelmonradar.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid="pixelmonradar",value=Dist.CLIENT)
public final class BlockEspHud {
 private BlockEspHud(){}

 @SubscribeEvent public static void render(RenderGuiEvent.Post e){
  Minecraft mc=Minecraft.getInstance();
  if(!RadarSettings.oreEspEnabled||mc.player==null||mc.level==null||mc.screen!=null)return;
  if(!RadarSettings.espLabels&&!RadarSettings.espIcons)return;
  GuiGraphics g=e.getGuiGraphics(); Camera cam=mc.gameRenderer.getMainCamera();
  Vec3 cp=cam.getPosition(); Vector3f look=cam.getLookVector(),up=cam.getUpVector(),left=cam.getLeftVector();
  int w=g.guiWidth(),h=g.guiHeight(),shown=0;
  double fov=Math.toRadians(mc.options.fov().get()); double focal=(h*.5)/Math.tan(fov*.5);
  for(BlockPos p:OreScanner.scan()){
   if(shown>=Math.min(18,RadarSettings.espMaxIcons))break;
   double dist=Math.sqrt(p.distSqr(mc.player.blockPosition())); if(dist>RadarSettings.espDetailRange)continue;
   double rx=p.getX()+.5-cp.x,ry=p.getY()+.5-cp.y,rz=p.getZ()+.5-cp.z;
   double depth=rx*look.x+ry*look.y+rz*look.z; if(depth<=.2)continue;
   double sx=w*.5+(rx*left.x+ry*left.y+rz*left.z)*focal/depth;
   double sy=h*.5-(rx*up.x+ry*up.y+rz*up.z)*focal/depth;
   if(sx<-80||sx>w+80||sy<-30||sy>h+30)continue;
   var state=mc.level.getBlockState(p); ResourceLocation id=BuiltInRegistries.BLOCK.getKey(state.getBlock());
   ItemStack stack=new ItemStack(state.getBlock().asItem()); String name=stack.isEmpty()?title(id.getPath()):stack.getHoverName().getString();
   int x=(int)Math.round(sx),y=(int)Math.round(sy);
   int icon=RadarSettings.espIcons&&!stack.isEmpty()?16:0;
   String label=name+"  "+Math.round(dist)+"m"; int tw=RadarSettings.espLabels?mc.font.width(label):0;
   int bw=icon+(icon>0&&tw>0?4:0)+tw+8,bh=20,l=x-bw/2,t=y-bh/2;
   g.fill(l,t,l+bw,t+bh,0xB510151B);g.fill(l,t,l+2,t+bh,color(id));
   int dx=l+5;if(icon>0){g.renderItem(stack,dx,t+2);dx+=20;}
   if(RadarSettings.espLabels)g.drawString(mc.font,label,dx,t+6,0xFFF4F7FA,true);
   shown++;
  }
 }
 private static int color(ResourceLocation id){String p=id.getPath();if(p.contains("pokestop")||p.contains("poke_stop"))return 0xFF26BFFF;if(p.contains("pokeloot")||p.contains("poke_loot")||p.contains("pokeball")||p.contains("poke_ball"))return 0xFFFF3340;if(p.contains("diamond")||p.contains("crystal"))return 0xFF33FFFF;if(p.contains("emerald"))return 0xFF33FF59;if(p.contains("redstone")||p.contains("ruby"))return 0xFFFF2626;if(p.contains("lapis")||p.contains("sapphire"))return 0xFF3366FF;if(p.contains("gold"))return 0xFFFFCC1A;if(p.contains("iron")||p.contains("silver")||p.contains("platinum"))return 0xFFE6E6E6;if(p.contains("copper")||p.contains("bauxite"))return 0xFFFF7330;if(p.contains("coal"))return 0xFF8C8C8C;return 0xFFFF4DCC;}
 private static String title(String s){StringBuilder o=new StringBuilder();boolean up=true;for(char ch:s.toCharArray()){if(ch=='_'){o.append(' ');up=true;}else{o.append(up?Character.toUpperCase(ch):ch);up=false;}}return o.toString();}
}
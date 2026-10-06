package dev.slithergoh.pixelmonradar.client;

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
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid="pixelmonradar",value=Dist.CLIENT)
public final class BlockEspHud {
 private BlockEspHud(){}
 private record Tag(BlockPos pos,float nx,float ny,double distance,ResourceLocation id,ItemStack stack,String name,int count){}
 private static volatile List<Tag> frameTags=List.of();

 // Capture positions using Minecraft's real world matrices. Nothing is drawn here.
 @SubscribeEvent public static void capture(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
  Minecraft mc=Minecraft.getInstance();
  if(!RadarSettings.oreEspEnabled||mc.player==null||mc.level==null||(!RadarSettings.espLabels&&!RadarSettings.espIcons)){frameTags=List.of();return;}

  Vec3 cam=mc.gameRenderer.getMainCamera().getPosition();
  Matrix4f world=new Matrix4f(e.getModelViewMatrix()).translate((float)-cam.x,(float)-cam.y,(float)-cam.z);
  Matrix4f projection=new Matrix4f(e.getProjectionMatrix());
  ArrayList<Tag> tags=new ArrayList<>();
  Set<String> clusters=new HashSet<>();

  for(BlockPos p:OreScanner.scan()){
   if(tags.size()>=12)break;
   double dist=Math.sqrt(p.distSqr(mc.player.blockPosition()));
   if(dist>Math.min(24,RadarSettings.espDetailRange))continue;
   var state=mc.level.getBlockState(p);
   ResourceLocation id=BuiltInRegistries.BLOCK.getKey(state.getBlock());
   String cluster=id+":"+(p.getX()>>1)+":"+(p.getY()>>1)+":"+(p.getZ()>>1);
   if(!clusters.add(cluster))continue;

   Vector4f clip=new Vector4f(p.getX()+.5f,p.getY()+1.10f,p.getZ()+.5f,1f).mul(world).mul(projection);
   if(clip.w<=0.001f)continue;
   float x=clip.x/clip.w,y=clip.y/clip.w,z=clip.z/clip.w;
   if(z<-1f||z>1f||x<-1.15f||x>1.15f||y<-1.15f||y>1.15f)continue;

   ItemStack stack=new ItemStack(state.getBlock().asItem());
   String name=stack.isEmpty()?title(id.getPath()):stack.getHoverName().getString();
   tags.add(new Tag(p,(x+1f)*.5f,(1f-y)*.5f,dist,id,stack,name,clusterCount(mc,p,state.getBlock())));
  }
  frameTags=List.copyOf(tags);
 }

 // Draw only in the GUI. This cannot mutate world render buffers or world transforms.
 @SubscribeEvent public static void draw(RenderGuiEvent.Post e){
  Minecraft mc=Minecraft.getInstance();
  if(!RadarSettings.oreEspEnabled||mc.player==null||mc.level==null||mc.screen!=null)return;
  GuiGraphics g=e.getGuiGraphics();
  for(Tag t:frameTags){
   int x=Math.round(t.nx*g.guiWidth()),y=Math.round(t.ny*g.guiHeight());
   boolean icon=RadarSettings.espIcons&&!t.stack.isEmpty();
   String text=t.name+(t.count>1?" ×"+t.count:"")+" · "+Math.round(t.distance)+"m";
   int tw=RadarSettings.espLabels?mc.font.width(text):0;
   int iw=icon?12:0;
   int gap=icon&&RadarSettings.espLabels?3:0;
   int bw=iw+gap+tw+8,bh=14,left=x-bw/2,top=y-bh/2;

   g.fill(left,top,left+bw,top+bh,0xA60A0E12);
   g.fill(left,top,left+2,top+bh,color(t.id));
   int dx=left+4;
   if(icon){
    g.pose().pushPose();
    g.pose().translate(dx,top+1,0);
    g.pose().scale(.75f,.75f,1f);
    g.renderItem(t.stack,0,0);
    g.pose().popPose();
    dx+=15;
   }
   if(RadarSettings.espLabels)g.drawString(mc.font,text,dx,top+3,0xFFF2F5F7,true);
  }
 }

 private static int clusterCount(Minecraft mc,BlockPos center,net.minecraft.world.level.block.Block block){
  int n=0;for(BlockPos p:OreScanner.scan()){if(n>=9)break;if(p.distSqr(center)<=8&&mc.level.getBlockState(p).getBlock()==block)n++;}return n;
 }
 private static int color(ResourceLocation id){String p=id.getPath();if(p.contains("pokestop")||p.contains("poke_stop"))return 0xFF26BFFF;if(p.contains("pokeloot")||p.contains("poke_loot")||p.contains("pokeball")||p.contains("poke_ball"))return 0xFFFF3340;if(p.contains("diamond")||p.contains("crystal"))return 0xFF33FFFF;if(p.contains("emerald"))return 0xFF33FF59;if(p.contains("redstone")||p.contains("ruby"))return 0xFFFF2626;if(p.contains("lapis")||p.contains("sapphire"))return 0xFF3366FF;if(p.contains("gold"))return 0xFFFFCC1A;if(p.contains("iron")||p.contains("silver")||p.contains("platinum"))return 0xFFE6E6E6;if(p.contains("copper")||p.contains("bauxite"))return 0xFFFF7330;if(p.contains("coal"))return 0xFF8C8C8C;return 0xFFFF4DCC;}
 private static String title(String s){StringBuilder o=new StringBuilder();boolean up=true;for(char ch:s.toCharArray()){if(ch=='_'){o.append(' ');up=true;}else{o.append(up?Character.toUpperCase(ch):ch);up=false;}}return o.toString();}
}
package dev.slithergoh.pixelmonradar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import java.util.*;

@EventBusSubscriber(modid="pixelmonradar",value=Dist.CLIENT)
public final class BlockEspHud {
 private BlockEspHud(){}
 private record Vein(BlockPos anchor,ResourceLocation id,ItemStack stack,String name,int count,double distance,int priority){}
 private record Tag(BlockPos pos,float nx,float ny,double distance,ResourceLocation id,ItemStack stack,String name,int count,int priority,long seen){}
 private static final Map<BlockPos,Tag> persistent=new LinkedHashMap<>();
 private static String worldKey="";
 private static long generation;

 @SubscribeEvent public static void capture(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS)return;
  Minecraft mc=Minecraft.getInstance();
  if(!RadarSettings.oreEspEnabled||mc.player==null||mc.level==null||(!RadarSettings.espLabels&&!RadarSettings.espIcons)){clear();return;}
  String wk=mc.level.dimension().location().toString();
  if(!wk.equals(worldKey)){clear();worldKey=wk;}
  generation++;

  List<Vein> veins=new ArrayList<>(buildVeins(mc));
  veins.sort(Comparator.comparingInt(Vein::priority).reversed().thenComparingDouble(Vein::distance));
  Vec3 cam=mc.gameRenderer.getMainCamera().getPosition();
  Matrix4f world=new Matrix4f(e.getModelViewMatrix()).translate((float)-cam.x,(float)-cam.y,(float)-cam.z);
  Matrix4f projection=new Matrix4f(e.getProjectionMatrix());
  Set<BlockPos> alive=new HashSet<>();
  int accepted=0,max=Math.max(4,Math.min(24,RadarSettings.espMaxIcons));

  for(Vein v:veins){
   if(accepted>=max)break;
   alive.add(v.anchor);
   Vector4f clip=new Vector4f(v.anchor.getX()+.5f,v.anchor.getY()+1.10f,v.anchor.getZ()+.5f,1f).mul(world).mul(projection);
   if(clip.w<=0.001f)continue;
   float x=clip.x/clip.w,y=clip.y/clip.w,z=clip.z/clip.w;
   if(z<-1f||z>1f||x<-1.2f||x>1.2f||y<-1.2f||y>1.2f)continue;
   persistent.put(v.anchor,new Tag(v.anchor,(x+1f)*.5f,(1f-y)*.5f,v.distance,v.id,v.stack,v.name,v.count,v.priority,generation));
   accepted++;
  }

  // Scanner truth owns lifecycle. A missed projection frame cannot wipe the layer.
  Set<BlockPos> validAnchors=new HashSet<>();
  for(Vein v:veins)validAnchors.add(v.anchor);
  persistent.entrySet().removeIf(en->!validAnchors.contains(en.getKey())||generation-en.getValue().seen>90);
 }

 @SubscribeEvent public static void draw(RenderGuiEvent.Post e){
  Minecraft mc=Minecraft.getInstance();
  if(!RadarSettings.oreEspEnabled||mc.player==null||mc.level==null||mc.screen!=null)return;
  GuiGraphics g=e.getGuiGraphics();
  ArrayList<Tag> tags=new ArrayList<>(persistent.values());
  tags.sort(Comparator.comparingInt(Tag::priority).reversed().thenComparingDouble(Tag::distance));
  int drawn=0,max=Math.max(4,Math.min(24,RadarSettings.espMaxIcons));
  for(Tag t:tags){
   if(drawn++>=max)break;
   int x=Math.round(t.nx*g.guiWidth()),y=Math.round(t.ny*g.guiHeight());
   if(x<-30||x>g.guiWidth()+30||y<-20||y>g.guiHeight()+20)continue;
   boolean focused=Math.abs(t.nx-.5f)<.105f&&Math.abs(t.ny-.5f)<.14f;
   boolean full=RadarSettings.espLabels&&(t.distance<=12||focused||t.priority>=80);
   boolean icon=RadarSettings.espIcons&&!t.stack.isEmpty();
   String text=t.name+(t.count>1?" ×"+t.count:"")+" · "+Math.round(t.distance)+"m"+(full&&t.distance<18?" · Y"+t.pos.getY():"");
   String compact=Math.round(t.distance)+"m";
   String shown=full?text:compact;
   int tw=RadarSettings.espLabels?mc.font.width(shown):0,iw=icon?12:0,gap=icon&&RadarSettings.espLabels?3:0;
   int bw=iw+gap+tw+8,bh=14,left=x-bw/2,top=y-bh/2;
   int bg=full?0xB30A0E12:0x980A0E12;
   g.fill(left,top,left+bw,top+bh,bg);
   g.fill(left,top,left+2,top+bh,color(t.id));
   int dx=left+4;
   if(icon){g.pose().pushPose();g.pose().translate(dx,top+1,0);g.pose().scale(.75f,.75f,1f);g.renderItem(t.stack,0,0);g.pose().popPose();dx+=15;}
   if(RadarSettings.espLabels)g.drawString(mc.font,shown,dx,top+3,full?0xFFF2F5F7:0xFFD1D8DC,true);
  }
 }

 private static List<Vein> buildVeins(Minecraft mc){
  List<BlockPos> scan=OreScanner.scan();
  if(scan.isEmpty())return List.of();
  int detail=Math.min(RadarSettings.oreRange,Math.max(8,RadarSettings.espDetailRange));
  double rr=(double)detail*detail;
  Map<Block,Set<BlockPos>> byBlock=new HashMap<>();
  for(BlockPos p:scan)if(p.distSqr(mc.player.blockPosition())<=rr)byBlock.computeIfAbsent(mc.level.getBlockState(p).getBlock(),k->new HashSet<>()).add(p);
  ArrayList<Vein> out=new ArrayList<>();
  for(var entry:byBlock.entrySet()){
   Block block=entry.getKey();Set<BlockPos> left=new HashSet<>(entry.getValue());
   while(!left.isEmpty()){
    BlockPos start=left.iterator().next();ArrayDeque<BlockPos> q=new ArrayDeque<>();q.add(start);left.remove(start);
    ArrayList<BlockPos> group=new ArrayList<>();group.add(start);
    while(!q.isEmpty()&&group.size()<64){BlockPos p=q.removeFirst();for(Direction d:Direction.values()){BlockPos n=p.relative(d);if(left.remove(n)){q.addLast(n);group.add(n);}}}
    BlockPos anchor=group.stream().min(Comparator.comparingDouble(p->p.distSqr(mc.player.blockPosition()))).orElse(start);
    ResourceLocation id=BuiltInRegistries.BLOCK.getKey(block);ItemStack stack=new ItemStack(block.asItem());
    String name=stack.isEmpty()?title(id.getPath()):stack.getHoverName().getString();
    double dist=Math.sqrt(anchor.distSqr(mc.player.blockPosition()));
    out.add(new Vein(anchor,id,stack,name,group.size(),dist,priority(id)));
   }
  }
  return out;
 }
 private static int priority(ResourceLocation id){String p=id.getPath();if(p.contains("pokeloot")||p.contains("poke_loot")||p.contains("pokestop")||p.contains("poke_stop")||p.contains("master"))return 100;if(p.contains("diamond")||p.contains("ruby")||p.contains("sapphire")||p.contains("platinum")||p.contains("crystal"))return 90;if(p.contains("emerald")||p.contains("gold")||p.contains("silver"))return 75;if(p.contains("redstone")||p.contains("lapis")||p.contains("iron"))return 55;if(p.contains("copper")||p.contains("bauxite"))return 40;if(p.contains("coal"))return 20;return 60;}
 private static void clear(){persistent.clear();generation=0;}
 private static int color(ResourceLocation id){String p=id.getPath();if(p.contains("pokestop")||p.contains("poke_stop"))return 0xFF26BFFF;if(p.contains("pokeloot")||p.contains("poke_loot")||p.contains("pokeball")||p.contains("poke_ball"))return 0xFFFF3340;if(p.contains("diamond")||p.contains("crystal"))return 0xFF33FFFF;if(p.contains("emerald"))return 0xFF33FF59;if(p.contains("redstone")||p.contains("ruby"))return 0xFFFF2626;if(p.contains("lapis")||p.contains("sapphire"))return 0xFF3366FF;if(p.contains("gold"))return 0xFFFFCC1A;if(p.contains("iron")||p.contains("silver")||p.contains("platinum"))return 0xFFE6E6E6;if(p.contains("copper")||p.contains("bauxite"))return 0xFFFF7330;if(p.contains("coal"))return 0xFF8C8C8C;return 0xFFFF4DCC;}
 private static String title(String s){StringBuilder o=new StringBuilder();boolean up=true;for(char ch:s.toCharArray()){if(ch=='_'){o.append(' ');up=true;}else{o.append(up?Character.toUpperCase(ch):ch);up=false;}}return o.toString();}
}
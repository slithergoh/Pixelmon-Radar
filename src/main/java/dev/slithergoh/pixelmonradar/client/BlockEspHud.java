package dev.slithergoh.pixelmonradar.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid="pixelmonradar",value=Dist.CLIENT)
public final class BlockEspHud {
 private BlockEspHud(){}

 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
  Minecraft mc=Minecraft.getInstance();
  if(!RadarSettings.oreEspEnabled||mc.player==null||mc.level==null||mc.screen!=null)return;
  if(!RadarSettings.espLabels&&!RadarSettings.espIcons)return;

  Vec3 camera=mc.gameRenderer.getMainCamera().getPosition();

  // Match the proven 1.21 XRay transform: event model-view first, then one camera offset.
  // Tags are children of that exact world transform, never separately projected.
  PoseStack world=new PoseStack();
  world.mulPose(e.getModelViewMatrix());
  world.translate(-camera.x,-camera.y,-camera.z);

  // Completely private render memory: never acquire or flush Minecraft's world BufferSource.
  try(ByteBufferBuilder memory=new ByteBufferBuilder(1024*1024)){
   MultiBufferSource.BufferSource out=MultiBufferSource.immediate(memory);
   Set<String> clusters=new HashSet<>();
   int shown=0;

   for(BlockPos p:OreScanner.scan()){
    if(shown>=12)break;
    double dist=Math.sqrt(p.distSqr(mc.player.blockPosition()));
    if(dist>Math.min(24,RadarSettings.espDetailRange))continue;

    var state=mc.level.getBlockState(p);
    ResourceLocation id=BuiltInRegistries.BLOCK.getKey(state.getBlock());
    String cluster=id+":"+(p.getX()>>1)+":"+(p.getY()>>1)+":"+(p.getZ()>>1);
    if(!clusters.add(cluster))continue;

    ItemStack stack=new ItemStack(state.getBlock().asItem());
    String name=stack.isEmpty()?title(id.getPath()):stack.getHoverName().getString();
    int count=clusterCount(mc,p,state.getBlock());

    world.pushPose();
    world.translate(p.getX()+.5,p.getY()+1.10,p.getZ()+.5);
    world.mulPose(mc.gameRenderer.getMainCamera().rotation());

    if(RadarSettings.espIcons&&!stack.isEmpty()){
     world.pushPose();
     world.translate(0,.015,0);
     world.scale(.105f,.105f,.105f);
     mc.getItemRenderer().renderStatic(stack,ItemDisplayContext.GUI,15728880,OverlayTexture.NO_OVERLAY,world,out,mc.level,p.hashCode());
     world.popPose();
    }

    if(RadarSettings.espLabels){
     String text=name+(count>1?" ×"+count:"")+"  "+Math.round(dist)+"m";
     world.pushPose();
     world.translate(0,RadarSettings.espIcons?.105:.035,0);
     float scale=.0082f;
     world.scale(-scale,-scale,scale);
     Matrix4f matrix=world.last().pose();
     float x=-mc.font.width(text)/2f;
     mc.font.drawInBatch(text,x,0,0xFFF3F6F8,false,matrix,out,Font.DisplayMode.SEE_THROUGH,0x76000000,15728880);
     world.popPose();
    }

    world.popPose();
    shown++;
   }
   out.endBatch();
  }
 }

 private static int clusterCount(Minecraft mc,BlockPos center,net.minecraft.world.level.block.Block block){
  int n=0;
  for(BlockPos p:OreScanner.scan()){
   if(n>=9)break;
   if(p.distSqr(center)<=8&&mc.level.getBlockState(p).getBlock()==block)n++;
  }
  return n;
 }

 private static String title(String s){
  StringBuilder o=new StringBuilder();boolean up=true;
  for(char ch:s.toCharArray()){if(ch=='_'){o.append(' ');up=true;}else{o.append(up?Character.toUpperCase(ch):ch);up=false;}}
  return o.toString();
 }
}
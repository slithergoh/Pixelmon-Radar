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

@EventBusSubscriber(modid="pixelmonradar",value=Dist.CLIENT)
public final class BlockEspHud {
 private BlockEspHud(){}

 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS)return;
  Minecraft mc=Minecraft.getInstance();
  if(!RadarSettings.oreEspEnabled||mc.player==null||mc.level==null||mc.screen!=null)return;
  if(!RadarSettings.espLabels&&!RadarSettings.espIcons)return;

  Vec3 cam=mc.gameRenderer.getMainCamera().getPosition();
  PoseStack ps=e.getPoseStack();
  ps.pushPose();
  ps.translate(-cam.x,-cam.y,-cam.z);

  // Critical: tags/items use their OWN vertex memory and BufferSource.
  // Never flush Minecraft's shared world BufferSource from this pass.
  try(ByteBufferBuilder memory=new ByteBufferBuilder(512*1024)){
   MultiBufferSource.BufferSource tags=MultiBufferSource.immediate(memory);
   int shown=0;
   for(BlockPos p:OreScanner.scan()){
    if(shown>=10)break;
    double dist=Math.sqrt(p.distSqr(mc.player.blockPosition()));
    if(dist>Math.min(20,RadarSettings.espDetailRange))continue;

    var state=mc.level.getBlockState(p);
    ResourceLocation id=BuiltInRegistries.BLOCK.getKey(state.getBlock());
    ItemStack stack=new ItemStack(state.getBlock().asItem());
    String name=stack.isEmpty()?title(id.getPath()):stack.getHoverName().getString();

    ps.pushPose();
    ps.translate(p.getX()+.5,p.getY()+1.04,p.getZ()+.5);
    ps.mulPose(mc.gameRenderer.getMainCamera().rotation());

    if(RadarSettings.espIcons&&!stack.isEmpty()){
     ps.pushPose();
     ps.translate(0,-.035,0);
     ps.scale(.16f,.16f,.16f);
     mc.getItemRenderer().renderStatic(stack,ItemDisplayContext.GUI,15728880,OverlayTexture.NO_OVERLAY,ps,tags,mc.level,p.hashCode());
     ps.popPose();
    }

    if(RadarSettings.espLabels){
     ps.pushPose();
     ps.translate(0,RadarSettings.espIcons?.13:.03,0);
     float scale=.009f;
     ps.scale(-scale,-scale,scale);
     Matrix4f matrix=ps.last().pose();
     String label=name+" · "+Math.round(dist)+"m";
     float x=-mc.font.width(label)/2f;
     mc.font.drawInBatch(label,x,0,color(id),false,matrix,tags,Font.DisplayMode.SEE_THROUGH,0x85000000,15728880);
     ps.popPose();
    }

    ps.popPose();
    shown++;
   }
   tags.endBatch();
  }
  ps.popPose();
 }

 private static int color(ResourceLocation id){String p=id.getPath();if(p.contains("pokestop")||p.contains("poke_stop"))return 0xFF26BFFF;if(p.contains("pokeloot")||p.contains("poke_loot")||p.contains("pokeball")||p.contains("poke_ball"))return 0xFFFF3340;if(p.contains("diamond")||p.contains("crystal"))return 0xFF33FFFF;if(p.contains("emerald"))return 0xFF33FF59;if(p.contains("redstone")||p.contains("ruby"))return 0xFFFF2626;if(p.contains("lapis")||p.contains("sapphire"))return 0xFF3366FF;if(p.contains("gold"))return 0xFFFFCC1A;if(p.contains("iron")||p.contains("silver")||p.contains("platinum"))return 0xFFE6E6E6;if(p.contains("copper")||p.contains("bauxite"))return 0xFFFF7330;if(p.contains("coal"))return 0xFF8C8C8C;return 0xFFFF4DCC;}
 private static String title(String s){StringBuilder o=new StringBuilder();boolean up=true;for(char ch:s.toCharArray()){if(ch=='_'){o.append(' ');up=true;}else{o.append(up?Character.toUpperCase(ch):ch);up=false;}}return o.toString();}
}
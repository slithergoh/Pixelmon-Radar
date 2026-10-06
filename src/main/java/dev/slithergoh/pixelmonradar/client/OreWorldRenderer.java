package dev.slithergoh.pixelmonradar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid="pixelmonradar", value=Dist.CLIENT)
public final class OreWorldRenderer {
 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(!RadarSettings.oreEspEnabled || e.getStage()!=RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS) return;
  Minecraft mc=Minecraft.getInstance(); if(mc.level==null||mc.player==null) return;
  Vec3 cam=mc.gameRenderer.getMainCamera().getPosition(); PoseStack ps=e.getPoseStack(); ps.pushPose(); ps.translate(-cam.x,-cam.y,-cam.z);
  MultiBufferSource.BufferSource bs=mc.renderBuffers().bufferSource(); VertexConsumer vc=bs.getBuffer(RenderType.lines());
  int count=0; for(BlockPos p:OreScanner.scan()){ if(count++>800) break; LevelRenderer.renderLineBox(ps,vc,p.getX(),p.getY(),p.getZ(),p.getX()+1,p.getY()+1,p.getZ()+1,1f,0.2f,0.2f,1f,1f,1f,1f); }
  bs.endBatch(RenderType.lines()); ps.popPose();
 }
}

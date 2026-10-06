package dev.slithergoh.pixelmonradar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(modid="pixelmonradar",value=Dist.CLIENT)
public final class BlockEspHud {
 private BlockEspHud(){}
 private static VertexBuffer markerBuffer;
 private static int signature=Integer.MIN_VALUE;

 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)return;
  Minecraft mc=Minecraft.getInstance();
  if(!RadarSettings.oreEspEnabled||mc.player==null||mc.level==null||mc.screen!=null)return;
  if(!RadarSettings.espLabels&&!RadarSettings.espIcons)return;

  var blocks=OreScanner.scan();
  int sig=31*blocks.hashCode()+(RadarSettings.espIcons?1:0)+(RadarSettings.espLabels?2:0);
  if(markerBuffer==null||sig!=signature){signature=sig;rebuild(mc,blocks);}
  if(markerBuffer==null)return;

  Vec3 camera=mc.gameRenderer.getMainCamera().getPosition();
  PoseStack ps=e.getPoseStack();
  RenderSystem.depthMask(false);
  RenderSystem.enableBlend();
  RenderSystem.defaultBlendFunc();
  RenderSystem.setShader(GameRenderer::getPositionColorShader);
  RenderSystem.depthFunc(GL11.GL_ALWAYS);
  ps.pushPose();
  ps.mulPose(e.getModelViewMatrix());
  ps.translate(-camera.x,-camera.y,-camera.z);
  markerBuffer.bind();
  markerBuffer.drawWithShader(ps.last().pose(),e.getProjectionMatrix(),RenderSystem.getShader());
  VertexBuffer.unbind();
  ps.popPose();
  RenderSystem.depthFunc(GL11.GL_LEQUAL);
  RenderSystem.depthMask(true);
  RenderSystem.applyModelViewMatrix();
 }

 private static void rebuild(Minecraft mc,java.util.List<BlockPos> blocks){
  if(markerBuffer!=null){markerBuffer.close();markerBuffer=null;}
  Tesselator tess=Tesselator.getInstance();
  BufferBuilder b=tess.begin(VertexFormat.Mode.DEBUG_LINES,DefaultVertexFormat.POSITION_COLOR);
  int shown=0;
  for(BlockPos p:blocks){
   if(shown++>=80)break;
   double dist=Math.sqrt(p.distSqr(mc.player.blockPosition()));
   if(dist>Math.min(24,RadarSettings.espDetailRange))continue;
   ResourceLocation id=BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(p).getBlock());
   int c=color(id);float r=((c>>16)&255)/255f,g=((c>>8)&255)/255f,bl=(c&255)/255f;
   double x=p.getX()+.5,y=p.getY()+1.08,z=p.getZ()+.5;
   // XRay-style geometry: every marker uses the exact same absolute block position
   // and the same camera transform as the proven outline renderer.
   if(RadarSettings.espIcons)cross(b,x,y,z,.075,r,g,bl);
   if(RadarSettings.espLabels){
    // compact underline/tag anchor; text name remains in the menu to avoid a second
    // world text renderer corrupting or drifting independently from the block.
    line(b,x-.14,y+.10,z,x+.14,y+.10,z,r,g,bl);
    line(b,x-.14,y+.10,z,x-.14,y+.145,z,r,g,bl);
    line(b,x+.14,y+.10,z,x+.14,y+.145,z,r,g,bl);
   }
  }
  MeshData mesh=b.build();
  if(mesh==null)return;
  markerBuffer=new VertexBuffer(VertexBuffer.Usage.STATIC);
  markerBuffer.bind();markerBuffer.upload(mesh);VertexBuffer.unbind();
 }

 private static void cross(BufferBuilder b,double x,double y,double z,double s,float r,float g,float bl){
  line(b,x-s,y,z,x+s,y,z,r,g,bl);line(b,x,y-s,z,x,y+s,z,r,g,bl);line(b,x,y,z-s,x,y,z+s,r,g,bl);
 }
 private static void line(BufferBuilder b,double x1,double y1,double z1,double x2,double y2,double z2,float r,float g,float bl){b.addVertex((float)x1,(float)y1,(float)z1).setColor(r,g,bl,1f);b.addVertex((float)x2,(float)y2,(float)z2).setColor(r,g,bl,1f);}
 private static int color(ResourceLocation id){String p=id.getPath();if(p.contains("pokestop")||p.contains("poke_stop"))return 0x26BFFF;if(p.contains("pokeloot")||p.contains("poke_loot")||p.contains("pokeball")||p.contains("poke_ball"))return 0xFF3340;if(p.contains("diamond")||p.contains("crystal"))return 0x33FFFF;if(p.contains("emerald"))return 0x33FF59;if(p.contains("redstone")||p.contains("ruby"))return 0xFF2626;if(p.contains("lapis")||p.contains("sapphire"))return 0x3366FF;if(p.contains("gold"))return 0xFFCC1A;if(p.contains("iron")||p.contains("silver")||p.contains("platinum"))return 0xE6E6E6;if(p.contains("copper")||p.contains("bauxite"))return 0xFF7330;if(p.contains("coal"))return 0x8C8C8C;return 0xFF4DCC;}
}
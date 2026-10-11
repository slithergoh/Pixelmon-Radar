package dev.slithergoh.atlascobblemon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Client-only screen projection of the eight corners of an entity bounding box. */
public final class PokemonEsp {
 private PokemonEsp(){}
 public static void draw(DrawContext g){
  MinecraftClient mc=MinecraftClient.getInstance();
  if(mc.world==null||mc.player==null||mc.currentScreen!=null)return;
  AtlasSettings s=AtlasSettings.INSTANCE;
  for(CobblemonScanner.Pokemon p:AtlasCobblemonClient.visible){
   boolean tracked=p.id()==s.trackedId;
   if(!tracked&&!s.esp)continue;
   if(!tracked&&!p.rare())continue;
   Entity entity=mc.world.getEntityById(p.id());
   if(entity==null)continue;
   int color=tracked?0xFF55DFFB:p.shiny()?0xFFFFDB58:0xFFFF704E;
   outline(g,entity.getBoundingBox().expand(.07),color);
  }
 }
 public static void outline(DrawContext g,Box b,int color){
  MinecraftClient mc=MinecraftClient.getInstance();
  var camera=mc.gameRenderer.getCamera();
  Vec3d eye=camera.getPos();
  Quaternionf q=new Quaternionf(camera.getRotation());
  Vector3f right=new Vector3f(1,0,0).rotate(q);
  Vector3f up=new Vector3f(0,1,0).rotate(q);
  Vector3f forward=new Vector3f(0,0,-1).rotate(q);
  int sw=g.getScaledWindowWidth(),sh=g.getScaledWindowHeight();
  double tan=Math.tan(Math.toRadians(mc.options.getFov().getValue())/2),aspect=(double)sw/sh;
  int minX=sw,maxX=0,minY=sh,maxY=0;
  for(int xi=0;xi<2;xi++)for(int yi=0;yi<2;yi++)for(int zi=0;zi<2;zi++){
   double dx=(xi==0?b.minX:b.maxX)-eye.x;
   double dy=(yi==0?b.minY:b.maxY)-eye.y;
   double dz=(zi==0?b.minZ:b.maxZ)-eye.z;
   double depth=dx*forward.x+dy*forward.y+dz*forward.z;
   if(depth<=.1)return;
   double nx=(dx*right.x+dy*right.y+dz*right.z)/(depth*tan*aspect);
   double ny=(dx*up.x+dy*up.y+dz*up.z)/(depth*tan);
   int x=(int)((nx+1)*sw*.5),y=(int)((1-ny)*sh*.5);
   minX=Math.min(minX,x);maxX=Math.max(maxX,x);
   minY=Math.min(minY,y);maxY=Math.max(maxY,y);
  }
  if(maxX<0||minX>=sw||maxY<0||minY>=sh)return;
  minX=Math.max(0,minX);maxX=Math.min(sw-1,maxX);
  minY=Math.max(0,minY);maxY=Math.min(sh-1,maxY);
  if(maxX-minX<2||maxY-minY<2)return;
  g.fill(minX,minY,maxX+1,minY+2,color);
  g.fill(minX,maxY-1,maxX+1,maxY+1,color);
  g.fill(minX,minY,minX+2,maxY+1,color);
  g.fill(maxX-1,minY,maxX+1,maxY+1,color);
 }
}

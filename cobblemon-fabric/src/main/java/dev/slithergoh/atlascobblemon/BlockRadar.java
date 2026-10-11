package dev.slithergoh.atlascobblemon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import java.util.*;
public final class BlockRadar {
 private static final Map<BlockPos,String> found=new HashMap<>();
 private static int cursor=0;
 private static String dimension="";
 private static BlockPos center=BlockPos.ORIGIN;
 private static int oldSelection=0;
 private BlockRadar(){}
 public static void tick(){
  MinecraftClient mc=MinecraftClient.getInstance();AtlasSettings s=AtlasSettings.INSTANCE;
  if(!s.esp||s.blocks.isEmpty()||mc.world==null||mc.player==null){found.clear();cursor=0;return;}
  String dim=mc.world.getRegistryKey().getValue().toString();int selection=s.blocks.hashCode();
  if(!dimension.equals(dim)||center.getSquaredDistance(mc.player.getBlockPos())>16||selection!=oldSelection){found.clear();cursor=0;center=mc.player.getBlockPos();dimension=dim;oldSelection=selection;}
  int r=Math.max(4,Math.min(48,s.blockRange)),side=r*2+1,total=side*side*side;
  BlockPos.Mutable m=new BlockPos.Mutable();
  for(int i=0;i<1800;i++){
   int n=cursor++%total;
   int dx=n%side-r,dy=(n/side)%side-r,dz=n/(side*side)-r;
   m.set(center.getX()+dx,center.getY()+dy,center.getZ()+dz);
   if(!mc.world.isChunkLoaded(m.getX()>>4,m.getZ()>>4))continue;
   String id=Registries.BLOCK.getId(mc.world.getBlockState(m).getBlock()).toString();
   if(s.blocks.contains(id))found.put(m.toImmutable(),id);
   else found.remove(m);
  }
 }
 public static void draw(DrawContext g){
  MinecraftClient mc=MinecraftClient.getInstance();AtlasSettings s=AtlasSettings.INSTANCE;
  if(!s.esp||mc.world==null||mc.player==null||mc.currentScreen!=null)return;
  var cam=mc.gameRenderer.getCamera();Vec3d eye=cam.getPos();
  Quaternionf rot=new Quaternionf(cam.getRotation());
  Vector3f right=new Vector3f(1,0,0).rotate(rot),up=new Vector3f(0,1,0).rotate(rot),forward=new Vector3f(0,0,-1).rotate(rot);
  int sw=g.getScaledWindowWidth(),sh=g.getScaledWindowHeight();
  double tan=Math.tan(Math.toRadians(mc.options.getFov().getValue())/2),aspect=(double)sw/sh;
  List<Map.Entry<BlockPos,String>> blocks=new ArrayList<>(found.entrySet());
  blocks.sort(Comparator.comparingDouble(a->a.getKey().getSquaredDistance(mc.player.getBlockPos())));
  int drawn=0;
  for(var e:blocks){
   if(drawn>=s.maxMarkers)break;
   BlockPos b=e.getKey();double dx=b.getX()+.5-eye.x,dy=b.getY()+.5-eye.y,dz=b.getZ()+.5-eye.z;
   double z=dx*forward.x+dy*forward.y+dz*forward.z;if(z<=.1)continue;
   double nx=(dx*right.x+dy*right.y+dz*right.z)/(z*tan*aspect),ny=(dx*up.x+dy*up.y+dz*up.z)/(z*tan);
   if(Math.abs(nx)>1||Math.abs(ny)>1)continue;
   int x=(int)((nx+1)*sw*.5),y=(int)((1-ny)*sh*.5);
   String name=Registries.BLOCK.get(net.minecraft.util.Identifier.of(e.getValue())).getName().getString();
   int distance=(int)Math.sqrt(b.getSquaredDistance(mc.player.getBlockPos()));
   String label=name+"  "+distance+"m";int w=mc.textRenderer.getWidth(label)+10;
   g.fill(x-w/2,y-8,x+w/2,y+7,0xC0100B11);g.fill(x-w/2,y-8,x-w/2+2,y+7,0xFFDC3556);
   g.drawTextWithShadow(mc.textRenderer,label,x-w/2+5,y-4,0xFFF4E8EC);drawn++;
  }
 }
}

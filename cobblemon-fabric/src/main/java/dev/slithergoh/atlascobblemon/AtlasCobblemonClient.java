package dev.slithergoh.atlascobblemon;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import java.util.*;
public final class AtlasCobblemonClient implements ClientModInitializer {
 private static KeyBinding open,toggleHud,toggleEsp;
 private static final Set<Integer> alerted=new HashSet<>();
 private static int ticks;
 public static List<CobblemonScanner.Pokemon> visible=List.of();
 @Override public void onInitializeClient(){
  AtlasSettings.load();
  open=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.atlascobblemon.open",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_R,"category.atlascobblemon"));
  toggleHud=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.atlascobblemon.hud",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_H,"category.atlascobblemon"));
  toggleEsp=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.atlascobblemon.esp",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_UNKNOWN,"category.atlascobblemon"));
  ClientTickEvents.END_CLIENT_TICK.register(mc->{
   while(open.wasPressed())mc.setScreen(new AtlasScreen());
   while(toggleHud.wasPressed()){AtlasSettings.INSTANCE.hud=!AtlasSettings.INSTANCE.hud;AtlasSettings.save();}
   while(toggleEsp.wasPressed()){AtlasSettings.INSTANCE.esp=!AtlasSettings.INSTANCE.esp;AtlasSettings.save();}
   if(mc.world==null||mc.player==null){alerted.clear();visible=List.of();return;}
   BlockRadar.tick();if(++ticks%5!=0)return;
   visible=CobblemonScanner.scan(false);
   var wild=CobblemonScanner.scan(true);Set<Integer> live=new HashSet<>();
   for(var p:wild){
    live.add(p.id());String key=p.name().toLowerCase(Locale.ROOT);
    boolean target=AtlasSettings.INSTANCE.targets.contains(key);
    if(AtlasSettings.INSTANCE.alerts&&(p.rare()||target)&&alerted.add(p.id())){
     mc.player.sendMessage(Text.literal("[ATLAS] "+(p.shiny()?"SHINY ":p.legendary()?"LEGENDARY ":p.mythical()?"MYTHICAL ":"TARGET ")+p.name()+" • "+Math.round(p.distance())+"m").formatted(p.shiny()?Formatting.AQUA:Formatting.RED),false);
     if(AtlasSettings.INSTANCE.alertSound)mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),.8f,1.3f);
    }
   }
   alerted.retainAll(live);
  });
  HudRenderCallback.EVENT.register((draw,tick)->{
   MinecraftClient mc=MinecraftClient.getInstance();BlockRadar.draw(draw);PokemonEsp.draw(draw);if(!AtlasSettings.INSTANCE.hud||mc.player==null||mc.currentScreen!=null)return;
   AtlasSettings settings=AtlasSettings.INSTANCE;int x=settings.hudX,y=settings.hudY;
   int shown=Math.min(4,visible.size()),w=176,h=29+shown*22+(visible.size()>shown?10:0);
   draw.fill(x,y,x+w,y+h,0xDF140F16);draw.fill(x,y,x+3,y+h,0xFFB84058);
   draw.drawTextWithShadow(mc.textRenderer,"ATLAS",x+10,y+6,0xFFF4F8FA);
   draw.drawTextWithShadow(mc.textRenderer,visible.size()+" nearby",x+w-65,y+6,0xFFB9A0A9);
   for(int i=0;i<shown;i++){
    var p=visible.get(i);int py=y+31+i*22;
    if(p.id()==settings.trackedId)draw.fill(x+5,py-3,x+w-5,py+18,0xAA21483E);
    int accent=p.shiny()?0xFF62FFFF:p.legendary()||p.mythical()?0xFFFFB64F:0xFFE8EEF1;
    String name=p.name();if(mc.textRenderer.getWidth(name)>87)name=mc.textRenderer.trimToWidth(name,83)+"…";
    draw.drawTextWithShadow(mc.textRenderer,name,x+29,py,accent);
    draw.drawTextWithShadow(mc.textRenderer,p.owned()?"OWNED":"WILD",x+29,py+10,p.owned()?0xFF70C990:0xFF62D7FF);
    draw.drawTextWithShadow(mc.textRenderer,Math.round(p.distance())+"m",x+w-34,py+4,0xFF9FB1B9);
   }
   if(visible.size()>shown)draw.drawTextWithShadow(mc.textRenderer,"+"+(visible.size()-shown)+" more  •  R opens Atlas",x+10,y+31+shown*22,0xFF718995);
   if(settings.partyHud){
    var party=CobblemonParty.slots();
    if(!party.isEmpty()){
     int px=settings.partyX,py=settings.partyY;
     draw.fill(px,py,px+190,py+19+party.size()*15,0xDC0E0B12);
     draw.fill(px,py,px+3,py+19+party.size()*15,0xFFD52C4D);
     draw.drawTextWithShadow(mc.textRenderer,"PARTY  •  "+party.size()+"/6",px+9,py+5,0xFFFFC0D1);
     int slot=0;for(var p:party){
      int yy=py+18+slot++*15;
      String hp=p.maxHp()>0?"  "+p.hp()+"/"+p.maxHp():"";
      draw.drawTextWithShadow(mc.textRenderer,p.name()+"  Lv."+p.level()+hp,px+9,yy,p.shiny()?0xFF8BE5E8:0xFFEAE0E5);
      if(p.maxHp()>0){int bar=(int)(Math.max(0,Math.min(1,(double)p.hp()/p.maxHp()))*180);draw.fill(px+8,yy+11,px+8+bar,yy+13,0xFF7DC59B);}
     }
    }
   }
   for(var p:visible)if(p.id()==settings.trackedId){
    int tx=settings.trackerX,ty=settings.trackerY;
    int accent=p.shiny()?0xFF62FFFF:p.legendary()||p.mythical()?0xFFFFB64F:0xFF4EB7DD;
    draw.fill(tx,ty,tx+208,ty+40,0xE00A1218);
    draw.fill(tx,ty,tx+208,ty+3,accent);
    draw.drawTextWithShadow(mc.textRenderer,p.name()+"  Lv."+p.level(),tx+41,ty+9,accent);
    double dx=p.pos().getX()+.5-mc.player.getX(),dz=p.pos().getZ()+.5-mc.player.getZ();
    double bearing=(Math.toDegrees(Math.atan2(-dx,dz))+360)%360;
    String[] dirs={"N","NE","E","SE","S","SW","W","NW"};
    String dir=dirs[(int)Math.round(bearing/45)%8];
    draw.drawTextWithShadow(mc.textRenderer,Math.round(p.distance())+"m  •  "+dir+"  •  Y "+p.pos().getY(),tx+41,ty+23,0xFF9DB2BC);
    draw.drawTextWithShadow(mc.textRenderer,"◆",tx+10,ty+15,accent);
    break;
   }
  });
 }
}

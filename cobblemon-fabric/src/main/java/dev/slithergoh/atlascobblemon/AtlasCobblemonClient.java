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
   MinecraftClient mc=MinecraftClient.getInstance();BlockRadar.draw(draw);if(!AtlasSettings.INSTANCE.hud||mc.player==null||mc.currentScreen!=null)return;
   AtlasSettings settings=AtlasSettings.INSTANCE;int x=settings.hudX,y=settings.hudY;draw.fill(x,y,x+186,y+19+Math.min(visible.size(),9)*13,0xB6090A10);
   draw.fill(x,y,x+3,y+19+Math.min(visible.size(),9)*13,0xFFD52C4D);
   draw.drawTextWithShadow(mc.textRenderer,"ATLAS  •  COBBLEMON",x+9,y+5,0xFFF6D8DF);
   int i=0;for(var p:visible){if(i++>=9)break;int col=p.shiny()?0xFF6CE8E8:p.rare()?0xFFF1B4B4:0xFFE2DCE1;draw.drawTextWithShadow(mc.textRenderer,p.name()+"  Lv."+p.level()+"  "+Math.round(p.distance())+"m",x+9,y+9+i*13,col);}
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
    int tx=settings.trackerX,ty=settings.trackerY;draw.fill(tx,ty,tx+190,ty+39,0xDC0E0B12);draw.fill(tx,ty,tx+3,ty+39,0xFFD52C4D);
    draw.drawTextWithShadow(mc.textRenderer,"TRACKING  "+p.name(),tx+9,ty+7,0xFFFFB6C9);
    draw.drawTextWithShadow(mc.textRenderer,Math.round(p.distance())+"m  X:"+p.pos().getX()+" Y:"+p.pos().getY()+" Z:"+p.pos().getZ(),tx+9,ty+22,0xFFE7DCE1);break;
   }
  });
 }
}

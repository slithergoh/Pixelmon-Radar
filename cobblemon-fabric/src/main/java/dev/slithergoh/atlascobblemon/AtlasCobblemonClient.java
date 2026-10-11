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
   int x=7,y=7;draw.fill(x,y,x+186,y+19+Math.min(visible.size(),9)*13,0xB6090A10);
   draw.fill(x,y,x+3,y+19+Math.min(visible.size(),9)*13,0xFFD52C4D);
   draw.drawTextWithShadow(mc.textRenderer,"ATLAS  •  COBBLEMON",x+9,y+5,0xFFF6D8DF);
   int i=0;for(var p:visible){if(i++>=9)break;int col=p.shiny()?0xFF6CE8E8:p.rare()?0xFFF1B4B4:0xFFE2DCE1;draw.drawTextWithShadow(mc.textRenderer,p.name()+"  Lv."+p.level()+"  "+Math.round(p.distance())+"m",x+9,y+9+i*13,col);}
  });
 }
}

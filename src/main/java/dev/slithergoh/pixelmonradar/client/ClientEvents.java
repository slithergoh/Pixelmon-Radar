package dev.slithergoh.pixelmonradar.client;

import dev.slithergoh.pixelmonradar.client.screen.OreScreen;
import dev.slithergoh.pixelmonradar.client.screen.AtlasClickScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid="pixelmonradar",value=Dist.CLIENT)
public final class ClientEvents {
 @SubscribeEvent public static void tick(ClientTickEvent.Post event){Minecraft mc=Minecraft.getInstance();while(ClientKeys.OPEN_RADAR.consumeClick())mc.setScreen(new AtlasClickScreen());while(ClientKeys.OPEN_ORES.consumeClick())mc.setScreen(new AtlasClickScreen(true));while(ClientKeys.TOGGLE_HUD.consumeClick())RadarSettings.hudEnabled=!RadarSettings.hudEnabled;while(ClientKeys.TOGGLE_ORES.consumeClick())RadarSettings.oreEspEnabled=!RadarSettings.oreEspEnabled;OreScanner.tick();RareAlerts.tick();}
}

package dev.slithergoh.pixelmonradar.compat.journeymap;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import dev.slithergoh.pixelmonradar.client.RadarSettings;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.event.EntityRadarUpdateEvent;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.event.ClientEventRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
@JourneyMapPlugin(apiVersion="2.0.0")
public final class AtlasJourneyMapPlugin implements IClientPlugin{
 @Override public void initialize(IClientAPI api){ClientEventRegistry.ENTITY_RADAR_UPDATE_EVENT.subscribe("pixelmonradar",this::radar);}
 @Override public String getModId(){return "pixelmonradar";}
 private void radar(EntityRadarUpdateEvent event){
  var wrapped=event.getWrappedEntity();Entity e=wrapped.getEntityRef().get();if(!(e instanceof PixelmonEntity pe))return;
  var p=pe.getPokemon();if(p==null){wrapped.setDisable(true);return;}
  if(RadarSettings.hideOwnedPokemon&&pe.getOwnerUUID()!=null){wrapped.setDisable(true);return;}
  boolean shiny=p.isShiny(),legendary=p.isLegendary(true),mythical=p.isMythical(),ub=p.isUltraBeast(),paradox=p.isParadox(),boss=pe.isBossPokemon();
  boolean seen=false,caught=false;try{var mc=net.minecraft.client.Minecraft.getInstance();if(mc.player!=null){var dex=((com.pixelmonmod.pixelmon.api.extensions.PixelmonPlayerExtension)mc.player).getPokedexNow();if(dex!=null){seen=dex.hasSeen(p);caught=dex.hasCaught(p);}}}catch(Exception ignored){}
  if(RadarSettings.uncaughtOnly&&caught){wrapped.setDisable(true);return;}if(RadarSettings.unseenOnly&&seen){wrapped.setDisable(true);return;}
  boolean any=RadarSettings.shinyOnly||RadarSettings.rareOnly||RadarSettings.filterLegendary||RadarSettings.filterMythical||RadarSettings.filterUltraBeast||RadarSettings.filterParadox||RadarSettings.filterBoss;
  if(any){boolean match=(RadarSettings.shinyOnly&&shiny)||(RadarSettings.rareOnly&&(shiny||legendary||mythical||ub||paradox||boss))||(RadarSettings.filterLegendary&&legendary)||(RadarSettings.filterMythical&&mythical)||(RadarSettings.filterUltraBeast&&ub)||(RadarSettings.filterParadox&&paradox)||(RadarSettings.filterBoss&&boss);if(!match){wrapped.setDisable(true);return;}}
  String q=RadarSettings.search==null?"":RadarSettings.search.trim();if(!q.isEmpty()&&!pe.getLocalizedName().getString().toLowerCase(java.util.Locale.ROOT).contains(q.toLowerCase(java.util.Locale.ROOT))){wrapped.setDisable(true);return;}
  wrapped.setEntityIconLocation(p.getSprite());wrapped.setCustomName(Component.literal(pe.getLocalizedName().getString()+"  Lv."+p.getPokemonLevel()));
  int color=shiny?0x62FFFF:(legendary||mythical)?0xFFB64F:(ub||paradox)?0xB987FF:boss?0xFF6666:!caught?0x62D7FF:0xFFFFFF;
  wrapped.setColor(color);wrapped.setLabelColor(color);wrapped.setDrawOutline(shiny||legendary||mythical||ub||paradox||boss||!caught);
  wrapped.setEntityToolTips(java.util.List.of(Component.literal((caught?"CAUGHT":seen?"SEEN":"NEW")+"  •  "+Math.round(e.distanceTo(net.minecraft.client.Minecraft.getInstance().player))+"m"),Component.literal((shiny?"SHINY  ":"")+(legendary?"LEGENDARY  ":"")+(mythical?"MYTHICAL  ":"")+(ub?"ULTRA BEAST  ":"")+(paradox?"PARADOX  ":"")+(boss?"BOSS":"")).withStyle(net.minecraft.ChatFormatting.GRAY)));
 }
}
package dev.slithergoh.pixelmonradar.client.screen;

import dev.slithergoh.pixelmonradar.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class RadarScreen extends Screen {
 private EditBox search; private List<PokemonInfo> visible=List.of(); private int scroll;
 public RadarScreen(){super(Component.literal("Pixelmon Radar"));}
 @Override protected void init(){int l=width/2-220;search=new EditBox(font,l,40,205,20,Component.literal("Search Pokemon"));search.setHint(Component.literal("Search Pokemon..."));search.setValue(RadarSettings.search);search.setResponder(s->RadarSettings.search=s.trim());addRenderableWidget(search);
  addRenderableWidget(Button.builder(Component.literal("Sort: "+RadarSettings.sortMode),b->{RadarSettings.SortMode[]m=RadarSettings.SortMode.values();RadarSettings.sortMode=m[(RadarSettings.sortMode.ordinal()+1)%m.length];b.setMessage(Component.literal("Sort: "+RadarSettings.sortMode));RadarSettings.save();}).bounds(l+210,40,130,20).build());
  addRenderableWidget(Button.builder(Component.literal("Range: "+RadarSettings.pokemonRange),b->{RadarSettings.pokemonRange=RadarSettings.pokemonRange>=256?32:RadarSettings.pokemonRange+32;b.setMessage(Component.literal("Range: "+RadarSettings.pokemonRange));RadarSettings.save();}).bounds(l+345,40,95,20).build());
  addRenderableWidget(toggle(l,65,105,"HUD",()->RadarSettings.hudEnabled,v->RadarSettings.hudEnabled=v));addRenderableWidget(toggle(l+110,65,105,"Shiny",()->RadarSettings.shinyOnly,v->RadarSettings.shinyOnly=v));addRenderableWidget(toggle(l+220,65,105,"Rare",()->RadarSettings.rareOnly,v->RadarSettings.rareOnly=v));addRenderableWidget(toggle(l+330,65,110,"Alerts",()->RadarSettings.alertsEnabled,v->RadarSettings.alertsEnabled=v));
 }
 private interface Get{boolean get();}private interface Set{void set(boolean v);}private Button toggle(int x,int y,int w,String n,Get g,Set s){return Button.builder(Component.literal(n+": "+onOff(g.get())),b->{s.set(!g.get());b.setMessage(Component.literal(n+": "+onOff(g.get())));RadarSettings.save();}).bounds(x,y,w,20).build();}private static String onOff(boolean v){return v?"ON":"OFF";}
 private void refresh(){ArrayList<PokemonInfo>l=new ArrayList<>(PokemonScanner.scan());String q=RadarSettings.search.toLowerCase();if(!q.isBlank())l.removeIf(p->!p.name().toLowerCase().contains(q));Comparator<PokemonInfo>c=switch(RadarSettings.sortMode){case LEVEL->Comparator.comparingInt(PokemonInfo::level).reversed();case NAME->Comparator.comparing(PokemonInfo::name,String.CASE_INSENSITIVE_ORDER);case RARITY->Comparator.comparing(PokemonInfo::rare).reversed().thenComparingDouble(PokemonInfo::distance);default->Comparator.comparingDouble(PokemonInfo::distance);};l.sort(c);visible=l;}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){renderBackground(g,mx,my,pt);refresh();int l=width/2-220;g.fill(l-8,16,l+448,height-20,0xC0101016);g.drawCenteredString(font,title,width/2,22,0xFFFFFF);super.render(g,mx,my,pt);int max=Math.max(1,(height-125)/28);scroll=Math.min(scroll,Math.max(0,visible.size()-max));int y=99;for(int i=scroll;i<visible.size()&&i<scroll+max;i++){PokemonInfo p=visible.get(i);boolean tr=p.entityId()==RadarSettings.trackedEntityId;g.fill(l,y-3,l+440,y+22,tr?0x90508050:0x60303038);int col=p.shiny()?0x55FFFF:p.rare()?0xFFD75A:0xFFFFFF;g.drawString(font,(tr?"> ":"")+p.name()+"  Lv."+p.level()+"  "+Math.round(p.distance())+"m  "+p.badges(),l+5,y,col,true);String detail="XYZ "+p.pos().getX()+" "+p.pos().getY()+" "+p.pos().getZ()+" | "+p.gender()+" | Form: "+p.form()+" | Palette: "+p.palette()+(p.boss()?" | Boss: "+p.bossTier():"");g.drawString(font,detail,l+5,y+11,0xAFAFAF,false);y+=28;}g.drawString(font,"Click a row to track | mouse wheel to scroll | "+visible.size()+" found",l,height-16,0xAAAAAA,false);}
 @Override public boolean mouseClicked(double mx,double my,int b){int l=width/2-220,max=Math.max(1,(height-125)/28),y=99;if(mx>=l&&mx<=l+440)for(int i=scroll;i<visible.size()&&i<scroll+max;i++,y+=28)if(my>=y-3&&my<=y+22){int id=visible.get(i).entityId();RadarSettings.trackedEntityId=RadarSettings.trackedEntityId==id?-1:id;return true;}return super.mouseClicked(mx,my,b);}
 @Override public boolean mouseScrolled(double mx,double my,double sx,double sy){int max=Math.max(1,(height-125)/28);scroll=Math.max(0,Math.min(Math.max(0,visible.size()-max),scroll+(sy<0?1:-1)));return true;}@Override public void onClose(){RadarSettings.save();super.onClose();}@Override public boolean isPauseScreen(){return false;}
}

package dev.slithergoh.pixelmonradar.client.screen;

import dev.slithergoh.pixelmonradar.client.OreScanner;
import dev.slithergoh.pixelmonradar.client.RadarSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public final class OreScreen extends Screen {
 private EditBox search; private List<ResourceLocation> ores=List.of(); private int scroll;
 public OreScreen(){super(Component.literal("Ore Scanner"));}
 @Override protected void init(){int l=width/2-190; ores=OreScanner.candidates(); search=new EditBox(font,l,42,240,20,Component.literal("Search blocks")); search.setHint(Component.literal("Search ores / modded blocks...")); addRenderableWidget(search);
  addRenderableWidget(Button.builder(Component.literal("ESP: "+onOff(RadarSettings.oreEspEnabled)),b->{RadarSettings.oreEspEnabled=!RadarSettings.oreEspEnabled;b.setMessage(Component.literal("ESP: "+onOff(RadarSettings.oreEspEnabled)));}).bounds(l+250,42,65,20).build());
  addRenderableWidget(Button.builder(Component.literal("Range: "+RadarSettings.oreRange),b->{RadarSettings.oreRange=RadarSettings.oreRange>=64?8:RadarSettings.oreRange+8;b.setMessage(Component.literal("Range: "+RadarSettings.oreRange));}).bounds(l+320,42,70,20).build()); }
 private static String onOff(boolean v){return v?"ON":"OFF";}
 private List<ResourceLocation> filtered(){String q=search==null?"":search.getValue().toLowerCase(); return ores.stream().filter(x->x.toString().toLowerCase().contains(q)).toList();}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){renderBackground(g,mx,my,pt); int l=width/2-190; g.fill(l-8,18,l+398,height-22,0xB0121218);g.drawCenteredString(font,title,width/2,24,0xFFFFFF);super.render(g,mx,my,pt); List<ResourceLocation> a=filtered();int max=Math.max(1,(height-100)/18);scroll=Math.min(scroll,Math.max(0,a.size()-max));int y=75;for(int i=scroll;i<a.size()&&i<scroll+max;i++){ResourceLocation id=a.get(i);boolean sel=RadarSettings.selectedOres.contains(id.toString());g.fill(l,y-2,l+390,y+14,sel?0x80508050:0x50303038);g.drawString(font,(sel?"[x] ":"[ ] ")+id,l+5,y+1,sel?0x80FF80:0xFFFFFF,false);y+=18;}g.drawString(font,RadarSettings.selectedOres.size()+" selected | mouse wheel to scroll",l,height-17,0xAAAAAA,false);}
 @Override public boolean mouseClicked(double mx,double my,int b){int l=width/2-190;List<ResourceLocation>a=filtered();int max=Math.max(1,(height-100)/18);int y=75;if(mx>=l&&mx<=l+390)for(int i=scroll;i<a.size()&&i<scroll+max;i++,y+=18)if(my>=y-2&&my<=y+14){String id=a.get(i).toString();if(!RadarSettings.selectedOres.remove(id))RadarSettings.selectedOres.add(id);return true;}return super.mouseClicked(mx,my,b);}
 @Override public boolean mouseScrolled(double mx,double my,double sx,double sy){List<ResourceLocation>a=filtered();int max=Math.max(1,(height-100)/18);scroll=Math.max(0,Math.min(Math.max(0,a.size()-max),scroll+(sy<0?1:-1)));return true;}
 @Override public boolean isPauseScreen(){return false;}
}

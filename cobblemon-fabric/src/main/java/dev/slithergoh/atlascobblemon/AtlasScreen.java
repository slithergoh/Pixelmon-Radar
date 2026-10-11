package dev.slithergoh.atlascobblemon;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.*;
public final class AtlasScreen extends Screen {
 private enum Tab{RADAR,TARGETS,BLOCKS,HUD,SETTINGS}
 private Tab tab=Tab.RADAR;
 private TextFieldWidget search;
 private int scroll=0;
 private final int RED=0xFFDB3858,BG=0xF20E0D12,PANEL=0xFF201921;
 public AtlasScreen(){super(Text.literal("Atlas Cobblemon"));}
 @Override protected void init(){search=new TextFieldWidget(textRenderer,width/2-190,height/2-61,380,20,Text.literal("Search"));search.setChangedListener(s->scroll=0);addDrawableChild(search);sync();}
 private void sync(){search.setVisible(tab==Tab.TARGETS||tab==Tab.BLOCKS);search.setFocused(false);}
 private int left(){return width/2-210;}private int top(){return height/2-150;}
 private void line(DrawContext g,String s,int x,int y,int color){g.drawTextWithShadow(textRenderer,s,x,y,color);}
 @Override public void render(DrawContext g,int mx,int my,float delta){
  renderBackground(g,mx,my,delta);int x=left(),y=top();g.fill(x,y,x+420,y+300,BG);g.fill(x,y,x+420,y+3,RED);
  line(g,"ATLAS  /  COBBLEMON",x+14,y+13,0xFFF5E9ED);line(g,"Fabric 1.21.1",x+316,y+13,0xFFB29BA4);
  int j=0;for(Tab t:Tab.values()){int xx=x+12+j++*80;g.fill(xx,y+36,xx+76,y+59,tab==t?0xFF76243A:PANEL);line(g,t.name(),xx+8,y+43,0xFFF6E5EB);}
  int yy=y+68;AtlasSettings s=AtlasSettings.INSTANCE;
  if(tab==Tab.RADAR){
   line(g,"NEARBY POKEMON  /  "+AtlasCobblemonClient.visible.size(),x+14,yy,RED);
   int i=0;for(var p:AtlasCobblemonClient.visible){if(i>=12)break;int py=yy+17+i++*16;g.fill(x+12,py,x+407,py+15,i%2==0?PANEL:0xFF19151C);line(g,(AtlasSettings.INSTANCE.trackedId==p.id()?"[TRACK] ":"")+p.name()+"   Lv."+p.level()+"   "+Math.round(p.distance())+"m"+(p.shiny()?"  SHINY":p.legendary()?"  LEGENDARY":p.mythical()?"  MYTHICAL":""),x+18,py+3,p.rare()?0xFFF8A8B8:0xFFEAE2E7);}
  }else if(tab==Tab.TARGETS){
   line(g,"POKEMON TARGETS   /   "+s.targets.size()+" WATCHED",x+14,yy,RED);
   List<String> matches=SpeciesIndex.search(search.getText());
   if(matches.isEmpty())line(g,"No species loaded. Join a Cobblemon world first.",x+16,yy+58,0xFFE3A5B7);
   int cols=5,shown=0;for(int i=scroll;i<matches.size()&&shown<15;i++,shown++){
    String name=matches.get(i);int cx=x+12+(shown%cols)*79,cy=yy+49+(shown/cols)*65;
    boolean selected=s.targets.contains(name.toLowerCase(Locale.ROOT));
    g.fill(cx,cy,cx+75,cy+61,selected?0xFF652438:PANEL);
    g.fill(cx,cy,cx+75,cy+2,selected?0xFFFF5277:0xFF3C2934);
    String label=name.length()>12?name.substring(0,11)+"…":name;
    line(g,label,cx+5,cy+46,selected?0xFFFFC3D0:0xFFE8DEE3);
    line(g,selected?"WATCHED":"+ ADD",cx+8,cy+14,selected?0xFFF4A3B8:0xFFAD8999);
   }
   line(g,"Click a species to toggle wild alerts",x+14,y+281,0xFFAE98A2);
  }else if(tab==Tab.BLOCKS){
   line(g,"BLOCK ESP   /   "+(s.esp?"ENABLED":"DISABLED")+"  (click to toggle)",x+14,yy,s.esp?0xFF83D1A0:RED);
   String q=search.getText().toLowerCase(Locale.ROOT);int i=0,skipped=0;
   for(Identifier id:Registries.BLOCK.getIds()){
    if(!id.toString().toLowerCase(Locale.ROOT).contains(q)&&!Registries.BLOCK.get(id).getName().getString().toLowerCase(Locale.ROOT).contains(q))continue;
    if(skipped++<scroll)continue;if(i>=9)break;
    int py=yy+48+i++*21;g.fill(x+12,py,x+407,py+20,s.blocks.contains(id.toString())?0xFF572739:PANEL);
    Block block=Registries.BLOCK.get(id);ItemStack stack=new ItemStack(block.asItem());if(!stack.isEmpty())g.drawItem(stack,x+17,py+2);
    line(g,block.getName().getString(),x+40,py+6,0xFFE9DFE5);line(g,s.blocks.contains(id.toString())?"ON":"OFF",x+373,py+6,0xFFDF9FAE);
   }
  }else{
   line(g,"SETTINGS",x+14,yy,RED);
   String[] opts={"HUD: "+s.hud,"BLOCK ESP: "+s.esp,"RARE ALERTS: "+s.alerts,"ALERT SOUNDS: "+s.alertSound,"HIDE OWNED: "+s.hideOwned,"SHINY ONLY: "+s.shinyOnly,"RARE ONLY: "+s.rareOnly,"POKEMON RANGE: "+s.pokemonRange+"m","BLOCK RANGE: "+s.blockRange+"m"};
   for(int i=0;i<opts.length;i++){int py=yy+20+i*21;g.fill(x+12,py,x+407,py+19,PANEL);line(g,opts[i],x+19,py+5,0xFFF2E6EA);}
  }
  super.render(g,mx,my,delta);
 }
 @Override public boolean mouseClicked(double mx,double my,int button){
  int x=left(),y=top();if(my>=y+36&&my<y+59&&mx>=x+12&&mx<x+416){int i=(int)(mx-x-12)/80;if(i>=0&&i<Tab.values().length){tab=Tab.values()[i];scroll=0;sync();return true;}}
  AtlasSettings s=AtlasSettings.INSTANCE;int yy=y+68;
  if(tab==Tab.BLOCKS&&my>=yy&&my<yy+20){s.esp=!s.esp;AtlasSettings.save();return true;}
  if(tab==Tab.BLOCKS&&my>=yy+48&&my<yy+48+9*21){int row=((int)my-yy-48)/21;String q=search.getText().toLowerCase(Locale.ROOT);int i=0;for(Identifier id:Registries.BLOCK.getIds()){Block b=Registries.BLOCK.get(id);if(!id.toString().toLowerCase(Locale.ROOT).contains(q)&&!b.getName().getString().toLowerCase(Locale.ROOT).contains(q))continue;if(i++<scroll)continue;if(i-scroll-1==row){if(!s.blocks.add(id.toString()))s.blocks.remove(id.toString());AtlasSettings.save();return true;}}}
  if(tab==Tab.RADAR&&my>=yy+17&&my<yy+17+12*16){int row=((int)my-yy-17)/16;if(row>=0&&row<AtlasCobblemonClient.visible.size()){s.trackedId=AtlasCobblemonClient.visible.get(row).id();AtlasSettings.save();return true;}}
  if(tab==Tab.HUD&&my>=yy+58&&my<yy+91){client.setScreen(new AtlasHudEditor());return true;}
  if(tab==Tab.TARGETS&&my>=yy+49&&my<yy+49+3*65){int col=((int)mx-x-12)/79,row=((int)my-yy-49)/65;if(col>=0&&col<5&&row>=0&&row<3){int idx=scroll+row*5+col;List<String> matches=SpeciesIndex.search(search.getText());if(idx<matches.size()){String key=matches.get(idx).toLowerCase(Locale.ROOT);if(!s.targets.add(key))s.targets.remove(key);AtlasSettings.save();return true;}}}
  if(tab==Tab.SETTINGS&&my>=yy+20&&my<yy+20+9*21){int row=((int)my-yy-20)/21;switch(row){case 0->s.hud=!s.hud;case 1->s.esp=!s.esp;case 2->s.alerts=!s.alerts;case 3->s.alertSound=!s.alertSound;case 4->s.hideOwned=!s.hideOwned;case 5->s.shinyOnly=!s.shinyOnly;case 6->s.rareOnly=!s.rareOnly;case 7->s.pokemonRange=s.pokemonRange>=256?32:s.pokemonRange+32;case 8->s.blockRange=s.blockRange>=48?8:s.blockRange+8;}AtlasSettings.save();return true;}
  return super.mouseClicked(mx,my,button);
 }
 @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){if(tab==Tab.BLOCKS||tab==Tab.TARGETS){scroll=Math.max(0,scroll+(vertical<0?(tab==Tab.TARGETS?5:3):-(tab==Tab.TARGETS?5:3)));return true;}return super.mouseScrolled(mx,my,horizontal,vertical);}
}

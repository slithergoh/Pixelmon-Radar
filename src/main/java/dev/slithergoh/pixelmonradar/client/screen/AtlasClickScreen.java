package dev.slithergoh.pixelmonradar.client.screen;
import dev.slithergoh.pixelmonradar.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
public final class AtlasClickScreen extends Screen{
 private enum Tab{MODULES,RADAR,FILTERS,HUD}
 private Tab tab=Tab.MODULES;
 private static int savedX=-1,savedY=-1;
 private int x,y,w=344,h=278,dragX,dragY,scroll;
 private boolean dragging;
 private List<PokemonInfo>pokemon=List.of();
 public AtlasClickScreen(){super(Component.literal("Atlas ClickGUI"));}
 @Override protected void init(){w=Math.min(344,width-12);h=Math.min(278,height-12);x=savedX<0?(width-w)/2:Math.max(0,Math.min(width-w,savedX));y=savedY<0?(height-h)/2:Math.max(0,Math.min(height-h,savedY));}
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){pokemon=PokemonScanner.scan();rect(g,x,y,w,h,0xF30B1219);rect(g,x,y,w,28,0xFF172A35);g.drawString(font,"ATLAS",x+12,y+10,0xFFFFFFFF,true);g.drawString(font,"DRAG TO MOVE",x+w-94,y+10,0xFF7895A5,false);
  String[]tabs={"MODULES","RADAR","FILTERS","HUD"};int tx=x+8;for(int i=0;i<tabs.length;i++){int tw=(w-16)/4;rect(g,tx+i*tw,y+34,tw-4,24,tab.ordinal()==i?0xFF24546B:0xFF16242E);g.drawCenteredString(font,tabs[i],tx+i*tw+(tw-4)/2,y+42,tab.ordinal()==i?0xFFFFFFFF:0xFF8DA7B4);}
  int top=y+66;g.enableScissor(x+8,top,x+w-8,y+h-26);
  switch(tab){case MODULES->{module(g,top,"Pokemon Radar","Nearby Pokemon and tracking",0);module(g,top+48,"Block ESP","Blocks, loot and ores",1);module(g,top+96,"HUD Overlay","Compact in-game information",2);module(g,top+144,"Rare Alerts","Notifications for rare spawns",3);}
  case RADAR->{g.drawString(font,pokemon.size()+" Pokemon nearby",x+15,top+6,0xFFB4D9E8,false);int max=Math.min(pokemon.size(),Math.max(0,(h-100)/34));for(int i=0;i<max;i++){PokemonInfo p=pokemon.get(i);int yy=top+23+i*34;boolean tracked=RadarSettings.trackedEntityId==p.entityId();rect(g,x+12,yy,w-24,30,tracked?0xFF245341:0xFF17242D);if(p.sprite()!=null)g.blit(p.sprite(),x+18,yy+3,0,0,24,24,24,24);String name=font.plainSubstrByWidth(p.name(),w-156);g.drawString(font,name,x+48,yy+5,p.rare()?0xFFFFC477:0xFFEAF4F7,true);g.drawString(font,"Lv."+p.level()+"  "+Math.round(p.distance())+"m",x+48,yy+17,0xFF92A9B5,false);g.drawString(font,tracked?"TRACKING":"TRACK",x+w-77,yy+11,tracked?0xFF80E9B6:0xFF6EC9EB,false);}} 
  case FILTERS->{String[]names={"Shiny only","Legendary","Mythical","Ultra Beast","Paradox","Boss","Not caught","Not seen","Hide owned"};boolean[]values={RadarSettings.shinyOnly,RadarSettings.filterLegendary,RadarSettings.filterMythical,RadarSettings.filterUltraBeast,RadarSettings.filterParadox,RadarSettings.filterBoss,RadarSettings.uncaughtOnly,RadarSettings.unseenOnly,RadarSettings.hideOwnedPokemon};for(int i=scroll;i<names.length&&i<scroll+5;i++)option(g,top+(i-scroll)*35,names[i],values[i]);}
  case HUD->{option(g,top,"Radar HUD",RadarSettings.hudEnabled);option(g,top+35,"Rare alerts",RadarSettings.alertsEnabled);option(g,top+70,"Alert sounds",RadarSettings.alertSound);option(g,top+105,"Raid card",RadarSettings.raidHud);option(g,top+140,"Party status",RadarSettings.partyHud);}}
  g.disableScissor();g.drawString(font,"ESC close   |   Right-click module: options",x+12,y+h-17,0xFF79919D,false);}
 private void module(GuiGraphics g,int yy,String name,String sub,int index){rect(g,x+12,yy,w-24,43,0xFF17242D);g.drawString(font,name,x+22,yy+9,0xFFF1F8FB,true);g.drawString(font,sub,x+22,yy+24,0xFF829BA8,false);g.drawString(font,index==1?(RadarSettings.oreEspEnabled?"ON":"OFF"):index==2?(RadarSettings.hudEnabled?"ON":"OFF"):index==3?(RadarSettings.alertsEnabled?"ON":"OFF"):"OPEN",x+w-52,yy+17,0xFF6BD4EF,false);}
 private void option(GuiGraphics g,int yy,String name,boolean on){rect(g,x+12,yy,w-24,30,0xFF17242D);g.drawString(font,name,x+23,yy+11,0xFFE7F2F6,false);rect(g,x+w-52,yy+7,31,16,on?0xFF26775E:0xFF354550);rect(g,x+w-(on?37:49),yy+10,10,10,0xFFF2F7FA);}
 private static void rect(GuiGraphics g,int x,int y,int w,int h,int c){g.fill(x,y,x+w,y+h,c);}
 @Override public boolean mouseClicked(double mx,double my,int button){if(my>=y&&my<y+28&&mx>=x&&mx<=x+w&&button==0){dragging=true;dragX=(int)mx-x;dragY=(int)my-y;return true;}if(my>=y+34&&my<y+58&&mx>=x+8&&mx<x+w-8){int i=((int)mx-x-8)/((w-16)/4);if(i>=0&&i<4){tab=Tab.values()[i];scroll=0;return true;}}int top=y+66;if(mx<x+12||mx>x+w-12||my<top||my>y+h-26)return super.mouseClicked(mx,my,button);int i=((int)my-top)/(tab==Tab.MODULES?48:tab==Tab.RADAR?34:35);if(tab==Tab.MODULES&&i>=0&&i<4){if(i==0)tab=Tab.RADAR;else if(i==1){minecraft.setScreen(new OreScreen());return true;}else if(i==2)RadarSettings.hudEnabled=!RadarSettings.hudEnabled;else RadarSettings.alertsEnabled=!RadarSettings.alertsEnabled;}
 else if(tab==Tab.RADAR){int index=((int)my-top-23)/34;if(my>=top+23&&index>=0&&index<pokemon.size()){int id=pokemon.get(index).entityId();RadarSettings.trackedEntityId=RadarSettings.trackedEntityId==id?-1:id;}}
 else if(tab==Tab.FILTERS){switch(i+scroll){case 0->RadarSettings.shinyOnly=!RadarSettings.shinyOnly;case 1->RadarSettings.filterLegendary=!RadarSettings.filterLegendary;case 2->RadarSettings.filterMythical=!RadarSettings.filterMythical;case 3->RadarSettings.filterUltraBeast=!RadarSettings.filterUltraBeast;case 4->RadarSettings.filterParadox=!RadarSettings.filterParadox;case 5->RadarSettings.filterBoss=!RadarSettings.filterBoss;case 6->RadarSettings.uncaughtOnly=!RadarSettings.uncaughtOnly;case 7->RadarSettings.unseenOnly=!RadarSettings.unseenOnly;case 8->RadarSettings.hideOwnedPokemon=!RadarSettings.hideOwnedPokemon;}}
 else if(tab==Tab.HUD){switch(i){case 0->RadarSettings.hudEnabled=!RadarSettings.hudEnabled;case 1->RadarSettings.alertsEnabled=!RadarSettings.alertsEnabled;case 2->RadarSettings.alertSound=!RadarSettings.alertSound;case 3->RadarSettings.raidHud=!RadarSettings.raidHud;case 4->RadarSettings.partyHud=!RadarSettings.partyHud;}}RadarSettings.save();return true;}
 @Override public boolean mouseDragged(double mx,double my,int b,double dx,double dy){if(dragging){x=Math.max(0,Math.min(width-w,(int)mx-dragX));y=Math.max(0,Math.min(height-h,(int)my-dragY));return true;}return super.mouseDragged(mx,my,b,dx,dy);}
 @Override public boolean mouseReleased(double mx,double my,int b){if(dragging){dragging=false;savedX=x;savedY=y;return true;}return super.mouseReleased(mx,my,b);}
 @Override public boolean mouseScrolled(double mx,double my,double dx,double dy){if(tab==Tab.FILTERS){scroll=Math.max(0,Math.min(4,scroll+(dy<0?1:-1)));return true;}return super.mouseScrolled(mx,my,dx,dy);}
 @Override public void onClose(){savedX=x;savedY=y;RadarSettings.save();super.onClose();}
 @Override public boolean isPauseScreen(){return false;}
}
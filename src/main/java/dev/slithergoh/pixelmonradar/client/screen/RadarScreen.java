package dev.slithergoh.pixelmonradar.client.screen;

import dev.slithergoh.pixelmonradar.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class RadarScreen extends Screen {
 private enum Tab { NEARBY, FILTERS, SETTINGS }
 private EditBox search; private Tab tab=Tab.NEARBY; private List<PokemonInfo> visible=List.of(); private int scroll;
 private int px,py,pw,ph; private long opened=System.currentTimeMillis();
 public RadarScreen(){super(Component.literal("Atlas"));}

 @Override protected void init(){
  pw=Math.min(430,width-28); ph=Math.min(318,height-28); px=(width-pw)/2; py=(height-ph)/2;
  search=new EditBox(font,px+16,py+48,pw-32,20,Component.literal("Search Pokemon"));
  search.setHint(Component.literal("Search nearby Pokemon..."));search.setValue(RadarSettings.search);
  search.setResponder(v->{RadarSettings.search=v.trim();scroll=0;});addRenderableWidget(search);
 }
 private void refresh(){
  ArrayList<PokemonInfo> l=new ArrayList<>(PokemonScanner.scan());String q=RadarSettings.search.toLowerCase();
  if(!q.isBlank())l.removeIf(p->!p.name().toLowerCase().contains(q));
  Comparator<PokemonInfo> c=switch(RadarSettings.sortMode){case LEVEL->Comparator.comparingInt(PokemonInfo::level).reversed();case NAME->Comparator.comparing(PokemonInfo::name,String.CASE_INSENSITIVE_ORDER);case RARITY->Comparator.comparingInt(this::priority).reversed().thenComparingDouble(PokemonInfo::distance);default->Comparator.comparingDouble(PokemonInfo::distance);};l.sort(c);visible=l;
 }
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  refresh();float anim=Math.min(1f,(System.currentTimeMillis()-opened)/180f);int off=(int)((1-anim)*8);
  int y0=py+off;g.fill(px,y0,px+pw,y0+ph,0xF20A1118);g.fill(px,y0,px+pw,y0+34,0xFF102A3A);g.fill(px,y0,px+3,y0+ph,0xFF42B7E8);
  g.drawString(font,"ATLAS",px+14,y0+10,0xFFFFFF,true);g.drawString(font,"LIVE RADAR",px+53,y0+10,0xFF79CFF1,false);
  g.drawString(font,visible.size()+" DETECTED",px+pw-75,y0+10,0xFFA8BBC5,false);
  drawTab(g,px+14,y0+35,78,"NEARBY",tab==Tab.NEARBY);drawTab(g,px+96,y0+35,78,"FILTERS",tab==Tab.FILTERS);drawTab(g,px+178,y0+35,82,"SETTINGS",tab==Tab.SETTINGS);
  super.render(g,mx,my,pt);
  if(tab==Tab.NEARBY)renderNearby(g,y0,mx,my);else if(tab==Tab.FILTERS)renderFilters(g,y0);else renderSettings(g,y0);
  g.drawString(font,"R close  •  click a Pokemon to track",px+14,y0+ph-14,0xFF718894,false);
 }
 private void renderNearby(GuiGraphics g,int y0,int mx,int my){
  int top=y0+76,row=42,max=Math.max(1,(ph-105)/row);scroll=Math.min(scroll,Math.max(0,visible.size()-max));
  if(visible.isEmpty()){g.drawCenteredString(font,"No Pokemon match the current radar filters",px+pw/2,top+55,0xFF8298A4);return;}
  for(int i=scroll;i<visible.size()&&i<scroll+max;i++){PokemonInfo p=visible.get(i);int y=top+(i-scroll)*row;boolean tr=p.entityId()==RadarSettings.trackedEntityId,hover=mx>=px+12&&mx<=px+pw-12&&my>=y&&my<=y+37;
   int accent=accent(p);g.fill(px+12,y,px+pw-12,y+37,tr?0xE0244C43:hover?0xD01B2B36:0xB9142029);g.fill(px+12,y,px+15,y+37,accent);
   if(p.sprite()!=null)g.blit(p.sprite(),px+20,y+3,0,0,31,31,31,31);
   g.drawString(font,p.name(),px+58,y+5,p.shiny()?0xFF6FFFFF:p.rare()?0xFFFFD76A:0xFFF2F5F7,true);
   g.drawString(font,"Lv."+p.level()+"  •  "+Math.round(p.distance())+"m  •  "+direction(p),px+58,y+18,0xFFAFC0C8,false);
   String badge=badge(p);if(!badge.isEmpty()){int bw=font.width(badge)+8;g.fill(px+pw-88,y+5,px+pw-88+bw,y+17,(accent&0x00FFFFFF)|0x55000000);g.drawString(font,badge,px+pw-84,y+7,accent,false);}
   g.drawString(font,tr?"TRACKING":"›",px+pw-55,y+23,tr?0xFF7CFFBA:0xFF78909C,false);
  }
 }
 private void renderFilters(GuiGraphics g,int y0){
  int y=y0+84;g.drawString(font,"QUICK FILTERS",px+18,y,0xFF8CA5B2,false);y+=17;
  chip(g,px+18,y,110,"SHINY",RadarSettings.shinyOnly,0xFF62FFFF);chip(g,px+134,y,110,"RARE+",RadarSettings.rareOnly,0xFFFFD66B);y+=31;
  g.drawString(font,"Filters combine intelligently: enabling Shiny + Rare shows either match.",px+18,y,0xFF78909C,false);y+=27;
  g.drawString(font,"SORT ORDER",px+18,y,0xFF8CA5B2,false);y+=16;
  RadarSettings.SortMode[] modes=RadarSettings.SortMode.values();int x=px+18;for(RadarSettings.SortMode m:modes){int w=font.width(m.name())+18;chip(g,x,y,w,m.name(),RadarSettings.sortMode==m,0xFF65C8EF);x+=w+6;}
  y+=35;g.drawString(font,"Radar prioritises rare encounters visually without hiding normal spawns.",px+18,y,0xFF78909C,false);
 }
 private void renderSettings(GuiGraphics g,int y0){
  int y=y0+82;setting(g,y,"HUD","Compact live nearby panel",RadarSettings.hudEnabled);y+=35;setting(g,y,"ALERTS","Notify when rare Pokemon appear",RadarSettings.alertsEnabled);y+=35;setting(g,y,"SOUND","Rare encounter audio cue",RadarSettings.alertSound);y+=43;
  g.drawString(font,"SCAN RANGE",px+18,y,0xFF8CA5B2,false);g.drawString(font,RadarSettings.pokemonRange+"m",px+pw-52,y,0xFF8DDBF7,true);y+=15;
  g.fill(px+18,y,px+pw-18,y+4,0xFF243641);int fill=(int)((pw-36)*Math.min(1,RadarSettings.pokemonRange/256f));g.fill(px+18,y,px+18+fill,y+4,0xFF48BCE8);
  g.drawString(font,"Click the range bar to change 32–256m",px+18,y+12,0xFF718894,false);
 }
 private void setting(GuiGraphics g,int y,String name,String desc,boolean on){g.drawString(font,name,px+18,y,on?0xFFF3F7F9:0xFF91A1A9,true);g.drawString(font,desc,px+18,y+12,0xFF718894,false);g.fill(px+pw-58,y+3,px+pw-18,y+18,on?0xFF286A58:0xFF293841);g.drawCenteredString(font,on?"ON":"OFF",px+pw-38,y+7,on?0xFF83FFC1:0xFF8A9AA2);}
 private void drawTab(GuiGraphics g,int x,int y,int w,String s,boolean a){g.fill(x,y,x+w,y+20,a?0xFF214B61:0x00111111);if(a)g.fill(x,y+18,x+w,y+20,0xFF58C7EF);g.drawCenteredString(font,s,x+w/2,y+6,a?0xFFFFFFFF:0xFF8299A5);}
 private void chip(GuiGraphics g,int x,int y,int w,String s,boolean a,int col){g.fill(x,y,x+w,y+22,a?(col&0x00FFFFFF)|0x66000000:0xFF17252E);g.drawCenteredString(font,s,x+w/2,y+7,a?col:0xFF8BA0AA);}
 private int priority(PokemonInfo p){if(p.shiny())return 100;if(p.legendary()||p.mythical())return 90;if(p.ultraBeast()||p.paradox())return 80;if(p.boss())return 70;return 0;}
 private int accent(PokemonInfo p){if(p.shiny())return 0xFF62FFFF;if(p.legendary()||p.mythical())return 0xFFFFB64F;if(p.ultraBeast()||p.paradox())return 0xFFB987FF;if(p.boss())return 0xFFFF6666;return 0xFF4EB7DD;}
 private String badge(PokemonInfo p){if(p.shiny())return "★ SHINY";if(p.legendary())return "◆ LEGEND";if(p.mythical())return "◆ MYTHIC";if(p.ultraBeast())return "UB";if(p.paradox())return "PARADOX";if(p.boss())return "BOSS";return "";}
 private String direction(PokemonInfo p){var mc=minecraft;double dx=p.pos().getX()+.5-mc.player.getX(),dz=p.pos().getZ()+.5-mc.player.getZ();double a=(Math.toDegrees(Math.atan2(-dx,dz))+360)%360;String[]d={"N","NE","E","SE","S","SW","W","NW"};return d[(int)Math.round(a/45)%8];}
 @Override public boolean mouseClicked(double mx,double my,int b){
  int y0=py;if(my>=y0+35&&my<=y0+55){if(mx>=px+14&&mx<=px+92){tab=Tab.NEARBY;return true;}if(mx>=px+96&&mx<=px+174){tab=Tab.FILTERS;return true;}if(mx>=px+178&&mx<=px+260){tab=Tab.SETTINGS;return true;}}
  if(tab==Tab.NEARBY){int top=y0+76,row=42,max=Math.max(1,(ph-105)/row);if(mx>=px+12&&mx<=px+pw-12&&my>=top)for(int i=scroll;i<visible.size()&&i<scroll+max;i++){int y=top+(i-scroll)*row;if(my>=y&&my<=y+37){int id=visible.get(i).entityId();RadarSettings.trackedEntityId=RadarSettings.trackedEntityId==id?-1:id;RadarSettings.save();return true;}}}
  if(tab==Tab.FILTERS){if(my>=y0+101&&my<=y0+123){if(mx>=px+18&&mx<=px+128){RadarSettings.shinyOnly=!RadarSettings.shinyOnly;RadarSettings.save();return true;}if(mx>=px+134&&mx<=px+244){RadarSettings.rareOnly=!RadarSettings.rareOnly;RadarSettings.save();return true;}}int y=y0+159,x=px+18;for(RadarSettings.SortMode m:RadarSettings.SortMode.values()){int w=font.width(m.name())+18;if(my>=y&&my<=y+22&&mx>=x&&mx<=x+w){RadarSettings.sortMode=m;RadarSettings.save();return true;}x+=w+6;}}
  if(tab==Tab.SETTINGS){if(mx>=px+pw-65&&mx<=px+pw-10){if(my>=y0+80&&my<=y0+105)RadarSettings.hudEnabled=!RadarSettings.hudEnabled;else if(my>=y0+115&&my<=y0+140)RadarSettings.alertsEnabled=!RadarSettings.alertsEnabled;else if(my>=y0+150&&my<=y0+175)RadarSettings.alertSound=!RadarSettings.alertSound;else return rangeClick(mx,my,y0);RadarSettings.save();return true;}return rangeClick(mx,my,y0);}
  return super.mouseClicked(mx,my,b);
 }
 private boolean rangeClick(double mx,double my,int y0){int y=y0+205;if(my>=y&&my<=y+24&&mx>=px+18&&mx<=px+pw-18){double f=(mx-(px+18))/(pw-36.0);RadarSettings.pokemonRange=32+(int)Math.round(Math.max(0,Math.min(1,f))*7)*32;RadarSettings.save();return true;}return false;}
 @Override public boolean mouseScrolled(double mx,double my,double sx,double sy){if(tab!=Tab.NEARBY)return super.mouseScrolled(mx,my,sx,sy);int max=Math.max(1,(ph-105)/42);scroll=Math.max(0,Math.min(Math.max(0,visible.size()-max),scroll+(sy<0?1:-1)));return true;}
 @Override public void onClose(){RadarSettings.save();super.onClose();}@Override public boolean isPauseScreen(){return false;}
}
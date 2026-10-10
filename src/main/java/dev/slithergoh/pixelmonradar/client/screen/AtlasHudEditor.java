package dev.slithergoh.pixelmonradar.client.screen;
import dev.slithergoh.pixelmonradar.client.RadarSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
/** Drag individual HUD previews; positions match RadarHud render coordinates. */
public final class AtlasHudEditor extends Screen{
 private static final String[] LABELS={"POKEMON RADAR","PARTY STATUS","RAID CARD","TRACKED POKEMON"};
 private static final int[] WIDTHS={176,176,176,208},HEIGHTS={126,108,39,40};
 private int selected=-1,dx,dy;
 public AtlasHudEditor(){super(Component.literal("Atlas HUD Layout"));}
 private int getX(int i){return switch(i){case 0->RadarSettings.hudX;case 1->RadarSettings.partyX;case 2->RadarSettings.raidX<0?width-183:RadarSettings.raidX;default->RadarSettings.trackerX<0?(width-208)/2:RadarSettings.trackerX;};}
 private int getY(int i){return switch(i){case 0->RadarSettings.hudY;case 1->RadarSettings.partyY;case 2->RadarSettings.raidY;default->RadarSettings.trackerY;};}
 private void put(int i,int x,int y){x=Math.max(0,Math.min(width-WIDTHS[i],x));y=Math.max(0,Math.min(height-HEIGHTS[i],y));switch(i){case 0->{RadarSettings.hudX=x;RadarSettings.hudY=y;}case 1->{RadarSettings.partyX=x;RadarSettings.partyY=y;}case 2->{RadarSettings.raidX=x;RadarSettings.raidY=y;}case 3->{RadarSettings.trackerX=x;RadarSettings.trackerY=y;}}}
 @Override public void renderBackground(GuiGraphics g,int mx,int my,float pt){}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){
  g.fill(0,0,width,height,0xA808070B);
  g.drawCenteredString(font,"ATLAS  /  HUD LAYOUT",width/2,8,0xFFFFDCE5);
  g.drawCenteredString(font,"Drag each panel to position it. ESC saves and returns.",width/2,22,0xFFB6A2A9);
  for(int i=0;i<4;i++){int x=getX(i),y=getY(i),w=WIDTHS[i],h=HEIGHTS[i];boolean active=i==selected;g.fill(x-1,y-1,x+w+1,y+h+1,active?0xFFFF738F:0xFF774052);g.fill(x,y,x+w,y+h,0xE51C171F);g.fill(x,y,x+w,y+22,active?0xFF703243:0xFF38212D);g.drawString(font,LABELS[i],x+8,y+7,0xFFF9E8ED,true);g.drawString(font,"DRAG",x+w-34,y+7,0xFFCC9DAA,false);g.drawString(font,"Preview area",x+10,y+33,0xFF917B86,false);}
  super.render(g,mx,my,pt);
 }
 @Override public boolean mouseClicked(double mx,double my,int button){if(button!=0)return super.mouseClicked(mx,my,button);for(int i=3;i>=0;i--){int x=getX(i),y=getY(i);if(mx>=x&&mx<x+WIDTHS[i]&&my>=y&&my<y+HEIGHTS[i]){selected=i;dx=(int)mx-x;dy=(int)my-y;return true;}}return super.mouseClicked(mx,my,button);}
 @Override public boolean mouseDragged(double mx,double my,int button,double ddx,double ddy){if(button==0&&selected>=0){put(selected,(int)mx-dx,(int)my-dy);return true;}return super.mouseDragged(mx,my,button,ddx,ddy);}
 @Override public boolean mouseReleased(double mx,double my,int button){if(selected>=0){selected=-1;RadarSettings.save();return true;}return super.mouseReleased(mx,my,button);}
 @Override public void onClose(){RadarSettings.save();minecraft.setScreen(new AtlasClickScreen());}
 @Override public boolean isPauseScreen(){return false;}
}
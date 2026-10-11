package dev.slithergoh.atlascobblemon;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
public final class AtlasHudEditor extends Screen{
 private int dragging=-1,offX,offY;
 public AtlasHudEditor(){super(Text.literal("Atlas HUD Editor"));}
 @Override public void render(DrawContext g,int mx,int my,float delta){
  renderBackground(g,mx,my,delta);
  AtlasSettings s=AtlasSettings.INSTANCE;
  g.drawCenteredTextWithShadow(textRenderer,"ATLAS HUD EDITOR  •  Drag widgets  •  ESC to save",width/2,18,0xFFF4D9E0);
  panel(g,s.hudX,s.hudY,190,100,"POKEMON RADAR",0xFFDA3657);
  panel(g,s.trackerX,s.trackerY,190,50,"POKEMON TRACKER",0xFFCA7790);
  super.render(g,mx,my,delta);
 }
 private void panel(DrawContext g,int x,int y,int w,int h,String title,int accent){
  g.fill(x,y,x+w,y+h,0xE50F0D14);g.fill(x,y,x+3,y+h,accent);
  g.drawTextWithShadow(textRenderer,title,x+12,y+9,0xFFF7E5E9);
  g.drawTextWithShadow(textRenderer,"Drag to reposition",x+12,y+29,0xFFAD98A2);
 }
 @Override public boolean mouseClicked(double mx,double my,int button){
  AtlasSettings s=AtlasSettings.INSTANCE;
  if(mx>=s.trackerX&&mx<s.trackerX+190&&my>=s.trackerY&&my<s.trackerY+50){dragging=1;offX=(int)mx-s.trackerX;offY=(int)my-s.trackerY;return true;}
  if(mx>=s.hudX&&mx<s.hudX+190&&my>=s.hudY&&my<s.hudY+100){dragging=0;offX=(int)mx-s.hudX;offY=(int)my-s.hudY;return true;}
  return super.mouseClicked(mx,my,button);
 }
 @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){
  if(dragging<0)return super.mouseDragged(mx,my,button,dx,dy);
  int x=Math.max(0,Math.min(width-190,(int)mx-offX)),y=Math.max(0,Math.min(height-50,(int)my-offY));
  if(dragging==0){AtlasSettings.INSTANCE.hudX=x;AtlasSettings.INSTANCE.hudY=y;}else{AtlasSettings.INSTANCE.trackerX=x;AtlasSettings.INSTANCE.trackerY=y;}
  return true;
 }
 @Override public boolean mouseReleased(double mx,double my,int button){if(dragging>=0){dragging=-1;AtlasSettings.save();return true;}return super.mouseReleased(mx,my,button);}
 @Override public void close(){AtlasSettings.save();super.close();}
}

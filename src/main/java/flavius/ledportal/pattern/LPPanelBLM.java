package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.color.LXColor;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanelBLM extends LPPanel3DGraphicsPattern {
  PFont font;

  public LPPanelBLM(LX lx) {
    super(lx);
    refreshFont();
  }

  public void refreshFont() {
    font = LXStudioApp.instance.prepareFont("uni0553-webfont.ttf", 8);
  }

  @Override
  public void beforeDraw(PGraphics pg) {
    super.beforeDraw(pg);
    pg.noSmooth();
  }

  @Override
  public void onDraw(PGraphics pg) {
    pg.pushMatrix();
    pg.textFont(font);
    pg.textSize(8);
    pg.noStroke();
    applyBackground(LXColor.RED);
    pg.fill(LXColor.BLACK);
    pg.rect(0, 0, model.width * 2, model.height/2);
    pg.textAlign(PGraphics.CENTER);
    pg.fill(LXColor.rgb(255, 255, 0));
    pg.translate(0.5f, 0.f);
    pg.text("BLM", Math.round(model.width/2 + 4), model.height);
    applyShear();
    pg.translate((float) model.width + 0.5f, model.height/2.f);
    pg.sphere(3.5f);
    pg.popMatrix();
  }
}

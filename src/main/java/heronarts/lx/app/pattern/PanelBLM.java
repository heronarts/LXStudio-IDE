package heronarts.lx.app.pattern;

import heronarts.lx.color.LXColor;
import heronarts.lx.LX;

import processing.core.PGraphics;
import processing.core.PMatrix2D;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class PanelBLM extends PanelGraphicsPattern {
  public PanelBLM(LX lx) {
    super(lx);
  }

  @Override
  public void loopGraphics() {
    pg.textFont(font);
    pg.textSize(8);
    pg.noSmooth();
    pg.noStroke();
    pg.background(LXColor.RED);
    pg.fill(LXColor.BLACK);
    pg.rect(0, 0, model.width * 2, model.height/2);
    pg.textAlign(PGraphics.CENTER);
    pg.fill(LXColor.rgb(255, 255, 0));
    pg.text("BLM", model.width/2 + 4, model.height);
    pg.applyMatrix(new PMatrix2D(
      1.f, -0.5f, 0.f,
      0.f, 1.f, 0.f
    ));
    pg.circle(model.width + 1, model.height/2, 7);
  }
}

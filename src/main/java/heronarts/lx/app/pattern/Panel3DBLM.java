package heronarts.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import processing.core.PGraphics;
import processing.core.PMatrix2D;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class Panel3DBLM extends Panel3DGraphicsPattern {

  public Panel3DBLM(LX lx) {
    super(lx);
  }

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
    pg.background(LXColor.RED);
    pg.fill(LXColor.BLACK);
    pg.rect(0, 0, model.width * 2, model.height/2);
    pg.textAlign(PGraphics.CENTER);
    pg.fill(LXColor.rgb(255, 255, 0));
    pg.translate(0.5f, 0.f);
    pg.text("BLM", Math.round(model.width/2 + 4), model.height);
    pg.applyMatrix(new PMatrix2D(
      1.f, -0.5f, 0.f,
      0.f, 1.f, 0.f
    ));
    pg.translate((float) model.width + 0.5f, model.height/2.f);
    pg.sphere(3.5f);
    pg.popMatrix();
  }
}

package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import processing.core.PGraphics;
import processing.core.PMatrix3D;

public class LPPanelSolidState extends LPPanel3DGraphicsPattern {

  public final String marqueeText = "SOLIDSTATE";

  public LPPanelSolidState(LX lx) {
    super(lx);

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("size", this.scale);
    addParameter("beats", this.beats);
    addParameter("xScanFuckery", this.xScanFuckery);
    addParameter("yScanFuckery", this.yScanFuckery);
    addParameter("pScanFuckery", this.pScanFuckery);
  }

  @Override
  public void refreshFont() {
    font = this.applet.createFont(fontPrefix + "Solid-State.ttf", 96);
  }

  @Override
  public void onDraw(final PGraphics pg) {

    final float xOffset = this.xOffset.getValuef();
    final float yOffset = this.yOffset.getValuef();
    final float zOffset = this.zOffset.getValuef();
    final float xRotate = this.xRotate.getValuef();
    final float yRotate = this.yRotate.getValuef();
    final float zRotate = this.zRotate.getValuef();
    final float scale = (float) Math.pow(this.scale.getValue(), 2.d);
    final int beats = this.beats.getValuei();
    final int marqueeIndex = (this.lx.engine.tempo.beatCount()/beats) % marqueeText.length();

    pg.pushMatrix();
    pg.textFont(font);
    pg.textSize(scale * this.frameSize);
    pg.background(LXColor.BLACK);
    pg.translate( //
      /**/ xOffset * model.width, //
      /**/ yOffset * model.height, //
      /**/ zOffset * Math.max(model.width, model.height));
    pg.applyMatrix(new PMatrix3D( //
      /**/ 1.f, -0.5f, 0.f, 0.f, //
      /**/ 0.f, 1.f, 0.f, 0.f, //
      /**/ 0.f, 0.f, 1.f, 0.f, //
      /**/ 0.f, 0.f, 0.f, 1.f)); //
    pg.rotateX((float) Math.PI * xRotate);
    pg.rotateY((float) Math.PI * yRotate);
    pg.rotateZ((float) Math.PI * zRotate);
    pg.noStroke();
    pg.fill(LXColor.rgb(255, 255, 0));
    char marqueeChar = marqueeText.charAt(marqueeIndex);
    pg.text(marqueeChar, xOffset * model.width, yOffset * model.height);
    pg.popMatrix();
  }
}

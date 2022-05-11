package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.color.LXColor;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

public class LPPanelTextSolidState extends LPPanel3DGraphicsPattern {
  PFont font;
  public final String marqueeText = "SOLIDSTATE";

  public LPPanelTextSolidState(LX lx) {
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

    refreshFont();
  }

  public void refreshFont() {
    font = LXStudioApp.instance.fontLibrary.prepareMedia("Solid-State.ttf", 96);
  }

  @Override
  public void onDraw(final PGraphics pg) {
    final int beats = this.beats.getValuei();
    final int marqueeIndex = (this.lx.engine.tempo.beatCount()/beats) % marqueeText.length();

    pg.pushMatrix();
    if (font != null) pg.textFont(font);
    pg.textAlign(PConstants.CENTER, PConstants.CENTER);
    applyBackground();
    applyTranslation();
    applyShear();
    applyRotation();
    applyScale();
    pg.noStroke();
    pg.fill(LXColor.rgb(255, 255, 0));
    char marqueeChar = marqueeText.charAt(marqueeIndex);
    pg.text(marqueeChar, 0.f, 0.f);
    pg.popMatrix();
  }
}

package heronarts.lx.app.ui;

import flavius.ledportal.LPMeshable;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;
import processing.core.PVector;

public class UIBillboardText extends UI3dComponent {
  public String text;
  public PVector position;
  public float s;
  public int c;
  public int alpha;

  public UIBillboardText(String text, PVector position, float s, int c,
    int alpha) {
    super();
    this.text = text;
    this.position = position;
    this.s = s;
    this.c = c;
    this.alpha = alpha;
  }

  public UIBillboardText(String text, PVector position, float s, int c) {
    this(text, position, s, c, 100);
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    PVector uiPosition = LPMeshable.worldUITransform(this.position);
    pg.pushStyle();
    pg.textMode(PGraphics.SHAPE);
    pg.fill(this.c, this.alpha);
    pg.textSize(this.s);
    pg.text(this.text, uiPosition.x, uiPosition.y, uiPosition.z);
    pg.popStyle();
  }
}

package heronarts.lx.app.ui;

import flavius.ledportal.LPMeshable;
import heronarts.lx.color.LXColor;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;
import processing.core.PVector;

public class UIAxes extends UI3dComponent {
  public int alpha = 100;

  public UIAxes() {
    super();
    this.addChild(
      new UIBillboardText("X", LPMeshable.xAxis, 1, LXColor.RED, alpha));
    this.addChild(
      new UIBillboardText("Y", LPMeshable.yAxis, 1, LXColor.GREEN, alpha));
    this.addChild(
      new UIBillboardText("Z", LPMeshable.zAxis, 1, LXColor.BLUE, alpha));
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    pg.pushStyle();
    pg.strokeWeight(5);

    PVector uiX = LPMeshable.worldUITransform(LPMeshable.xAxis);
    PVector uiY = LPMeshable.worldUITransform(LPMeshable.yAxis);
    PVector uiZ = LPMeshable.worldUITransform(LPMeshable.zAxis);
    pg.stroke(LXColor.RED, alpha);
    pg.line(0, 0, 0, uiX.x, uiX.y, uiX.z);
    pg.stroke(LXColor.GREEN, alpha);
    pg.line(0, 0, 0, uiY.x, uiY.y, uiY.z);
    pg.stroke(LXColor.BLUE, alpha);
    pg.line(0, 0, 0, uiZ.x, uiZ.y, uiZ.z);
    pg.popStyle();
  }
}

package heronarts.lx.app.ui;

import java.util.List;

import flavius.ledportal.LPDecoration;
import flavius.ledportal.LPMeshable;
import heronarts.lx.color.LXColor;
import heronarts.p4lx.ui.UI;
import heronarts.p4lx.ui.UI3dComponent;
import processing.core.PGraphics;
import processing.core.PVector;

// Note: For some reason it's easier to render with Y ans Z swapped
public class UIWireFrame extends UI3dComponent {
  public int colour;
  public float alpha = 100;
  LPDecoration data;
  List<PVector[]> edges;

  public UIWireFrame(LPDecoration data, int colour) {
    this.data = data;
    this.edges = data.getWorldEdges();
    this.colour = colour;
  }

  public UIWireFrame(LPDecoration data) {
    this(data, LXColor.WHITE);
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    pg.pushStyle();
    pg.strokeWeight(5);
    pg.stroke(this.colour, alpha);
    for (PVector[] edge : this.edges) {
      PVector start = LPMeshable.worldUITransform(edge[0]);
      PVector end = LPMeshable.worldUITransform(edge[1]);
      pg.line(start.x, start.y, start.z, end.x, end.y, end.z);
    }
    pg.popStyle();
  }
}

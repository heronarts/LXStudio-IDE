package heronarts.lx.app.ui;

import java.util.List;

import flavius.ledportal.LPMeshable;
import flavius.ledportal.LPStructure;
import heronarts.lx.color.LXColor;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;
import processing.core.PVector;

// Note: For some reason it's easier to render with Y ans Z swapped
public class UIWireframe extends UI3dComponent {
  public int colour;
  public float alpha = 100;
  LPStructure data;
  List<PVector[]> edges;

  public UIWireframe(LPStructure data, int colour) {
    this.data = data;
    this.edges = data.getWorldEdges();
    this.colour = colour;
  }

  public UIWireframe(LPStructure data) {
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

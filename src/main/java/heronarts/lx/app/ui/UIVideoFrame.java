package heronarts.lx.app.ui;

import flavius.ledportal.LPMeshable;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import java.util.List;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class UIVideoFrame extends UI3dComponent {
  public List<float[]> vertexUVPairs;
  public PImage videoFrame;

  public UIVideoFrame(List<float[]> vertexUVPairs, PImage videoFrame) {
    this.vertexUVPairs = vertexUVPairs;
    this.videoFrame = videoFrame;
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    pg.pushStyle();
    pg.noStroke();
    pg.beginShape();
    pg.tint(255, 126);
    pg.texture(this.videoFrame);

    for (float[] vertexUVPair : this.vertexUVPairs) {
      PVector uiPosition = LPMeshable.worldUITransform(
        new PVector(vertexUVPair[0], vertexUVPair[1], vertexUVPair[2]));
      pg.vertex(uiPosition.x, uiPosition.y, uiPosition.z, vertexUVPair[3],
        vertexUVPair[4]);
    }
    pg.endShape();
    pg.popStyle();
  }
}

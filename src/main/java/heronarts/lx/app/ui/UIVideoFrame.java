package heronarts.lx.app.ui;

import java.util.List;
import java.util.logging.Logger;

import flavius.ledportal.LPMeshable;
import heronarts.p4lx.ui.UI;
import heronarts.p4lx.ui.UI3dComponent;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

public class UIVideoFrame extends UI3dComponent {
  public List<float[]> vertexUVPairs;
  public PImage videoFrame;

  protected static final Logger logger = Logger
    .getLogger(UIVideoFrame.class.getName());

  public UIVideoFrame(List<float[]> vertexUVPairs, PImage videoFrame) {
    this.vertexUVPairs = vertexUVPairs;
    this.videoFrame = videoFrame;
  }

  /**
   * Last time a warning was emitted
   */
  long lastWarning = 0;

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    pg.pushStyle();
    pg.noStroke();
    pg.beginShape();
    pg.tint(255, 126);
    if(videoFrame == null) {
      long now = System.currentTimeMillis();
      if (lastWarning == 0 || now - lastWarning > 10000) {
        logger.warning("videoFrame is null...");
        lastWarning = now;
      }
      return;
    }
    pg.texture(videoFrame);
    if(vertexUVPairs == null) {
      long now = System.currentTimeMillis();
      if (lastWarning == 0 || now - lastWarning > 10000) {
        logger.warning("vertexUVPairs is null...");
        lastWarning = now;
      }
      return;
    }
    for (float[] vertexUVPair : vertexUVPairs) {
      PVector uiPosition = LPMeshable.worldUITransform(
        new PVector(vertexUVPair[0], vertexUVPair[1], vertexUVPair[2]));
      pg.vertex(uiPosition.x, uiPosition.y, uiPosition.z, vertexUVPair[3],
        vertexUVPair[4]);
    }
    pg.endShape();
    pg.popStyle();
  }
}

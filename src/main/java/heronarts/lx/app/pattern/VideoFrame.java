package heronarts.lx.app.pattern;

import java.util.logging.Logger;

import flavius.ledportal.LPMeshable;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

@LXCategory(LXCategory.TEXTURE)
public class VideoFrame extends LXPattern {
  private static final Logger logger = Logger
    .getLogger(VideoFrame.class.getName());

  public VideoFrame(LX lx) {
    super(lx);
  }

  public String getAuthor() {
    return "dev.laserphile.com";
  }

  float u;
  float v;
  int pixelValue;
  boolean firstRun = true;
  float[][] bounds;
  PImage frame;
  PMatrix3D flattener;

  public void run(double deltaMs) {
    if (LXStudioApp.videoFrame == null || LXStudioApp.videoFrame == null) {
      return;
    }

    flattener = LXStudioApp.flattener;
    frame = LXStudioApp.videoFrame;
    bounds = LXStudioApp.flatBounds;

    for (LXPoint point : model.points) {
      PVector uiPosition = new PVector(point.x, point.y, point.z);
      PVector worldPosition = LPMeshable.pixelWorldTransform(uiPosition);
      PVector flattenedPosition = LPMeshable.coordinateTransform(flattener, worldPosition);
      u = (flattenedPosition.x - bounds[0][0]) / (bounds[0][1] - bounds[0][0]);
      v = (flattenedPosition.y - bounds[1][0]) / (bounds[1][1] - bounds[1][0]);
      pixelValue = frame.get((int) (frame.width * u), (int) (frame.height * v));
      if (firstRun && point.index < 10) {
        logger.fine(String.format(
          "point[%d] at %s -> %s -> %s has u %7.3f , v %7.3f, %7x", point.index,
          LPMeshable.formatPVector(uiPosition),
          LPMeshable.formatPVector(worldPosition),
          LPMeshable.formatPVector(flattenedPosition), u, v, pixelValue));
      }
      setColor(point.index, pixelValue);
    }
    firstRun = false;
  }
}

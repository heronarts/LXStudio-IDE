package flavius.ledportal.pattern;

import java.io.IOException;
import java.util.logging.Logger;

import flavius.ledportal.LPPanelModel;
import flavius.ledportal.LPPanelModel.Point;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.p3lx.P3LX;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanel3DGraphicsPattern extends LPPanelStructurePattern {
  PImage frame;
  boolean frameReady = false;
  PFont font;
  double totalMs;
  PApplet applet;
  PGraphics pg;
  LXLoopTask renderTask;

  protected static final Logger logger = Logger
    .getLogger(LPPanel3DGraphicsPattern.class.getName());

  public void beforeDraw(PGraphics pg) {}

  public void onDraw(PGraphics pg) {}

  public void afterDraw(PGraphics pg) {}

  public LPPanel3DGraphicsPattern(LX lx) {
    super(lx);
    applet = LXStudioApp.instance;
    this.getModel();
    pg = applet.createGraphics(
        model.width + 1, model.height + 1, PGraphics.P3D);
    frame = new PImage(pg.width, pg.height);
    String fontPrefix = "Content/fonts/";
    try {
      fontPrefix = lx.getMediaFolder(LX.Media.CONTENT).getCanonicalPath() + "/fonts/";
    } catch (IOException e) {
      logger.severe(String.format("could not get fontPrefix: %s", e.toString()));
    }
    font = applet.createFont(fontPrefix + "uni0553-webfont.ttf", 8);

    renderTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        synchronized(LPPanel3DGraphicsPattern.class) {
          beforeDraw(pg);
          pg.beginDraw();
          onDraw(pg);
          pg.endDraw();
          afterDraw(pg);
          frame.set(0, 0, pg.get());
          frameReady = true;
        }
      }
    };

    synchronized(LPPanel3DGraphicsPattern.class) {
      ((P3LX)lx).ui.addLoopTask(renderTask);
    }
  }

  @Override
  public void dispose() {
    synchronized(LPPanel3DGraphicsPattern.class) {
      ((P3LX)this.lx).ui.removeLoopTask(this.renderTask);
    }
    super.dispose();
  }

  @Override
  public void run(double deltaMs) {
    totalMs += deltaMs;
    synchronized(LPPanel3DGraphicsPattern.class) {
      LPPanelModel.PanelMetrics metrics = ((LPPanelModel)(this.getModel())).metrics;
      if(frameReady == false) {
        return;
      }
      for(Point point : model.points) {
        int x = point.xi - metrics.xiMin;
        int y = point.yi - metrics.yiMin;
        setColor(point.index, frame.get(x, y));
      }
      frameReady = false;
    }
  }
}

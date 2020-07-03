package heronarts.lx.app.pattern;

import java.io.IOException;
import java.util.logging.Logger;

import flavius.ledportal.LPPanelModel;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.model.GridModel.Point;
import heronarts.lx.pattern.LXModelPattern;
import heronarts.p3lx.P3LX;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class Panel3DGraphicsPattern extends LXModelPattern<LPPanelModel> {
  PImage frame;
  boolean frameReady = false;
  PFont font;
  double totalMs;
  PApplet applet;
  PGraphics pg;
  LXLoopTask renderTask;

  protected static final Logger logger = Logger
    .getLogger(Panel3DGraphicsPattern.class.getName());

  public void beforeDraw(PGraphics pg) {}

  public void onDraw(PGraphics pg) {}

  public void afterDraw(PGraphics pg) {}

  public Panel3DGraphicsPattern(LX lx) {
    super(lx);
    applet = LXStudioApp.instance;
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
        synchronized(Panel3DGraphicsPattern.class) {
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

    synchronized(Panel3DGraphicsPattern.class) {
      ((P3LX)lx).ui.addLoopTask(renderTask);
    }
  }

  @Override
  public void dispose() {
    synchronized(Panel3DGraphicsPattern.class) {
      ((P3LX)this.lx).ui.removeLoopTask(this.renderTask);
    }
    super.dispose();
  }

  @Override
  public void run(double deltaMs) {
    totalMs += deltaMs;
    synchronized(Panel3DGraphicsPattern.class) {
      if(frameReady == false) {
        return;
      }
      for(Point point : model.points) {
        setColor(point.index, frame.get((model.width - point.xi), (model.height - point.yi)));
      }
      frameReady = false;
    }
  }
}

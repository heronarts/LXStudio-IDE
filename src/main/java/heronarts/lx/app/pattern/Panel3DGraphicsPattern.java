package heronarts.lx.app.pattern;

import flavius.ledportal.LPPanelModel;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.LX;
import heronarts.lx.model.GridModel.Point;
import heronarts.lx.pattern.LXModelPattern;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.UITimerTask;

import java.io.IOException;
import java.util.logging.Logger;

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
  PFont font;
  double totalMs;
  UIPanel3DGraphicsRenderer renderer;
  PApplet applet;

  private static final Logger logger = Logger
    .getLogger(Panel3DGraphicsPattern.class.getName());

  class UIPanel3DGraphicsRenderer extends UIObject {
    public PGraphics pg;

    @Override
    public float getWidth() {
      return -1;
    }

    @Override
    public float getHeight() {
      return -1;
    }

    public UIPanel3DGraphicsRenderer(Panel3DGraphicsPattern pattern) {
      P3LX lx = (P3LX)pattern.lx;
      this.pg = lx.ui.applet.createGraphics(
        pattern.model.width + 1, pattern.model.height + 1, PGraphics.P3D);
      lx.ui.addLoopTask(new UITimerTask(60, UITimerTask.Mode.FPS) {
        @Override
        protected void run() {
          pattern.beforeDraw(pg);
          pg.beginDraw();
          pattern.onDraw(pg);
          pg.endDraw();
          pattern.afterDraw(pg);
        }
      });
      logger.info(String.format("pg: %s", this.pg.toString()));
    }
  }

  public void beforeDraw(PGraphics pg) {
    pg.pushStyle();
  }

  public void onDraw(PGraphics pg) {}

  public void afterDraw(PGraphics pg) {
    pg.popStyle();
    this.frame.set(0, 0, pg.get());
  }

  public Panel3DGraphicsPattern(LX lx) {
    super(lx);
    this.applet = LXStudioApp.instance;
    this.renderer = new UIPanel3DGraphicsRenderer(this);
    this.frame = new PImage(model.width + 1, model.height + 1);
    String fontPrefix = "Content/fonts/";
    try {
      fontPrefix = lx.getMediaFolder(LX.Media.CONTENT).getCanonicalPath() + "/fonts/";
    } catch (IOException e) {
      logger.severe(String.format("could not get fontPrefix: %s", e.toString()));
    }
    this.font = this.applet.createFont(fontPrefix + "uni0553-webfont.ttf", 8);
  }

  @Override
  public void run(double deltaMs) {
    totalMs += deltaMs;
    if(frame == null) {
      logger.info("null frame");
      return;
    }
    for(Point point : model.points) {
      setColor(point.index, frame.get((model.width - point.xi), (model.height - point.yi)));
    }
  }
}

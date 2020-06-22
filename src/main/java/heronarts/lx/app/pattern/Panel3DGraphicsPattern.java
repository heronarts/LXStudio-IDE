package heronarts.lx.app.pattern;

import flavius.ledportal.LPPanelModel;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.LX;
import heronarts.lx.model.GridModel.Point;
import heronarts.lx.pattern.LXModelPattern;

import java.io.IOException;
import java.util.logging.Logger;

import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class Panel3DGraphicsPattern extends LXModelPattern<LPPanelModel> {
  PGraphics pg;
  PFont font;
  double totalMs;
  PImage frame;
  private static final Logger logger = Logger
    .getLogger(Panel3DGraphicsPattern.class.getName());

  public Panel3DGraphicsPattern(LX lx) {
    super(lx);
    setupGraphics(lx);
  }

  private void setupGraphics(LX lx) {
    pg = LXStudioApp.instance.createGraphics(model.width + 1, model.height + 1, PGraphics.P3D);
    logger.info(String.format("pg: %s", pg.toString()));
    String fontPrefix = "Content/fonts/";
    try {
      fontPrefix = lx.getMediaFolder(LX.Media.CONTENT).getCanonicalPath() + "/fonts/";
    } catch (IOException e) {
      logger.severe(String.format("could not get fontPrefix: %s", e.toString()));
    }
    font = LXStudioApp.instance.createFont(fontPrefix + "uni0553-webfont.ttf", 8);
  }

  public void beforeLoopGraphics() {
  }

  public void loopGraphics() {
  }

  @Override
  public void run(double deltaMs) {
    totalMs += deltaMs;
    beforeLoopGraphics();
    pg.beginDraw();
    pg.pushStyle();
    loopGraphics();
    pg.popStyle();
    for(Point point : model.points) {
      setColor(point.index, pg.get((model.width - point.xi), (model.height - point.yi)));
    }
    pg.endDraw();
  }
}

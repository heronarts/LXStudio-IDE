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

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class PanelGraphicsPattern extends LXModelPattern<LPPanelModel> {
  PGraphics pg;
  PFont font;
  private static final Logger logger = Logger
    .getLogger(PanelGraphicsPattern.class.getName());

  public PanelGraphicsPattern(LX lx) {
    super(lx);
    setupGraphics(lx);
  }

  private void setupGraphics(LX lx) {
    pg = LXStudioApp.instance.createGraphics(model.width + 1, model.height + 1);
    logger.info(String.format("pg: %s", pg.toString()));
    String fontPrefix = "Content/fonts/";
    try {
      fontPrefix = lx.getMediaFolder(LX.Media.CONTENT).getCanonicalPath() + "/fonts/";
    } catch (IOException e) {
      logger.severe(String.format("could not get fontPrefix: %s", e.toString()));
    }
    font = LXStudioApp.instance.createFont(fontPrefix + "uni0553-webfont.ttf", 8);
  }

  public void loopGraphics() {
  }

  @Override
  public void run(double deltaMs) {
    pg.beginDraw();
    pg.pushStyle();
    pg.pushMatrix();
    loopGraphics();
    pg.popMatrix();
    pg.popStyle();
    pg.endDraw();
    for(Point point : model.points) {
      setColor(point.index, pg.get((model.width - point.xi), (model.height - point.yi)));
    }
  }
}

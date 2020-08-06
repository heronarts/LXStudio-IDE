package flavius.ledportal.pattern;

import java.io.IOException;
import java.util.logging.Logger;

import flavius.ledportal.LPPanelModel;
import flavius.ledportal.LPPanelModel.Point;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
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
  int frameSize;
  String mediaPrefix;
  String fontPrefix;

  protected static final Logger logger = Logger
    .getLogger(LPPanel3DGraphicsPattern.class.getName());

  public void beforeDraw(PGraphics pg) {}

  public void onDraw(PGraphics pg) {}

  public void afterDraw(PGraphics pg) {}

  public final CompoundParameter xOffset = new CompoundParameter("X-Off", 0, -1,
    1).setDescription("Sets the placement in the X axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yOffset = new CompoundParameter("Y-Off", 0, -1,
    1).setDescription("Sets the placement in the Y axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zOffset = new CompoundParameter("Z-Off", 0, -1,
    1).setDescription("Sets the placement in the Z axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter xRotate = new CompoundParameter("X-Rot", 0, -1,
    1).setDescription("Sets the rotation about the X axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yRotate = new CompoundParameter("Y-Rot", 0, -1,
    1).setDescription("Sets the rotation about the Y axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zRotate = new CompoundParameter("Z-Rot", 0, -1,
    1).setDescription("Sets the rotation about the Z axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter scale = new CompoundParameter("Size", 1, 0, 20)
    .setDescription("Sets the size");

  public final DiscreteParameter beats = new DiscreteParameter("Beats", 4, 1, 16)
    .setDescription("The number of beats between a letter change");

  public final CompoundParameter xScanFuckery = new CompoundParameter("X-Scan", 0, 0,
  1).setDescription("Sets the amount of fuckery in the x axis when scanning")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yScanFuckery = new CompoundParameter("Y-Scan", 0, 0,
  1).setDescription("Sets the amount of fuckery in the y axis when scanning")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter pScanFuckery = new CompoundParameter("F-Scan", 0, 0,
  1).setDescription("Sets the period (inv of freq) of fuckery when scanning")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public LPPanel3DGraphicsPattern(LX lx) {
    super(lx);

    fontPrefix = "Content/fonts/";
    try {
      fontPrefix = lx.getMediaFolder(LX.Media.CONTENT).getCanonicalPath() + "/fonts/";
    } catch (IOException e) {
      logger.severe(String.format("could not get fontPrefix: %s", e.toString()));
    }
    mediaPrefix = "Content/media/";
    try {
      mediaPrefix = lx.getMediaFolder(LX.Media.CONTENT).getCanonicalPath() + "/media/";
    } catch (IOException e) {
      logger.severe(String.format("could not get mediaPrefix: %s", e.toString()));
    }

    refreshFont();

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

  public void refreshFont() {
    font = this.applet.createFont(fontPrefix + "uni0553-webfont.ttf", 8);
  }

  @Override
  public void beforeUpdateModel(LPPanelModel newModel) {
    if(this.applet == null)
      this.applet = LXStudioApp.instance;
    boolean createGraphics = false;
    boolean disposeGraphics = false;
    if(this.pg == null) {
      createGraphics = true;
    } else if(this.pg.width != newModel.width || this.pg.height != newModel.height) {
      createGraphics = true;
      disposeGraphics = true;
    }
    if(disposeGraphics) {
      this.pg.dispose();
    }
    if(createGraphics) {
      this.pg = this.applet.createGraphics(
          newModel.width + 1, newModel.height + 1, PGraphics.P3D);
      this.frame = new PImage(this.pg.width, this.pg.height);
      this.frameSize = Math.max(this.pg.width, this.pg.height);
    }
  }

  @Override
  public void dispose() {
    synchronized(LPPanel3DGraphicsPattern.class) {
      ((P3LX)this.lx).ui.removeLoopTask(this.renderTask);
    }
    super.dispose();
    // this.pg.dispose();
  }

  @Override
  public void run(double deltaMs) {
    totalMs += deltaMs;
    synchronized(LPPanel3DGraphicsPattern.class) {
      LPPanelModel.PanelMetrics metrics = ((LPPanelModel)(this.getModel())).metrics;
      if(frameReady == false) {
        return;
      }
      float period = this.pScanFuckery.getValuef() * Math.max(frame.width, frame.height);
      int xScanFuckery = (int) (this.xScanFuckery.getValuef() * frame.width / 2);
      int yScanFuckery = (int) (this.yScanFuckery.getValuef() * frame.height / 2);
      int beats = this.beats.getValuei();
      float phase = (float)(beats + this.lx.engine.tempo.basis()) / beats;
      for(Point point : model.points) {
        int x = (point.xi - metrics.xiMin + (int)(xScanFuckery * Math.sin(2 * Math.PI * ((point.yi / period) + phase)))) % frame.width;
        int y = (point.yi - metrics.yiMin + (int)(yScanFuckery * Math.sin(2 * Math.PI * ((point.xi / period) + phase)))) % frame.height;
        setColor(point.index, frame.get(x, y));
      }
      frameReady = false;
    }
  }
}

package flavius.ledportal.pattern;

import java.io.IOException;
import java.util.logging.Logger;

import flavius.ledportal.LPPanelModel;
import flavius.ledportal.LPPanelModel.Point;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanel3DGraphicsPattern extends LPPanelModelPattern {
  PImage frame;
  boolean frameReady = false;
  boolean pgNeedsDisposal = false;
  PFont font;
  double totalMs;
  PGraphics pg;
  LXLoopTask renderTask;
  int frameSize;
  String mediaPrefix;
  String fontPrefix;

  public final String renderer = PGraphics.P3D;

  protected static final Logger logger = Logger
    .getLogger(LPPanel3DGraphicsPattern.class.getName());

  public void beforeDraw(PGraphics pg) {
  }

  public void onDraw(PGraphics pg) {
  }

  public void afterDraw(PGraphics pg) {
  }

  public final CompoundParameter xOffset = new CompoundParameter("X-Off", 0, -2,
    2).setDescription("Sets the placement in the X axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yOffset = new CompoundParameter("Y-Off", 0, -2,
    2).setDescription("Sets the placement in the Y axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zOffset = new CompoundParameter("Z-Off", 0, -2,
    2).setDescription("Sets the placement in the Z axis")
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

  public final DiscreteParameter beats = new DiscreteParameter("Beats", 4, 1,
    16).setDescription("The number of beats between a letter change");

  public final CompoundParameter xScanFuckery = new CompoundParameter("X-Scan",
    0, 0, 1)
      .setDescription("Sets the amount of fuckery in the x axis when scanning")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yScanFuckery = new CompoundParameter("Y-Scan",
    0, 0, 1)
      .setDescription("Sets the amount of fuckery in the y axis when scanning")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter pScanFuckery = new CompoundParameter("F-Scan",
    0, 0, 1)
      .setDescription("Sets the period (inv of freq) of fuckery when scanning")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public LPPanel3DGraphicsPattern(LX lx) {
    super(lx);

    fontPrefix = "Content/fonts/";
    try {
      fontPrefix = lx.getMediaFolder(LX.Media.CONTENT).getCanonicalPath()
        + "/fonts/";
    } catch (IOException e) {
      logger
        .severe(String.format("could not get fontPrefix: %s", e.toString()));
    }
    mediaPrefix = "Content/media/";
    try {
      mediaPrefix = lx.getMediaFolder(LX.Media.CONTENT).getCanonicalPath()
        + "/media/";
    } catch (IOException e) {
      logger
        .severe(String.format("could not get mediaPrefix: %s", e.toString()));
    }

    refreshFont();
    scheduleRenderTask();
  }

  public void scheduleRenderTask() {
    renderTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        synchronized (LPPanel3DGraphicsPattern.class) {
          getModel();
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

    LXStudioApp.instance.scheduleDrawLoopTask(renderTask);
  }

  public void refreshFont() {
    font = LXStudioApp.instance.createFont(fontPrefix + "uni0553-webfont.ttf",
      8);
  }

  public void disposePG() {
    LXLoopTask disposalTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        if (pg != null) {
          pg.dispose();
          pg = null;
        }
      }
    };
    LXStudioApp.instance.scheduleDrawLoopTaskOnce(disposalTask);
  }

  public void resize(int width, int height) {
    LXLoopTask resizeTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        if (pg != null)
          pg.dispose();
        pg = LXStudioApp.instance.createGraphics(width, height, renderer);
        frame = new PImage(pg.width, pg.height);
        frameSize = Math.max(pg.width, pg.height);
      }
    };
    LXStudioApp.instance.scheduleDrawLoopTaskOnce(resizeTask);
  }

  @Override
  public void beforeUpdateModel(LPPanelModel newModel) {
    if (pg != null && pg.width == newModel.width + 1
      && pg.height == newModel.height + 1) {
      return;
    }
    resize(newModel.width + 1, newModel.height + 1);
  }

  @Override
  public void dispose() {
    synchronized (LPPanel3DGraphicsPattern.class) {
      LXStudioApp.instance.scheduleDrawLoopTaskRemoval(this.renderTask);
    }
    super.dispose();
    this.disposePG();
  }

  @Override
  public void run(double deltaMs) {
    totalMs += deltaMs;
    synchronized (LPPanel3DGraphicsPattern.class) {
      LPPanelModel.PanelMetrics metrics = ((LPPanelModel) (this
        .getModel())).metrics;
      if (frameReady == false) {
        return;
      }
      float period = this.pScanFuckery.getValuef()
        * Math.max(frame.width, frame.height);
      int xScanFuckery = (int) (this.xScanFuckery.getValuef() * frame.width
        / 2);
      int yScanFuckery = (int) (this.yScanFuckery.getValuef() * frame.height
        / 2);
      int beats = this.beats.getValuei();
      float phase = (float) (beats + this.lx.engine.tempo.basis()) / beats;
      for (Point point : model.points) {
        int x = (point.xi - metrics.xiMin
          + (int) (xScanFuckery
            * Math.sin(2 * Math.PI * ((point.yi / period) + phase))))
          % frame.width;
        int y = (point.yi - metrics.yiMin
          + (int) (yScanFuckery
            * Math.sin(2 * Math.PI * ((point.xi / period) + phase))))
          % frame.height;
        setColor(point.index, frame.get(x, y));
      }
      frameReady = false;
    }
  }
}

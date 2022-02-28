package flavius.ledportal.pattern;

import java.util.logging.Logger;

import flavius.ledportal.LPPanelModel;
import flavius.ledportal.LPPanelModel.Point;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PShader;

/**
 * Draw a PGraphics.P3D-rendered pattern directly to a panel where pixels are
 * arranged in a fixed grid pattern
 */
// TODO: rename LPPanelGraphicsPattern
public class LPPanelShaderDirect extends LPPanelModelPattern {
  /**
   * A graphics object perfectly aligned with the grid of an LPPanelModel to
   * which animations are written during a render which must occur in UI thread.
   */
  PGraphics pg;
  /**
   * The most recently rendered frame.
   */
  PImage frame;
  /**
   * Signals that a render task is complete, and a frame is ready to be written
   * to indexBuffers
   */
  boolean frameReady = false;
  /**
   * The maximum of the current frame's width and height
   */
  int frameSize;
  /**
   * Sum of all deltaMs seen by calls to run()
   */
  double totalMs;
  /**
   * loop task to render animation to pg in the UI thread.
   */
  LXLoopTask renderTask;

  /**
   * The colour shader object to be rendered.
   */
  protected PShader shader;

  public final String renderer = PGraphics.P3D;

  protected static final Logger logger = Logger
    .getLogger(LPPanel3DGraphicsPattern.class.getName());

  public final DiscreteParameter beats //
    = new DiscreteParameter("Beats", 4, 1, 16)
      .setDescription("The number of beats between a letter change");

  public final CompoundParameter xScanFuckery //
    = new CompoundParameter("X-Scan", 0, 0, 1)
      .setDescription("The amount of fuckery in the x axis when scanning")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yScanFuckery //
    = new CompoundParameter("Y-Scan", 0, 0, 1)
      .setDescription("The amount of fuckery in the y axis when scanning")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter pScanFuckery //
    = new CompoundParameter("F-Scan", 0, 0, 1)
      .setDescription("The period (inv of freq) of fuckery when scanning")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter xShear //
    = new CompoundParameter("X-Shear", 0.5, -2, 2)
      .setDescription("The shear of the foreground X axis with increasing Y")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public LPPanelShaderDirect(LX lx) {
    super(lx);
    scheduleRenderTask();
  }


  public void beforeDraw(PGraphics pg) {
    pg.camera();
    pg.perspective();
  }

  public void onDraw(PGraphics pg) {
    if (shader != null) {
      pg.shader(shader);
    }
    pg.rect(0, 0, pg.width, pg.height);
  }

  public void afterDraw(PGraphics pg) {
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
          refreshFrame();
        }
      }
    };

    LXStudioApp.instance.scheduleDrawLoopTask(renderTask);
  }

  /**
   * copy the graphics object to frame
   */
  protected void refreshFrame() {
    synchronized (LPPanel3DGraphicsPattern.class) {
      if (pg == null) {
        return;
      }
      frame.set(0, 0, pg.get());
      frameReady = true;
    }
  }

  protected void _disposePG() {
    if (pg != null) {
      pg.dispose();
      pg = null;
    }
  }

  public void scheduleDisposePG() {
    LXLoopTask disposalTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        _disposePG();
      }
    };
    LXStudioApp.instance.scheduleDrawLoopTaskOnce(disposalTask);
  }

  protected void _resize(int width, int height) {
    if (pg != null)
      pg.dispose();
    pg = LXStudioApp.instance.createGraphics(width, height, renderer);
    frame = new PImage(pg.width, pg.height);
    frameSize = Math.max(pg.width, pg.height);
  }

  public void scheduleResize(int width, int height) {
    LXLoopTask resizeTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        _resize(width, height);
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
    scheduleResize(newModel.width + 1, newModel.height + 1);
  }

  @Override
  public void dispose() {
    synchronized (LPPanel3DGraphicsPattern.class) {
      LXStudioApp.instance.scheduleDrawLoopTaskRemoval(this.renderTask);
    }
    super.dispose();
    this.scheduleDisposePG();
  }

  public void applyBackground(int colour) {
    pg.background(colour);
  }

  public void applyBackground() {
    applyBackground(LXColor.BLACK);
  }

  @Override
  public void run(double deltaMs) {
    totalMs += deltaMs;
    // TODO: is sync necessary?
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

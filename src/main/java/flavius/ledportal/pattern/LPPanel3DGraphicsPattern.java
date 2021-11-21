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
import processing.core.PMatrix2D;

/**
 * Draw a PGraphics.P3D-rendered pattern directly to a panel where pixels are
 * arranged in a fixed grid pattern
 */
// TODO: rename LPPanelGraphicsPattern
public class LPPanel3DGraphicsPattern extends LPPanelModelPattern {
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
   * Last time a warning was emitted
   */
  long lastWarning = 0;

  public final String renderer = PGraphics.P3D;

  protected static final Logger logger = Logger
    .getLogger(LPPanel3DGraphicsPattern.class.getName());

  public final CompoundParameter fov //
    = new CompoundParameter("fov", Math.PI/4, Math.PI/8, Math.PI)
      .setDescription("The camera field of view");

  public final CompoundParameter depth //
    = new CompoundParameter("depth", 3, 1, 10)
      .setDescription("The camera depth");

  public final CompoundParameter xOffset //
    = new CompoundParameter("X-Off", 0, -2, 2)
      .setDescription("The foreground placement in the X axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yOffset //
    = new CompoundParameter("Y-Off", 0, -2, 2)
      .setDescription("The foreground placement in the Y axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zOffset //
    = new CompoundParameter("Z-Off", 0, -2, 2)
      .setDescription("The foreground placement in the Z axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter xRotate //
    = new CompoundParameter("X-Rot", 0, -1, 1)
      .setDescription("The foreground rotation about the X axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yRotate //
    = new CompoundParameter("Y-Rot", 0, -1, 1)
      .setDescription("The foreground rotation about the Y axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zRotate //
    = new CompoundParameter("Z-Rot", 0, -1, 1)
      .setDescription("The foreground rotation about the Z axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter scale //
    = new CompoundParameter("Scale", 1, 0, 2).setDescription("The foreground Scale")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

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

  public LPPanel3DGraphicsPattern(LX lx) {
    super(lx);
    scheduleRenderTask();
  }


  public void beforeDraw(PGraphics pg) {
    if (pg == null) {
      return;
    }
    final float fov = this.fov.getValuef();
    final float depth = this.depth.getValuef();
    int pgSize = Math.max(pg.width, pg.height);
    float cameraZ = (float) (pgSize / Math.tan(fov / 2.0f));
    pg.camera( //
      0, 0, cameraZ * depth, //
      0, 0, 0, //
      0, 1, 0 //
    );
    pg.perspective(fov, (float) (pg.width / pg.height), cameraZ / (depth * depth),
      cameraZ * (depth * depth));
  }

  public void onDraw(PGraphics pg) {
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
   * copy the graphics object to fame
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

  /**
   * When the model containing all our fixtures has been updated, ensure that
   * our `pg` and `frame` are sized correctly.
   * 
   * making these square makes the camera math behave better.
   */
  @Override
  public void beforeUpdateModel(LPPanelModel newModel) {
    int modelSize = Math.max(newModel.width + 1, newModel.height + 1);
    if (pg != null && pg.width == modelSize && pg.height == modelSize) {
      return;
    }
    scheduleResize(modelSize, modelSize);
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

  public void applyShear() {
    final float xShear = this.xShear.getValuef();
    pg.applyMatrix(new PMatrix2D( //
      1.f, xShear, 0.f, //
      0.f, 1.f, 0.f //
    )); ///
  }

  public void applyRotation() {
    final float xRotate = this.xRotate.getValuef();
    final float yRotate = this.yRotate.getValuef();
    final float zRotate = this.zRotate.getValuef();
    pg.rotateX((float) Math.PI * xRotate);
    pg.rotateY((float) Math.PI * yRotate);
    pg.rotateZ((float) Math.PI * zRotate);
  }

  public void applyTranslation() {
    final float xOffset = this.xOffset.getValuef();
    final float yOffset = this.yOffset.getValuef();
    final float zOffset = this.zOffset.getValuef();
    pg.translate( //
      xOffset * model.width, //
      yOffset * model.height, //
      zOffset * Math.max(model.width, model.height));
  }

  public void applyScale() {
    final float scale = (float) Math.pow(this.scale.getValue(), 2.d);
    pg.scale(scale * frameSize);
  }

  public void applyForeground(PImage foreground) {
    if (foreground == null) {
      long now = System.currentTimeMillis();
      if (lastWarning == 0 || now - lastWarning > 10000) {
        logger.warning("foreground is null...");
        lastWarning = now;
      }
      return;
    }
    if(foreground.width == 0 || foreground.height == 0) {      
      long now = System.currentTimeMillis();
      if (lastWarning == 0 || now - lastWarning > 10000) {
        logger.warning("foreground is zero-sized...");
        lastWarning = now;
      }
      return;
    }
    int foreSize = Math.max(foreground.width, foreground.height);
    int foreHeight = foreground.height;
    int foreWidth = foreground.width;
    pg.noStroke();
    pg.beginShape();
    pg.texture(foreground);
    float foreX = frameSize * foreWidth / foreSize / 2.0f;
    float foreY = frameSize * foreHeight / foreSize / 2.0f;
    // pg.image(foreground, -foreX, -foreY, foreX * 2.0f, foreY * 2.0f );
    pg.vertex(-foreX, -foreY, 0, 0, 0);
    pg.vertex(foreX, -foreY, 0, foreWidth, 0);
    pg.vertex(foreX, foreY, 0, foreWidth, foreHeight);
    pg.vertex(-foreX, foreY, 0, 0, foreHeight);

    pg.endShape();
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

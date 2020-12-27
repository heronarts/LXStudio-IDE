package flavius.ledportal.pattern;

import java.util.HashSet;
import java.util.Set;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanelCapture extends LPPanel3DGraphicsPattern {

  PImage foreground;
  public float[] screenCapBounds;
  PImage screenBuffer;
  private Robot robot;
  private GraphicsDevice activeScreen;
  private Rectangle screenCapRectangle;

  public final CompoundParameter boundsXL //
    = new CompoundParameter("bounds-x-l", 0, 0, 1)
      .setDescription("The lower x bound");

  public final CompoundParameter boundsYL //
    = new CompoundParameter("bounds-y-l", 0, 0, 1)
      .setDescription("The lower y bound");

  public final CompoundParameter boundsXU //
    = new CompoundParameter("bounds-x-u", 1, 0, 1)
      .setDescription("The upper x bound");

  public final CompoundParameter boundsYU //
    = new CompoundParameter("bounds-y-u", 1, 0, 1)
      .setDescription("The upper y bound");

  private final Set<LXParameter> boundsParameters = new HashSet<LXParameter>();

  public LPPanelCapture(LX lx) {
    super(lx);

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    // addParameter("xShear", this.xShear);
    addParameter("size", this.scale);
    // addParameter("fov", this.fov);
    // addParameter("depth", this.depth);
    addBoundsParameter("bounds-x-l", this.boundsXL);
    addBoundsParameter("bounds-y-l", this.boundsYL);
    addBoundsParameter("bounds-x-u", this.boundsXU);
    addBoundsParameter("bounds-y-u", this.boundsYU);

    scheduleRefreshScreenBufferOnce();
    scheduleRefreshForeground();
  }

  /**
   * Adds a parameter which impacts the screencap bounds.
   *
   * @param path      Path to parameter
   * @param parameter Parameter
   */
  protected void addBoundsParameter(String path, LXParameter parameter) {
    addParameter(path, parameter);
    this.boundsParameters.add(parameter);
  }

  public void scheduleRefreshForeground() {
    LXLoopTask videoFrameTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        _refreshForeground();
      }
    };
    LXStudioApp.instance.scheduleDrawLoopTask(videoFrameTask);
  }

  public void onParameterChanged(LXParameter p) {
    super.onParameterChanged(p);
    if (this.boundsParameters.contains(p)) {
      // Note: this will rebuild this fixture and trigger the structure
      // to rebuild as well
      regenerateBounds();
    }
  }

  public void regenerateBounds() {
    float[] newScreenCapBounds = new float[] { this.boundsXL.getValuef(),
      this.boundsYL.getValuef(), this.boundsXU.getValuef(),
      this.boundsYU.getValuef() };
    // if (newScreenCapBounds[2] <= newScreenCapBounds[0] ) {
    // throw new IllegalArgumentException(
    // String.format("screencap bounds must have maintain x boundary conditions,
    // instead x-upper (%s) <= x-lower", newScreenCapBounds[2],
    // newScreenCapBounds[0]));
    // } else if (newScreenCapBounds[3] <= newScreenCapBounds[1] ) {

    // }
    screenCapBounds = newScreenCapBounds;
  }

  protected void _refreshScreenBuffer() {
    activeScreen = GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getDefaultScreenDevice();
    int activeScreenWidth = activeScreen.getDisplayMode().getWidth();
    int activeScreenHeight = activeScreen.getDisplayMode().getHeight();
    logger.info(String.format("active screen dimensions: [%d, %d]",
      activeScreenWidth, activeScreenHeight));
    regenerateBounds();
    screenCapRectangle = new Rectangle(
      (int) (screenCapBounds[0] * activeScreenWidth),
      (int) (screenCapBounds[1] * activeScreenHeight),
      (int) (screenCapBounds[2] * activeScreenWidth),
      (int) (screenCapBounds[3] * activeScreenHeight));
    logger.info(String.format("screenCap rectangle: %s", screenCapRectangle));
    try {
      robot = new Robot(activeScreen);
    } catch (Exception e) {
      logger.warning(e.getMessage());
    }
    screenBuffer = new PImage(robot.createScreenCapture(screenCapRectangle));
  }

  protected void _refreshForeground() {
    screenBuffer = new PImage(robot.createScreenCapture(screenCapRectangle));
    if (screenBuffer == null) {
      logger.warning("screenBuffer is null");
      return;
    }
    int newWidth = screenBuffer.width;
    int newHeight = screenBuffer.height;
    if (foreground == null
      || (foreground.width != newWidth || foreground.height != newHeight)) {
      foreground = LXStudioApp.instance.createImage(newWidth, newHeight,
        PConstants.RGB);
    }
    foreground.copy(screenBuffer, 0, 0, screenBuffer.width, screenBuffer.height, 0, 0,
      foreground.width, foreground.height);
  }

  public void scheduleRefreshScreenBufferOnce() {
    LXLoopTask refreshScreenBufferTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        _refreshScreenBuffer();
      }
    };
    LXStudioApp.instance.scheduleDrawLoopTaskOnce(refreshScreenBufferTask);
  }

  @Override
  public void beforeDraw(final PGraphics pg) {
    super.beforeDraw(pg);
    pg.smooth(8);
  }

  @Override
  public void onDraw(final PGraphics pg) {
    pg.pushMatrix();
    applyBackground();
    applyTranslation();
    applyShear();
    applyRotation();
    applyScale();
    applyForeground(foreground);
    pg.popMatrix();
  }
}

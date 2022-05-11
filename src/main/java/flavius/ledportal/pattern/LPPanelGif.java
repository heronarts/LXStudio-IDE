package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.ObjectParameter;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import gifAnimation.Gif;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanelGif extends LPPanel3DGraphicsPattern {

  PImage foreground;
  Gif gif;

  public final ObjectParameter<String> gifName;

  /**
   * Last time a warning was emitted
   */
  long lastWarning = 0;

  int lastFrame = -1;

  public LPPanelGif(LX lx) {
    super(lx);

    String[] gifNames = LXStudioApp.instance.gifLibrary.getNames();
    if (gifNames.length == 0) {
      logger.warning("no videos available");
      gifName = new ObjectParameter<String>("gif", new String[] { "" });
    } else {
      gifName = new ObjectParameter<String>("gif", gifNames);
    }

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    // addParameter("zOffset", this.zOffset);
    // addParameter("xRotate", this.xRotate);
    // addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    // addParameter("xShear", this.xShear);
    addParameter("size", this.scale);
    addParameter("gif", this.gifName);
    // addParameter("fov", this.fov);
    // addParameter("depth", this.depth);
    addParameter("alphaCurve", this.alphaCurve);

    scheduleRefreshGifOnce();
    scheduleRefreshForeground();
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
    if (p == this.gifName) {
      scheduleRefreshGifOnce();
    }
  }

  protected void _refreshGif() {
    String name = (String) this.gifName.getObject();
    if (name == "") {
      return;
    }
    Gif newGif = LXStudioApp.instance.gifLibrary.prepareMedia(name);

    if (newGif == null) {
      long now = System.currentTimeMillis();
      if (lastWarning == 0 || now - lastWarning > 10000) {
        logger.warning(String.format("newGif %s is null", name));
        lastWarning = now;
      }
      return;
    } else {
      if (gif != newGif) {
        logger.info(String.format("newGif: %s", newGif.toString()));
      }
    }
    lastFrame = -1;
    gif = newGif;
  }

  protected void _refreshForeground() {
    if (gif == null) {
      long now = System.currentTimeMillis();
      if (lastWarning == 0 || now - lastWarning > 10000) {
        logger.warning("gif is null");
        lastWarning = now;
      }
      return;
    }
    int newWidth = gif.width;
    int newHeight = gif.height;
    if (foreground == null
      || (foreground.width != newWidth || foreground.height != newHeight)) {
      foreground = LXStudioApp.instance.createImage(newWidth, newHeight,
        PConstants.RGB);
    }
    int thisFrame = gif.currentFrame();
    if (lastFrame != thisFrame) {
      LXStudioApp.instance.setPixelsFrom(foreground, gif.getPImages()[gif.currentFrame()]);
      foreground.updatePixels();
      lastFrame = thisFrame;
    }
  }

  public void scheduleRefreshGifOnce() {
    LXLoopTask refreshGifTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        _refreshGif();
      }
    };
    LXStudioApp.instance.scheduleDrawLoopTaskOnce(refreshGifTask);
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

package flavius.ledportal.pattern;

import org.apache.commons.lang3.reflect.FieldUtils;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.ObjectParameter;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Capture;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanelCapture extends LPPanel3DGraphicsPattern {

  PImage foreground;
  Capture capture;

  public final ObjectParameter<String> captureName;

  public LPPanelCapture(LX lx) {
    super(lx);

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("xShear", this.xShear);
    addParameter("size", this.scale);
    addParameter("fov", this.fov);
    addParameter("depth", this.depth);

    String[] deviceNames = Capture.list();
    if (deviceNames.length == 0) {
      logger.warning("no capture devices available");
      captureName = new ObjectParameter<String>("capture", new String[] { "" });
    } else {
      logger.info(String.format("capture devices available: %s",
        String.join(", ", deviceNames)));
      captureName = new ObjectParameter<String>("capture", deviceNames);
    }

    addParameter("capture", captureName);

    scheduleRefreshCaptureOnce();
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
    if (p == this.captureName) {
      // Note: this will rebuild this fixture and trigger the structure
      // to rebuild as well
      _refreshCapture();
    }
  }

  protected void _refreshCapture() {
    String name = (String) this.captureName.getObject();
    if (name == "") {
      return;
    }
    String captureDevice = "";
    // TODO: this doesn't work on Windows?
    try {
      if (capture != null) {
        captureDevice = (String) FieldUtils.readField(capture, "device", true);
      }
    } catch (Exception e) {
      logger.warning(e.toString());
    }
    if (capture == null || name != captureDevice) {
      if (capture != null) {
        capture.stop();
      }
      Capture newCapture = new Capture(LXStudioApp.instance, name);
      newCapture.start();
      logger.info(
        String.format("newCapture (%s) : %s", name, newCapture.toString()));
      capture = newCapture;
    }
  }

  protected void _refreshForeground() {
    if (capture == null) {
      logger.warning("capture is null");
      return;
    }
    if (capture.available()) {
      capture.read();
    }
    int newWidth = capture.width;
    int newHeight = capture.height;
    if (newWidth > 0 && newHeight > 0 && (foreground == null
      || foreground.width != newWidth || foreground.height != newHeight)) {
      foreground = LXStudioApp.instance.createImage(newWidth, newHeight,
        PConstants.RGB);
    }

    LXStudioApp.instance.setPixelsFrom(foreground, capture);
  }

  public void scheduleRefreshCaptureOnce() {
    LXLoopTask refreshCaptureTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        _refreshCapture();
      }
    };
    LXStudioApp.instance.scheduleDrawLoopTaskOnce(refreshCaptureTask);
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

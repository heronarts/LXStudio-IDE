package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.app.media.GifLibrary;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.parameter.StringParameter;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

import gifAnimation.Gif;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIDropMenu;
import heronarts.p4lx.ui.component.UIKnob;
import heronarts.p4lx.ui.component.UITextBox;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanelGif extends LPPanel3DGraphicsPattern
  implements UIDeviceControls<LPPanelGif> {

  PImage foreground;
  Gif gif;
  protected GifLibrary library;

  protected final ObjectParameter<String> gifName;
  protected final StringParameter query = new StringParameter("query");
  protected final UIDropMenu uiDrop;

  /**
   * Last time a warning was emitted
   */
  long lastWarning = 0;

  int lastFrame = -1;

  public LPPanelGif(LX lx) {
    super(lx);

    library = LXStudioApp.instance.gifLibrary;

    String[] gifNames = library.getNames();
    if (gifNames.length == 0) {
      logger.warning("no gifs available");
      gifName = new ObjectParameter<String>("gif", new String[] { "" });
    } else {
      gifName = new ObjectParameter<String>("gif", gifNames);
    }

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zRotate", this.zRotate);
    addParameter("size", this.scale);
    addParameter("gif", this.gifName);
    addParameter("alphaCurve", this.alphaCurve);
    addParameter("query", this.query);

    uiDrop = new UIDropMenu(COL_WIDTH * 3, gifName);
    uiDrop.setDirection(UIDropMenu.Direction.UP);

    scheduleRefreshGifNamesOnce();
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
    logger.info(String.format("on param changed %s", p.toString()));
    super.onParameterChanged(p);
    if (p == this.query) {
      scheduleRefreshGifNamesOnce();
    }
    if (p == this.gifName) {
      scheduleRefreshGifOnce();
    }
  }

  protected void _refreshGif() {
    String name = (String) this.gifName.getObject();
    if (name == "") {
      return;
    }
    Gif newGif = library.prepareMedia(name);

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

  protected void _refreshGifNames() {
    String queryString = query.getString();
    String[] gifNames = null;
    if (queryString.length() > 0) {
      logger
        .warning(String.format("refreshing gif names. Query: %s", queryString));
      gifNames = library.getNames((String name) -> {
        return name.contains(queryString);
      });
    } else {
      gifNames = library.getNames();
    }

    if (gifNames.length == 0) {
      logger.warning("no gifs available");
    } else {
      gifName.setObjects(gifNames);
      uiDrop.setParameter(gifName);
      uiDrop.setOptions(gifNames);
    }
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
      LXStudioApp.instance.setPixelsFrom(foreground,
        gif.getPImages()[gif.currentFrame()]);
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

  public void scheduleRefreshGifNamesOnce() {
    LXLoopTask refreshGifTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        _refreshGifNames();
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

  @Override
  public void buildDeviceControls(UI ui, UIDevice uiDevice,
    LPPanelGif pattern) {
    uiDrop.setParameter(pattern.gifName);
    float height = UIDropMenu.DEFAULT_HEIGHT;
    uiDevice.setContentWidth(COL_WIDTH * 3);
    UITextBox searchUI = new UITextBox(0, 0, COL_WIDTH * 2, height);
    searchUI.setParameter(pattern.query);
    addColumn(uiDevice, COL_WIDTH * 3, //
      searchUI, //
      uiDrop, //
      UI2dContainer.newHorizontalContainer(UIKnob.HEIGHT, 0, //
        new UIKnob(pattern.xOffset), //
        new UIKnob(pattern.yOffset), //
        new UIKnob(pattern.zRotate), //
        new UIKnob(pattern.scale) //
      ), //
      UI2dContainer.newHorizontalContainer(UIKnob.HEIGHT, 0, //
        new UIKnob(pattern.gifName), //
        new UIKnob(pattern.alphaCurve) //
      ) //
    );
  }
}

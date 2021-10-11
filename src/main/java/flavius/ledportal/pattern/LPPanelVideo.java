package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.ObjectParameter;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Movie;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanelVideo extends LPPanel3DGraphicsPattern {

  PImage foreground;
  Movie movie;

  public final ObjectParameter<String> video;

  public final CompoundParameter volume //
    = new CompoundParameter("Vol", 0, 0, 1)
      .setDescription("The volume of the video");

  /**
   * Last time a warning was emitted
   */
  long lastWarning = 0;

  public LPPanelVideo(LX lx) {
    super(lx);

    String[] videoNames = LXStudioApp.instance.videoLibrary.getNames();
    // String[] videoNames = LXStudioApp.instance.videoLibrary.getNames((String
    // name) -> name.contains("Steamed"));
    if (videoNames.length == 0) {
      logger.warning("no videos available");
      video = new ObjectParameter<String>("video", new String[] { "" });
    } else {
      video = new ObjectParameter<String>("video", videoNames);
    }

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    // addParameter("xShear", this.xShear);
    addParameter("size", this.scale);
    addParameter("video", this.video);
    addParameter("volume", this.volume);
    // addParameter("fov", this.fov);
    // addParameter("depth", this.depth);

    scheduleRefreshVideoOnce();
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
    if (p == this.video || p == this.volume) {
      scheduleRefreshVideoOnce();
    }
  }

  protected void _refreshVideo() {
    String name = (String) this.video.getObject();
    if (name == "") {
      return;
    }
    Movie newMovie = LXStudioApp.instance.videoLibrary.prepareMedia(name);
    if (movie != null && movie != newMovie) {
      logger.info(String.format("newMovie: %s", newMovie.toString()));
    }
    if (newMovie == null) {
      long now = System.currentTimeMillis();
      if (lastWarning == 0 || now - lastWarning > 10000) {
        logger.warning(String.format("newMovie %s is null", name));
        lastWarning = now;
      }
      return;
    }
    movie = newMovie;
    float volume = this.volume.getValuef();
    movie.volume(volume);
  }

  protected void _refreshForeground() {
    if (movie == null) {
      long now = System.currentTimeMillis();
      if (lastWarning == 0 || now - lastWarning > 10000) {
        logger.warning("movie is null");
        lastWarning = now;
      }
      return;
    }
    if (movie.available()) 
      movie.read();
    int newWidth = movie.width;
    int newHeight = movie.height;
    if (foreground == null
      || (foreground.width != newWidth || foreground.height != newHeight)) {
      foreground = LXStudioApp.instance.createImage(newWidth, newHeight,
        PConstants.RGB);
    }
    LXStudioApp.instance.setPixelsFrom(foreground, movie);
  }

  public void scheduleRefreshVideoOnce() {
    LXLoopTask refreshVideoTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        _refreshVideo();
      }
    };
    LXStudioApp.instance.scheduleDrawLoopTaskOnce(refreshVideoTask);
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

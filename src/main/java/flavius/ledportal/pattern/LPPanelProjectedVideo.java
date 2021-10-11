package flavius.ledportal.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import flavius.ledportal.LPMeshable;
import flavius.ledportal.LPPanelModel;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.ObjectParameter;
import processing.core.PConstants;
import processing.core.PImage;
import processing.video.Movie;
import processing.core.PVector;

/**
 * Project a video to all panels
 */
public class LPPanelProjectedVideo extends LPPanelModelPattern {

  PImage foreground;
  Movie movie;

  public final ObjectParameter<String> video;

  public final CompoundParameter volume //
    = new CompoundParameter("Vol", 0, 0, 1)
      .setDescription("The volume of the video");

  public LPPanelProjectedVideo(LX lx) {
    super(lx);

    String[] videoNames = LXStudioApp.instance.videoLibrary.getNames();
    if (videoNames.length == 0) {
      logger.warning("no videos available");
      video = new ObjectParameter<String>("video", new String[] { "" });
    } else {
      video = new ObjectParameter<String>("video", videoNames);
    }

    // addParameter("xOffset", this.xOffset);
    // addParameter("yOffset", this.yOffset);
    // addParameter("zOffset", this.zOffset);
    // addParameter("xRotate", this.xRotate);
    // addParameter("yRotate", this.yRotate);
    // addParameter("zRotate", this.zRotate);
    // addParameter("xShear", this.xShear);
    // addParameter("size", this.scale);
    addParameter("video", this.video);
    addParameter("volume", this.volume);
    // addParameter("fov", this.fov);
    // addParameter("depth", this.depth);

    scheduleRefreshVideoOnce();
    scheduleRefreshForeground();
  }

  public void updateGeometry(LPPanelModel newModel) {
    LXStudioApp.flattener = LPMeshable.getFlattener(newModel.centroid,
      newModel.normal);
    LXStudioApp.unflattener = LPMeshable.getFlattener(newModel.centroid,
      newModel.normal);
    List<PVector> modelPoints = Arrays.stream(newModel.points)
      .map(point -> new PVector(point.x, point.y, point.z))
      .collect(Collectors.toList());

    LXStudioApp.modelBounds = LPMeshable.getAxisBounds(modelPoints);
    List<PVector> modelFlattenedPoints = new ArrayList<PVector>();
    for (final PVector point : modelPoints) {
      modelFlattenedPoints
        .add(LPMeshable.coordinateTransform(LXStudioApp.flattener, point));
    }

    LXStudioApp.modelBounds = new float[][] {
      new float[] { newModel.xMax, newModel.xMin },
      new float[] { newModel.yMax, newModel.yMin },
      new float[] { newModel.zMax, newModel.zMin } };
    LXStudioApp.flatBounds = LPMeshable.getAxisBounds(modelFlattenedPoints);
  }

  @Override
  public void beforeUpdateModel(LPPanelModel newModel) {
    updateGeometry(newModel);
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
      logger.warning(String.format("newMovie %s is null", name));
      return;
    }
    movie = newMovie;
    float volume = this.volume.getValuef();
    movie.volume(volume);
  }

  protected void _refreshForeground() {
    if (movie == null) {
      logger.warning("movie is null");
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
    foreground.copy(movie, 0, 0, movie.width, movie.height, 0, 0,
      foreground.width, foreground.height);
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

  // @Override
  // public void beforeDraw(final PGraphics pg) {
  // super.beforeDraw(pg);
  // pg.smooth(8);
  // }

  // @Override
  // public void onDraw(final PGraphics pg) {
  // pg.pushMatrix();
  // applyBackground();
  // applyTranslation();
  // applyShear();
  // applyRotation();
  // applyScale();
  // applyForeground(foreground);
  // pg.popMatrix();
  // }

  @Override
  public void run(double deltaMs) {

    // for (LXPoint point : model.points) {
    //   PVector uiPosition = new PVector(point.x, point.y, point.z);
    //   PVector worldPosition = LPMeshable.pixelWorldTransform(uiPosition);
    //   PVector flattenedPosition = LPMeshable.coordinateTransform(LXStudioApp.flattener, worldPosition);
    //   u = (flattenedPosition.x - LXStudioApp.flatBounds[0][0]) / (LXStudioApp.flatBounds[0][1] - LXStudioApp.flatBounds[0][0]);
    //   v = (flattenedPosition.y - LXStudioApp.flatBounds[1][0]) / (LXStudioApp.flatBounds[1][1] - LXStudioApp.flatBounds[1][0]);
    //   pixelValue = LXStudioApp.videoFrame.get((int) (LXStudioApp.videoFrame.width * u), (int) (LXStudioApp.videoFrame.height * v));
    //   if (firstRun && point.index < 10) {
    //     // logger.fine(String.format(
    //     //   "point[%d] at %s -> %s -> %s has u %7.3f , v %7.3f, %7x", point.index,
    //     //   LPMeshable.formatPVector(uiPosition),
    //     //   LPMeshable.formatPVector(worldPosition),
    //     //   LPMeshable.formatPVector(flattenedPosition), u, v, pixelValue));
    //   }
    //   setColor(point.index, pixelValue);
    // }
    // firstRun = false;
  }
}

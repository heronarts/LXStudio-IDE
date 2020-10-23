/**
 * Copyright 2020- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.app;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.lang.IllegalArgumentException;

import flavius.ledportal.LPDecoration;
import flavius.ledportal.LPMeshable;
import flavius.ledportal.LPSimConfig;
import flavius.ledportal.pattern.LPPanelHexLife;
import flavius.ledportal.pattern.LPPanelSolidState;
import flavius.ledportal.pattern.LPPanelTexture;
import flavius.ledportal.pattern.LPPanel3DRotatingCube;
import flavius.ledportal.pattern.LPPanelBLM;
import flavius.ledportal.structure.LPPanelFixture;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.LX.Media;
import heronarts.lx.LXPlugin;
import heronarts.lx.app.pattern.VideoFrame;
import heronarts.lx.app.ui.UIAxes;
import heronarts.lx.app.ui.UIPanelFixture;
import heronarts.lx.app.ui.UIVideoFrame;
import heronarts.lx.app.ui.UIWireFrame;
import heronarts.lx.pattern.GraphicEqualizerPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI.CoordinateSystem;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.video.Movie;

/**
 * This is an example top-level class to build and run an LX Studio application
 * via an IDE. The main() method of this class can be invoked with arguments to
 * either run with a full Processing 3 UI or as a headless command-line only
 * engine.
 */
public class LXStudioApp extends PApplet implements LXPlugin {

  // Window Settings
  private static final String WINDOW_TITLE = "LEDPortal";
  private static int WIDTH = 1280;
  private static int HEIGHT = 800;
  private static boolean FULLSCREEN = false;

  private static final Logger logger = Logger
    .getLogger(LXStudioApp.class.getName());

  // TODO: make this false, and have patterns schedule prepareFOO()
  private static boolean PREPARE_MEDIA = true;
  // TODO: move the following fields to contentLibrary class, make hashmaps
  // private
  /**
   * A list of image filenames to paths available to load from Content/images
   */
  public static HashMap<String, String> imageListing = new HashMap<String, String>();
  /**
   * A cache of PImage objects previously loaded from imageListing
   */
  public static HashMap<String, PImage> images = new HashMap<String, PImage>();
  /**
   * A map of video filenames to paths available to load from Content/videos
   */
  public static HashMap<String, String> videoListing = new HashMap<String, String>();
  /**
   * A hashmap of Movie objects previously loaded from Content/video
   */
  public static HashMap<String, Movie> videos = new HashMap<String, Movie>();
  /**
   * A list of font filenames to paths available to load from Content/fonts
   */
  public static HashMap<String, String> fontListing = new HashMap<String, String>();
  /**
   * A hashmap of PFont objects previously loaded from Content/fonts
   */
  public static HashMap<String, PFont> fonts = new HashMap<String, PFont>();

  public static final int DEFAULT_FONT_SIZE = 96;

  // TODO: Move these to individual video patterns
  private Movie movie;
  // TODO: Move these to individual screencap patterns
  private Robot robot;
  private GraphicsDevice activeScreen;
  private Rectangle screenCapRectangle;
  // TODO: deprecate LPSimConfig
  public static LPSimConfig config;
  // TODO: deprecate videoFrame, use videos instead
  public static PImage videoFrame;
  // TODO: move these metrics out of LXStudioApp
  public static PMatrix3D flattener;
  public static PMatrix3D unflattener;
  public static float[][] flatBounds;
  public static float[][] modelBounds;
  // TODO: move these metrics out of LXStudioApp
  public static LXStudio studio;
  public static LXStudioApp instance;

  @Override
  public void settings() {
    if (FULLSCREEN) {
      fullScreen(PApplet.P3D);
    } else {
      size(WIDTH, HEIGHT, PApplet.P3D);
    }
  }

  @Override
  public void setup() {
    instance = this;
    config = new LPSimConfig();
    for (String activeDecoration : config.activeDecorations) {
      config.updateFromJSONObject(loadJSONObject(activeDecoration));
    }

    flattener = config.getWorldFlattener();
    unflattener = config.getWorldUnflattener();
    modelBounds = config.getModelBounds();
    flatBounds = config.getModelFlatBounds();

    LXStudio.Flags flags = new LXStudio.Flags(this);
    flags.resizable = true;
    flags.useGLPointCloud = false;
    flags.startMultiThreaded = true;
    flags.mediaPath = System.getProperty("user.dir");
    studio = new LXStudio(this, flags);
    surface.setTitle(WINDOW_TITLE);
    addVideoFrameDrawLoopTask();
  }

  public void addVideoFrameDrawLoopTask() {
    LXLoopTask videoFrameTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        if (movie != null && movie.available()) {
          movie.read();
          videoFrame.copy(movie, 0, 0, movie.width, movie.height, 0, 0,
            movie.width, movie.height);
        } else if (screenCapRectangle != null) {
          PImage screenBuffer = new PImage(
            robot.createScreenCapture(screenCapRectangle));
          videoFrame.copy(screenBuffer, 0, 0, screenBuffer.width,
            screenBuffer.height, 0, 0, screenBuffer.width, screenBuffer.height);
        }
      }
    };
    this.addDrawLoopTask(videoFrameTask);
  }

  @Override
  public void initialize(LX lx) {
    // Here is where you should register any custom components or make
    // modifications to the LX engine or hierarchy. This is also used in
    // headless mode, so note that you cannot assume you are working with an
    // LXStudio class or that any UI will be available.

    // Register custom pattern and effect types
    // lx.registry.addPattern(heronarts.lx.app.pattern.AppPattern.class);
    lx.registry.addPattern(VideoFrame.class);
    lx.registry.addPattern(LPPanelBLM.class);
    lx.registry.addPattern(LPPanel3DRotatingCube.class);
    lx.registry.addPattern(LPPanelTexture.class);
    lx.registry.addPattern(LPPanelSolidState.class);
    lx.registry.addPattern(LPPanelHexLife.class);
    lx.registry.addPattern(GraphicEqualizerPattern.class);
    // lx.registry.addEffect(heronarts.lx.app.effect.AppEffect.class);

    lx.registry.addFixture(LPPanelFixture.class);

    initializeImages(lx);
    initializeVideos(lx);
    initializeFonts(lx);

    if (videoFrame == null)
      initializeVideoFrame(lx);
  }

  // TODO: move the following functions to media.ContentLibrary class +
  // subclasses
  String getCanonicalContentPath(LX lx, String contentSubdirectory) {
    String contentPath = String.format("Content/%s/", contentSubdirectory);
    try {
      contentPath = lx.getMediaFolder(Media.CONTENT).getCanonicalPath()
        + String.format("/%s/", contentSubdirectory);
    } catch (IOException e) {
      logger
        .severe(String.format("failed to get media folder: %s", e.toString()));
    }
    return contentPath;
  }

  String[] splitExt(String fileName) {
    String[] result = fileName.split("\\.(?=[^\\.]+$)");
    if(result.length > 0){
      return result;
    }
    result = new String[] {fileName, ""};
    // String[] result = new String[2];
    // result[0] = fileName;
    // result[1] = "";
    // String[] name_tokens = fileName.split("\\.(?=[^\\.]+$)");
    // if(name_tokens.length > 0) {
    //   result[1] = name_tokens[name_tokens.length - 1];
    //   result[0] = fileName.substring(0, fileName.length() - result[1].length() - 1);
    // }
    return result;
  }

  FileFilter getFileFilterForExtensions(List<String> extensions) {
    return new FileFilter() {
      @Override
      public boolean accept(File file) {
        return extensions.contains(splitExt(file.getName())[1]);
      }
    };
  }

  public void initializeImages(LX lx) {
    String contentPath = getCanonicalContentPath(lx, "images");

    File dir = new File(contentPath);
    FileFilter filter = getFileFilterForExtensions(
      Arrays.asList("gif", "jpg", "tga", "png"));
    File[] directoryListing = dir.listFiles(filter);

    if (directoryListing != null) {
      for (File child : directoryListing) {
        imageListing.put(child.getName(), child.getAbsolutePath());
        if (PREPARE_MEDIA) {
          prepareImage(child.getName());
        }
      }
    }

    logger.info(String.format("imageListing: %s", imageListing.keySet().toString()));
  }

  public PImage prepareImage(String name) {
    PImage result = images.get(name);
    if (result != null) {
      return result;
    }
    String fullPath = imageListing.get(name);
    if (fullPath == null) {
      throw new IllegalArgumentException(String.format(
        "name %s not in listing: %s", name, imageListing.keySet().toString()));
    }

    result = loadImage(fullPath);
    images.put(name, result);
    return result;
  }

  public void initializeVideos(LX lx) {
    String contentPath = getCanonicalContentPath(lx, "videos");

    File dir = new File(contentPath);
    FileFilter filter = getFileFilterForExtensions(Arrays.asList("mov", "mp4"));
    File[] directoryListing = dir.listFiles(filter);

    if (directoryListing != null) {
      for (File child : directoryListing) {
        videoListing.put(child.getName(), child.getAbsolutePath());
        if (PREPARE_MEDIA) {
          prepareVideo(child.getName());
        }
      }
    }

    logger.info(String.format("videoListing: %s", videoListing.keySet().toString()));
  }

  public Movie prepareVideo(String name) {
    Movie result = videos.get(name);
    if (result != null) {
      return result;
    }
    String fullPath = videoListing.get(name);
    if (fullPath == null) {
      throw new IllegalArgumentException(String.format(
        "name %s not in listing: %s", name, videoListing.keySet().toString()));
    }
    result = new Movie(this, fullPath);
    result.loop();
    videos.put(name, result);
    return result;
  }

  public Movie prepareVideo(String name, float volume) {
    Movie result = prepareVideo(name);
    result.volume(volume);
    return result;
  }

  public void initializeFonts(LX lx) {
    String contentPath = getCanonicalContentPath(lx, "fonts");

    File dir = new File(contentPath);
    FileFilter filter = getFileFilterForExtensions(Arrays.asList("ttf", "otf", "vlw"));
    File[] directoryListing = dir.listFiles(filter);

    if (directoryListing != null) {
      for (File child : directoryListing) {
        fontListing.put(child.getName(), child.getAbsolutePath());
        if (PREPARE_MEDIA) {
          prepareFont(child.getName());
        }
      }
    }

    logger.info(String.format("fontListing: %s", fontListing.keySet().toString()));
  }

  public PFont prepareFont(String name, int size) {
    String fullPath = fontListing.get(name);
    if (fullPath == null) {
      throw new IllegalArgumentException(String.format(
        "name %s not in listing: %s", name, fontListing.keySet().toString()));
    }
    String[] splitResult = splitExt(fullPath);
    if(splitResult[1] != "vlw") {
      name = String.join(".", String.format("%s-%d", splitResult[0], size), "vlw");
    }
    PFont result = fonts.get(name);
    if (result != null) {
      return result;
    }
    if(splitResult[1] != "vlw") {
      result = createFont(fullPath, size);
    } else {
      result = loadFont(fullPath);
    }
    fonts.put(name, result);
    return result;
  }

  public PFont prepareFont(String name) {
    return prepareFont(name, DEFAULT_FONT_SIZE);
  }

  void initializeVideoFrame(LX lx) {
    if (config.activeImage != null) {
      videoFrame = prepareImage(config.activeImage);
    } else if (config.activeMovie != null) {
      movie = prepareVideo(config.activeMovie, config.movieVolume);
      while (!movie.available())
        ;
      movie.read();
      if (videoFrame == null)
        videoFrame = createImage(movie.width, movie.height, RGB);
    } else if (config.screenCapBounds != null) {
      activeScreen = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice();
      int activeScreenWidth = activeScreen.getDisplayMode().getWidth();
      int activeScreenHeight = activeScreen.getDisplayMode().getHeight();
      logger.info(String.format("active screen dimensions: [%d, %d]",
        activeScreenWidth, activeScreenHeight));
      screenCapRectangle = new Rectangle(
        (int) (config.screenCapBounds[0] * activeScreenWidth),
        (int) (config.screenCapBounds[1] * activeScreenHeight),
        (int) (config.screenCapBounds[2] * activeScreenWidth),
        (int) (config.screenCapBounds[3] * activeScreenHeight));
      logger.info(String.format("screenCap rectangle: %s", screenCapRectangle));
      try {
        robot = new Robot(activeScreen);
      } catch (Exception e) {
        logger.warning(e.getMessage());
      }
      BufferedImage screenBuffer = robot
        .createScreenCapture(screenCapRectangle);
      videoFrame = new PImage(screenBuffer);
    }
    if (videoFrame != null)
      logger.info(String.format("videoFrame: %d x %d", videoFrame.width,
        videoFrame.height));
  }

  public void initializeUI(LXStudio lx, LXStudio.UI ui) {
    // Here is where you may modify the initial settings of the UI before it is
    // fully built. Note that this will not be called in headless mode. Anything
    // required for headless mode should go in the raw initialize method above.

    if (LPMeshable.useRightHandedCoordinates) {
      ui.setCoordinateSystem(CoordinateSystem.valueOf("RIGHT_HANDED"));
    }

    try {
      MethodUtils.invokeMethod(FieldUtils.readField(ui, "registry", true), true,
        "addUIFixtureControls", new Object[] { UIPanelFixture.class });
      // logger.info(String.format("ui.registry.fixtureControls: %s",
      //   FieldUtils.readField(ui, "registry", true)));
    } catch (Exception e) {
      logger.warning(e.toString());
    }

  }

  public void onUIReady(LXStudio lx, LXStudio.UI ui) {
    // At this point, the LX Studio application UI has been built. You may now
    // add additional views and components to the Ui hierarchy.

    for (LPDecoration decoration : config.decorations) {
      ui.preview.addComponent(new UIWireFrame(decoration));
    }
    for (LPDecoration debugDecoration : config.debugDecorations) {
      ui.preview.addComponent(new UIWireFrame(debugDecoration, 0xff0000));
    }

    ui.preview.addComponent(new UIAxes());

    onUIReadyMovie(lx, ui);
  }

  void onUIReadyMovie(LXStudio lx, LXStudio.UI ui) {
    if (videoFrame == null)
      initializeVideoFrame(lx);
    if (videoFrame == null)
      return;

    List<float[]> vertexUVPairs = new ArrayList<float[]>();

    vertexUVPairs
      .add(new float[] { flatBounds[0][0], flatBounds[1][0], 0, 0, 0 });
    vertexUVPairs.add(new float[] { flatBounds[0][1], flatBounds[1][0], 0,
      videoFrame.width, 0 });
    vertexUVPairs.add(new float[] { flatBounds[0][1], flatBounds[1][1], 0,
      videoFrame.width, videoFrame.height });
    vertexUVPairs.add(new float[] { flatBounds[0][0], flatBounds[1][1], 0, 0,
      videoFrame.height });
    for (float[] vertexUVPair : vertexUVPairs) {
      PVector uvPosition = new PVector(vertexUVPair[0], vertexUVPair[1],
        vertexUVPair[2]);
      PVector unflattened = LPMeshable.coordinateTransform(unflattener,
        uvPosition);
      vertexUVPair[0] = unflattened.x;
      vertexUVPair[1] = unflattened.y;
      vertexUVPair[2] = unflattened.z;
      logger.fine(String.format("unflattened uv position %s to %s",
        LPMeshable.formatPVector(uvPosition),
        LPMeshable.formatPVector(unflattened)));
    }
    ui.preview.addComponent(new UIVideoFrame(vertexUVPairs, videoFrame));
  }

  private final List<LXLoopTask> drawLoopTasks = new ArrayList<LXLoopTask>();
  private final List<LXLoopTask> drawLoopTasksToRemove = new ArrayList<LXLoopTask>();
  private final List<LXLoopTask> drawLoopTasksToAdd = new ArrayList<LXLoopTask>();

  /**
   * Add a task to be performed on every loop of draw().
   *
   * Can't be called from within a draw loop task, see scheduleDrawLoopTask
   *
   * @param drawLoopTask Task to be performed on every UI frame
   * @return this
   */
  public LXStudioApp addDrawLoopTask(LXLoopTask drawLoopTask) {
    drawLoopTasks.add(drawLoopTask);
    return this;
  }

  /**
   * Remove a draw loop task
   *
   * Can't be called from within a draw loop task, see
   * scheduleDrawLoopTaskRemoval
   *
   * @param drawLoopTask Task to be removed from work list
   * @return this
   */
  public LXStudioApp removeDrawLoopTask(LXLoopTask drawLoopTask) {
    drawLoopTasks.remove(drawLoopTask);
    return this;
  }

  /**
   * Schedule a task to be added to the draw loop tasks after the loop
   *
   * unlike addDrawLoopTask, this is safe to call from a loop task
   *
   * @param drawLoopTask Task to be removed from work list
   * @return this
   */
  public LXStudioApp scheduleDrawLoopTask(LXLoopTask drawLoopTask) {
    drawLoopTasksToAdd.add(drawLoopTask);
    return this;
  }

  /**
   * Schedule a task to be removed the draw loop tasks
   *
   * unlike removeDrawLoopTask, this is safe to call from a loop task
   *
   * @param drawLoopTask Task to be removed from work list
   * @return this
   */
  public LXStudioApp scheduleDrawLoopTaskRemoval(LXLoopTask drawLoopTask) {
    drawLoopTasksToRemove.add(drawLoopTask);
    return this;
  }

  /**
   * Schedule a task to be run once in the draw loop, disposes the task.
   *
   * unlike removeDrawLoopTask, this is safe to call from a loop task
   *
   * @param drawLoopTask Task to be removed from work list
   * @return this
   */
  public LXStudioApp scheduleDrawLoopTaskOnce(LXLoopTask drawLoopTask) {
    LXLoopTask oneOffTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        drawLoopTask.loop(deltaMs);
        scheduleDrawLoopTaskRemoval(this);
      }
    };
    scheduleDrawLoopTask(oneOffTask);
    return this;
  }

  @Override
  public void draw() {
    // All handled by core LX engine, do not modify, method exists only so that
    // Processing will run a draw-loop.
    for (LXLoopTask drawLoopTask : this.drawLoopTasksToAdd) {
      addDrawLoopTask(drawLoopTask);
    }
    drawLoopTasksToAdd.clear();
    for (LXLoopTask drawLoopTask : this.drawLoopTasks) {
      drawLoopTask.loop(0.f);
    }
    for (LXLoopTask drawLoopTask : this.drawLoopTasksToRemove) {
      removeDrawLoopTask(drawLoopTask);
    }
    drawLoopTasksToRemove.clear();
  }

  /**
   * Main interface into the program. Two modes are supported, if the --headless
   * flag is supplied then a raw CLI version of LX is used. If not, then we
   * embed in a Processing 3 applet and run as such.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    LX.log("Initializing LX version " + LXStudio.VERSION);
    boolean headless = false;
    File projectFile = null;
    for (int i = 0; i < args.length; ++i) {
      if ("--help".equals(args[i]) || "-h".equals(args[i])) {
      } else if ("--headless".equals(args[i])) {
        headless = true;
      } else if ("--fullscreen".equals(args[i]) || "-f".equals(args[i])) {
        FULLSCREEN = true;
      } else if ("--width".equals(args[i]) || "-w".equals(args[i])) {
        try {
          WIDTH = Integer.parseInt(args[++i]);
        } catch (Exception x) {
          LX.error("Width command-line argument must be followed by integer");
        }
      } else if ("--height".equals(args[i]) || "-h".equals(args[i])) {
        try {
          HEIGHT = Integer.parseInt(args[++i]);
        } catch (Exception x) {
          LX.error("Height command-line argument must be followed by integer");
        }
      } else if (args[i].endsWith(".lxp")) {
        try {
          projectFile = new File(args[i]);
        } catch (Exception x) {
          LX.error(x, "Command-line project file path invalid: " + args[i]);
        }
      }
    }
    if (headless) {
      // We're not actually going to run this as a PApplet, but we need to
      // explicitly construct and set the initialize callback so that any custom
      // components will be run
      LX.Flags flags = new LX.Flags();
      flags.initialize = new LXStudioApp();
      flags.mediaPath = System.getProperty("user.dir");
      if (projectFile == null) {
        LX.log("WARNING: No project filename was specified for headless mode!");
      }
      LX.headless(flags, projectFile);
    } else {
      PApplet.main(LXStudioApp.class, args);
    }
  }
}

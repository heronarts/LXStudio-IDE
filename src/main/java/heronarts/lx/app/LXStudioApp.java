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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import flavius.ledportal.LPDecoration;
import flavius.ledportal.LPMeshable;
import flavius.ledportal.LPSimConfig;
import flavius.ledportal.pattern.LPPanel3DRotatingCube;
import flavius.ledportal.pattern.LPPanelBLM;
import flavius.ledportal.pattern.LPPanelHexLife;
import flavius.ledportal.pattern.LPPanelProjectedVideo;
// import flavius.ledportal.pattern.LPPanelShader;
import flavius.ledportal.pattern.LPPanelShaderBlobby;
import flavius.ledportal.pattern.LPPanelShaderMonjori;
import flavius.ledportal.pattern.LPPanelShaderNebula;
import flavius.ledportal.pattern.LPPanelShaderPartyBlob;
import flavius.ledportal.pattern.LPPanelShaderSpiral;
import flavius.ledportal.pattern.LPPanelSolidState;
import flavius.ledportal.pattern.LPPanelTexture;
import flavius.ledportal.pattern.LPPanelVideo;
// import flavius.ledportal.pattern.LPPanelScreenCapture;
import flavius.ledportal.pattern.LPPanelCapture;
import flavius.ledportal.pattern.LPPanelGif;
import flavius.ledportal.structure.LPPanelFixture;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.LXPlugin;
import heronarts.lx.app.media.FontLibrary;
import heronarts.lx.app.media.GifLibrary;
import heronarts.lx.app.media.ImageLibrary;
import heronarts.lx.app.media.VideoLibrary;
import heronarts.lx.app.pattern.VideoFrame;
import heronarts.lx.app.ui.UIAxes;
import heronarts.lx.app.ui.UIPanelFixture;
import heronarts.lx.app.ui.UIWireFrame;
import heronarts.lx.pattern.GraphicEqualizerPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.p4lx.ui.UI.CoordinateSystem;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.video.Movie;

import java.lang.System;

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
  // private static boolean FULLSCREEN = true;

  protected static final Logger logger = Logger
    .getLogger(LXStudioApp.class.getName());

  public ImageLibrary imageLibrary = new ImageLibrary();
  public VideoLibrary videoLibrary = new VideoLibrary();
  public FontLibrary fontLibrary = new FontLibrary();
  public GifLibrary gifLibrary = new GifLibrary();

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
  public static LXStudio studio;
  public static LXStudioApp instance;

  /**
   * Last time a warning was emitted
   */
  long lastWarning = 0;

  private static int WINDOW_X = 0;
  private static int WINDOW_Y = 0;

  private static boolean HAS_WINDOW_POSITION = false;

  @Override
  public void settings() {
    System.setProperty("jogl.disable.openglcore", "true");
    // System.setProperty("jogl.disable.openglcore", "false");
    if (FULLSCREEN) {
      fullScreen(PApplet.P3D);
    } else {
      size(WIDTH, HEIGHT, PApplet.P3D);
    }
    int density = displayDensity();
    logger.info(String.format("density: %d", density));
    pixelDensity(displayDensity());
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
    // flags.startMultiThreaded = false;
    flags.startMultiThreaded = true;
    flags.mediaPath = System.getProperty("user.dir");
    studio = new LXStudio(this, flags);
    this.surface.setTitle(WINDOW_TITLE);
    if (!FULLSCREEN && HAS_WINDOW_POSITION) {
      this.surface.setLocation(WINDOW_X, WINDOW_Y);
    }
    addVideoFrameDrawLoopTask();
  }

  public void addVideoFrameDrawLoopTask() {
    LXLoopTask videoFrameTask = new LXLoopTask() {
      @Override
      public void loop(double deltaMs) {
        if (movie != null) {
          if (movie.available())
            movie.read();

          setPixelsFrom(videoFrame, movie);

        } else if (screenCapRectangle != null) {
          PImage screenBuffer = new PImage(
            robot.createScreenCapture(screenCapRectangle));

          setPixelsFrom(videoFrame, screenBuffer);
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
    lx.registry.addPattern(LPPanelVideo.class);
    // lx.registry.addPattern(LPPanelScreenCapture.class);
    lx.registry.addPattern(LPPanelGif.class);
    lx.registry.addPattern(LPPanelCapture.class);
    // lx.registry.addPattern(LPPanelShader.class);
    lx.registry.addPattern(LPPanelShaderBlobby.class);
    lx.registry.addPattern(LPPanelShaderSpiral.class);
    lx.registry.addPattern(LPPanelShaderPartyBlob.class);
    lx.registry.addPattern(LPPanelShaderNebula.class);
    lx.registry.addPattern(LPPanelShaderMonjori.class);
    lx.registry.addPattern(GraphicEqualizerPattern.class);
    lx.registry.addPattern(LPPanelProjectedVideo.class);
    // lx.registry.addEffect(heronarts.lx.app.effect.AppEffect.class);

    lx.registry.addFixture(LPPanelFixture.class);

    imageLibrary.init(lx);
    videoLibrary.init(lx);
    fontLibrary.init(lx);
    gifLibrary.init(lx);

    if (videoFrame == null)
      initializeVideoFrame(lx);
  }

  void initializeVideoFrame(LX lx) {
    if (config.activeImage != null) {
      videoFrame = imageLibrary.prepareMedia(config.activeImage);
    } else if (config.activeMovie != null) {
      movie = videoLibrary.prepareMedia(config.activeMovie, config.movieVolume);
      while (!movie.available())
        ;
      movie.read();
      if (videoFrame == null)
        videoFrame = createImage(movie.width, movie.height, ARGB);
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
      // FieldUtils.readField(ui, "registry", true)));
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
    if (videoFrame == null) {
      long now = System.currentTimeMillis();
      if (lastWarning == 0 || now - lastWarning > 10000) {
        logger.warning("videoframe is null");
        lastWarning = now;
      }
      return;
    }

    // List<float[]> vertexUVPairs = new ArrayList<float[]>();

    // vertexUVPairs
    //   .add(new float[] { flatBounds[0][0], flatBounds[1][0], 0, 0, 0 });
    // vertexUVPairs.add(new float[] { flatBounds[0][1], flatBounds[1][0], 0,
    //   videoFrame.width, 0 });
    // vertexUVPairs.add(new float[] { flatBounds[0][1], flatBounds[1][1], 0,
    //   videoFrame.width, videoFrame.height });
    // vertexUVPairs.add(new float[] { flatBounds[0][0], flatBounds[1][1], 0, 0,
    //   videoFrame.height });
    // for (float[] vertexUVPair : vertexUVPairs) {
    //   PVector uvPosition = new PVector(vertexUVPair[0], vertexUVPair[1],
    //     vertexUVPair[2]);
    //   PVector unflattened = LPMeshable.coordinateTransform(unflattener,
    //     uvPosition);
    //   vertexUVPair[0] = unflattened.x;
    //   vertexUVPair[1] = unflattened.y;
    //   vertexUVPair[2] = unflattened.z;
    //   logger.fine(String.format("unflattened uv position %s to %s",
    //     LPMeshable.formatPVector(uvPosition),
    //     LPMeshable.formatPVector(unflattened)));
    // }
    // ui.preview.addComponent(new UIVideoFrame(vertexUVPairs, videoFrame));
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
    synchronized (drawLoopTasksToAdd) {
      drawLoopTasksToAdd.add(drawLoopTask);
    }
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

  public void setPixelsFrom(PImage dst, PImage src) {
    if(dst == null || src == null) {
      return;
    }
    try {
      String srcClass = src.getClass().getName();
      if(srcClass == "processing.video.Capture" || srcClass == "processing.video.Movie") {
        int[] copyPixels = (int []) FieldUtils.readField(src, "copyPixels", true);
        int numPixels = Math.min(copyPixels.length, src.width * src.height);
        for( int i = 0; i < numPixels; i++) {
          int pixel = copyPixels[i];
          dst.pixels[i] = (
            (((pixel >> 16) & 0xff) << 0) |
            (pixel >> 0 & 0x00ff00) |
            (((pixel >> 0) & 0xff) << 16) |
            0xff000000
          );
        }
        dst.updatePixels();
      } else {
        dst.set(0, 0, src.get());
        // dst.copy(src, 0, 0, src.width, src.height, 0, 0, dst.width, dst.height);
      }
    } catch (Exception e) {
      logger.warning(e.toString());
    }
  }

  @Override
  public void draw() {
    // All handled by core LX engine, do not modify, method exists only so that
    // Processing will run a draw-loop.
    // return;
    synchronized (drawLoopTasks) {
      synchronized (drawLoopTasksToAdd) {
        for (LXLoopTask drawLoopTask : this.drawLoopTasksToAdd) {
          addDrawLoopTask(drawLoopTask);
        }
        drawLoopTasksToAdd.clear();
      }
      for (LXLoopTask drawLoopTask : this.drawLoopTasks) {
        drawLoopTask.loop(0.f);
      }
      synchronized (drawLoopTasksToRemove) {
        for (LXLoopTask drawLoopTask : this.drawLoopTasksToRemove) {
          removeDrawLoopTask(drawLoopTask);
        }
        drawLoopTasksToRemove.clear();
      }
    }
  }

  /**
   * Main interface into the program. Two modes are supported, if the --headless
   * flag is supplied then a raw CLI version of LX is used. If not, then we embed
   * in a Processing 4 applet and run as such.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    LX.log("Initializing LX version " + LXStudio.VERSION);
    boolean headless = false;
    File projectFile = null;
    for (int i = 0; i < args.length; ++i) {
      if ("--help".equals(args[i])) {
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
      } else if ("--windowx".equals(args[i]) || "-x".equals(args[i])) {
        try {
          WINDOW_X = Integer.parseInt(args[++i]);
          HAS_WINDOW_POSITION = true;
        } catch (Exception x ) {
          LX.error("Window X command-line argument must be followed by integer");
        }
      } else if ("--windowy".equals(args[i]) || "-y".equals(args[i])) {
        try {
          WINDOW_Y = Integer.parseInt(args[++i]);
          HAS_WINDOW_POSITION = true;
        } catch (Exception x ) {
          LX.error("Window Y command-line argument must be followed by integer");
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

  public void movieEvent(Movie m) {
    m.read();
  }
}

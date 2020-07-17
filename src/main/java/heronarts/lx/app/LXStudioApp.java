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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import flavius.ledportal.LPMeshable;
import flavius.ledportal.LPPanelFixture;
import flavius.ledportal.LPSimConfig;
import flavius.ledportal.LPStructure;
import heronarts.lx.LX;
import heronarts.lx.LX.Media;
import heronarts.lx.LXPlugin;
import heronarts.lx.app.pattern.HexLifePattern;
import heronarts.lx.app.pattern.Panel3DBLM;
import heronarts.lx.app.pattern.Panel3DRotatingCube;
import heronarts.lx.app.pattern.VideoFrame;
import heronarts.lx.app.ui.UIAxes;
import heronarts.lx.app.ui.UIPanelFixture;
import heronarts.lx.app.ui.UIVideoFrame;
import heronarts.lx.app.ui.UIWireFrame;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.LXOutput;
import heronarts.lx.pattern.GraphicEqualizerPattern;
import heronarts.lx.structure.SerialPacketStructure;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI.CoordinateSystem;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import processing.core.PApplet;
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

  private static final String WINDOW_TITLE = "LEDPortal";

  private static int WIDTH = 1280;
  private static int HEIGHT = 800;
  private static boolean FULLSCREEN = false;

  // TODO: get these from config
  public final int APA102_CLOCK_CHANNEL = 7;
  public final int APA102_FREQ = 800000;

  private static final Logger logger = Logger
    .getLogger(LXStudioApp.class.getName());
  public static LPSimConfig config;
  public static PImage videoFrame;
  public static PMatrix3D flattener;
  public static PMatrix3D unflattener;
  public static float[][] flatBounds;
  public static float[][] modelBounds;
  public static LXStudio studio;
  public static LXStudioApp instance;
  public static LXModel model;

  private Movie movie;
  private Robot robot;
  private GraphicsDevice activeScreen;
  private Rectangle screenCapRectangle;

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
    for (String activeStructure : config.activeStructures) {
      config.updateFromJSONObject(loadJSONObject(activeStructure));
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
    // model = config.getModel();
    studio = new LXStudio(this, flags);
    // studio = new LXStudio(this, flags, model);
    this.surface.setTitle(WINDOW_TITLE);
  }

  @Override
  public void initialize(LX lx) {
    // Here is where you should register any custom components or make
    // modifications
    // to the LX engine or hierarchy. This is also used in headless mode, so
    // note that
    // you cannot assume you are working with an LXStudio class or that any UI
    // will be
    // available.

    // Register custom pattern and effect types
    // lx.registry.addPattern(heronarts.lx.app.pattern.AppPattern.class);
    lx.registry.addPattern(VideoFrame.class);
    lx.registry.addPattern(Panel3DBLM.class);
    lx.registry.addPattern(Panel3DRotatingCube.class);
    lx.registry.addPattern(HexLifePattern.class);
    lx.registry.addPattern(GraphicEqualizerPattern.class);
    // lx.registry.addEffect(heronarts.lx.app.effect.AppEffect.class);

    lx.registry.addFixture(LPPanelFixture.class);

    if (videoFrame == null)
      initializeVideo(lx);
  }

  void initializeVideo(LX lx) {
    String mediaPrefix = "Content/media/";
    try {
      mediaPrefix = lx.getMediaFolder(Media.CONTENT).getCanonicalPath() + "/media/";
    } catch (IOException e) {
      logger.severe(String.format("could not get mediaPrefix: %s", e.toString()));
    }
    if (config.activeImage != null) {
      videoFrame = loadImage(mediaPrefix + config.activeImage);
    } else if (config.activeMovie != null) {
      movie = new Movie(this, (mediaPrefix + config.activeMovie));
      movie.loop();
      movie.volume(config.movieVolume);
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
    // fully
    // built. Note that this will not be called in headless mode. Anything
    // required
    // for headless mode should go in the raw initialize method above.

    if (LPMeshable.useRightHandedCoordinates) {
      ui.setCoordinateSystem(CoordinateSystem.valueOf("RIGHT_HANDED"));
    }

    try {
      MethodUtils.invokeMethod(
        FieldUtils.readField(ui, "registry", true),
        true,
        "addUIFixtureControls",
        new Object[] { UIPanelFixture.class }
      );
      logger.info(String.format("ui.registry.fixtureControls: %s", FieldUtils.readField(ui, "registry", true)));
    } catch (Exception e) {
      logger.warning(e.toString());
    }

    try {
      SerialPacketStructure structure = new SerialPacketStructure(lx);
      LXOutput output = structure.serialOutput;
      lx.addOutput(output);
      logger.info(String.format("added output %s", output));
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public void onUIReady(LXStudio lx, LXStudio.UI ui) {
    // At this point, the LX Studio application UI has been built. You may now
    // add
    // additional views and components to the Ui hierarchy.

    for (LPStructure structure : config.structures) {
      ui.preview.addComponent(new UIWireFrame(structure));
    }
    for (LPStructure debugStructure : config.debugStructures) {
      ui.preview.addComponent(new UIWireFrame(debugStructure, 0xff0000));
    }

    ui.preview.addComponent(new UIAxes());

    onUIReadyMovie(lx, ui);
  }

  void onUIReadyMovie(LXStudio lx, LXStudio.UI ui) {
    if (videoFrame == null)
      initializeVideo(lx);
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

  @Override
  public void draw() {
    // All handled by core LX engine, do not modify, method exists only so that
    // Processing
    // will run a draw-loop.
    if (movie != null && movie.available()) {
      movie.read();
      videoFrame.copy(movie, 0, 0, movie.width, movie.height, 0, 0, movie.width,
        movie.height);
    } else if (screenCapRectangle != null) {
      PImage screenBuffer = new PImage(
        robot.createScreenCapture(screenCapRectangle));
      videoFrame.copy(screenBuffer, 0, 0, screenBuffer.width,
        screenBuffer.height, 0, 0, screenBuffer.width, screenBuffer.height);
    }
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
      // explicitly
      // construct and set the initialize callback so that any custom components
      // will be run
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

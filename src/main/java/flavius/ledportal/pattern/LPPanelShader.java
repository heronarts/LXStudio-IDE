package flavius.ledportal.pattern;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.app.media.MediaLibrary;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

public class LPPanelShader extends LPPanel3DGraphicsPattern {
  PShader shader;
  String shaderPath;

  public LPPanelShader(LX lx) {
    super(lx);
    shaderPath = MediaLibrary.getCanonicalContentPath(lx, "shaders");
    // shader = LXStudioApp.instance.loadShader(shaderPath + "/ToonFrag.glsl", shaderPath + "/ToonVert.glsl");
    shader = LXStudioApp.instance.loadShader(shaderPath + "/LineFrag.glsl", shaderPath + "/LineVert.glsl");

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("scale", this.scale);
    addParameter("xScanFuckery", this.xScanFuckery);
    addParameter("yScanFuckery", this.yScanFuckery);
    addParameter("pScanFuckery", this.pScanFuckery);
  }

  @Override
  public void beforeDraw(final PGraphics pg) {
    super.beforeDraw(pg);
    pg.shader(shader, PConstants.LINES);
    pg.noStroke();
  }

  @Override
  public void onDraw(final PGraphics pg) {

    pg.pushMatrix();
    applyBackground();
    applyScale();
    applyTranslation();
    pg.pointLight(200, 200, 200, 1, 1, 1);
    // pg.stroke(pg.color(0xff));
    pg.fill(0x77000000);
    applyShear();
    applyRotation();
    applyScale();
    pg.popMatrix();
  }
}

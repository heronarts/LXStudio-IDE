package flavius.ledportal.pattern;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.app.media.MediaLibrary;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

public class LPPanelShader extends LPPanel3DGraphicsPattern {
  List<PVector> vertices = new ArrayList<PVector>();
  List<int[]> edges = new ArrayList<int[]>();
  List<int[]> faces = new ArrayList<int[]>();

  PShader shader;
  String shaderPath;

  public final CompoundParameter thicc = new CompoundParameter("Thicc", 0.5, 0,
    1).setDescription("The thiccness of the polyhedron lines")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

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
    addParameter("thicc", this.thicc);
    addParameter("xScanFuckery", this.xScanFuckery);
    addParameter("yScanFuckery", this.yScanFuckery);
    addParameter("pScanFuckery", this.pScanFuckery);

    LPPanel3DRotatingCube.youreACubeHarry(vertices, edges, faces);
  }

  @Override
  public void beforeDraw(final PGraphics pg) {
    super.beforeDraw(pg);
    pg.shader(shader, PConstants.LINES);
    pg.noStroke();
  }

  @Override
  public void onDraw(final PGraphics pg) {

    final float thicc = this.thicc.getValuef();
    final float weight = (float) (Math.pow(10,
      (double) (2 * (thicc - 1))));

    pg.pushMatrix();
    applyBackground();
    applyScale();
    applyTranslation();
    pg.pointLight(200, 200, 200, 1, 1, 1);
    // pg.stroke(pg.color(0xff));
    pg.fill(0x77000000);
    pg.strokeWeight(weight);
    applyShear();
    applyRotation();
    applyScale();


    for (int[] face : faces) {
      pg.beginShape(PConstants.QUAD);
      PVector normal = new PVector();
      for (int vertexId : face) {
        normal = normal.add(vertices.get(vertexId));
      }
      pg.normal(normal.x, normal.y, normal.z);
      for (int vertexId : face) {
        // pg.fill(0xffff0000);
        pg.stroke(0xffff0000);
        if (vertexId < vertices.size() / 2) {
          // pg.fill(0xff0000ff);
          pg.stroke(0xff0000ff);
        }
        PVector vertex = vertices.get(vertexId);
        pg.vertex(vertex.x, vertex.y, vertex.z);
      }
      pg.endShape();
    }

    pg.popMatrix();
  }
}

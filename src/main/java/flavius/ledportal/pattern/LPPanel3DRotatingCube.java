package flavius.ledportal.pattern;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
// TODO: rename LPPanel3DRotatingGeometry
public class LPPanel3DRotatingCube extends LPPanel3DGraphicsPattern {
  int lineLength;
  List<PVector> vertices = new ArrayList<PVector>();
  List<int[]> edges = new ArrayList<int[]>();

  public static float φ = (float) (1 + Math.sqrt(5)) / 2;

  public final CompoundParameter thicc = new CompoundParameter("Thicc", 0.5, 0, 1)
    .setDescription("The thiccness of the polyhedron lines")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public enum Shape {
    CUBE, OCTAHEDRON, DODECAHEDRON
  };

  public final EnumParameter<Shape> shape = new EnumParameter<LPPanel3DRotatingCube.Shape>(
    "shape", Shape.CUBE);

  public LPPanel3DRotatingCube(LX lx) {
    super(lx);
    lineLength = (int) (Math.min(model.width, model.height) * 0.8);

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("xShear", this.xShear);
    addParameter("scale", this.scale);
    addParameter("thicc", this.thicc);
    addParameter("shape", this.shape);
    addParameter("xScanFuckery", this.xScanFuckery);
    addParameter("yScanFuckery", this.yScanFuckery);
    addParameter("pScanFuckery", this.pScanFuckery);
    addParameter("fov", this.fov);
    addParameter("depth", this.depth);

    // youreACubeHarry();
    // youreADodecahedronHarry();
    youreAnOctahedronHarry();
  }

  public void youreACubeHarry() {
    vertices.clear();
    edges.clear();
    final float s = 0.5f;
    vertices.add(new PVector(-s, -s, -s));
    vertices.add(new PVector(-s, -s, s));
    vertices.add(new PVector(-s, s, -s));
    vertices.add(new PVector(-s, s, s));
    vertices.add(new PVector(s, -s, -s));
    vertices.add(new PVector(s, -s, s));
    vertices.add(new PVector(s, s, -s));
    vertices.add(new PVector(s, s, s));
    edges.add(new int[] { 0, 1 });
    edges.add(new int[] { 1, 3 });
    edges.add(new int[] { 3, 2 });
    edges.add(new int[] { 2, 0 });
    edges.add(new int[] { 0, 4 });
    edges.add(new int[] { 1, 5 });
    edges.add(new int[] { 2, 6 });
    edges.add(new int[] { 3, 7 });
    edges.add(new int[] { 4, 5 });
    edges.add(new int[] { 5, 7 });
    edges.add(new int[] { 7, 6 });
    edges.add(new int[] { 6, 4 });
  }

  public void youreAnOctahedronHarry() {
    vertices.clear();
    edges.clear();
    final float s = 1;
    vertices.add(new PVector(s, 0, 0));
    vertices.add(new PVector(0, s, 0));
    vertices.add(new PVector(0, 0, s));
    vertices.add(new PVector(-s, 0, 0));
    vertices.add(new PVector(0, -s, 0));
    vertices.add(new PVector(0, 0, -s));
    edges.add(new int[] { 0, 1 });
    edges.add(new int[] { 1, 2 });
    edges.add(new int[] { 2, 3 });
    edges.add(new int[] { 3, 4 });
    edges.add(new int[] { 4, 5 });
    edges.add(new int[] { 5, 0 });
    edges.add(new int[] { 0, 2 });
    edges.add(new int[] { 2, 4 });
    edges.add(new int[] { 4, 0 });
    edges.add(new int[] { 1, 3 });
    edges.add(new int[] { 3, 5 });
    edges.add(new int[] { 5, 1 });
  }

  public void youreADodecahedronHarry() {
    vertices.clear();
    edges.clear();
    int δ = 10;
    int ε = 11;
    vertices.add(new PVector(0, +1, +φ)); /* 0 */
    vertices.add(new PVector(0, -1, +φ)); /* 1 */
    vertices.add(new PVector(0, +1, -φ)); /* 2 */
    vertices.add(new PVector(0, -1, -φ)); /* 3 */
    vertices.add(new PVector(+1, +φ, 0)); /* 4 */
    vertices.add(new PVector(-1, +φ, 0)); /* 5 */
    vertices.add(new PVector(+1, -φ, 0)); /* 6 */
    vertices.add(new PVector(-1, -φ, 0)); /* 7 */
    vertices.add(new PVector(+φ, 0, +1)); /* 8 */
    vertices.add(new PVector(+φ, 0, -1)); /* 9 */
    vertices.add(new PVector(-φ, 0, +1)); /* δ */
    vertices.add(new PVector(-φ, 0, -1)); /* ε */
    edges.add(new int[] { 0, 1 });
    edges.add(new int[] { 0, δ });
    edges.add(new int[] { 0, 5 });
    edges.add(new int[] { 0, 4 });
    edges.add(new int[] { 0, 8 });

    edges.add(new int[] { 1, δ });
    edges.add(new int[] { δ, 5 });
    edges.add(new int[] { 5, 4 });
    edges.add(new int[] { 4, 8 });
    edges.add(new int[] { 8, 1 });

    edges.add(new int[] { 1, 6 });
    edges.add(new int[] { 1, 7 });
    edges.add(new int[] { δ, 7 });
    edges.add(new int[] { δ, ε });
    edges.add(new int[] { 5, ε });
    edges.add(new int[] { 5, 2 });
    edges.add(new int[] { 4, 2 });
    edges.add(new int[] { 4, 9 });
    edges.add(new int[] { 8, 9 });
    edges.add(new int[] { 8, 6 });

    edges.add(new int[] { 2, ε });
    edges.add(new int[] { ε, 7 });
    edges.add(new int[] { 7, 6 });
    edges.add(new int[] { 6, 9 });
    edges.add(new int[] { 9, 2 });

    edges.add(new int[] { 3, 2 });
    edges.add(new int[] { 3, ε });
    edges.add(new int[] { 3, 7 });
    edges.add(new int[] { 3, 6 });
    edges.add(new int[] { 3, 9 });
  }

  @Override
  public void beforeDraw(final PGraphics pg) {
    super.beforeDraw(pg);
    pg.smooth(8);
  }

  @Override
  public void onDraw(final PGraphics pg) {
    final float thicc = this.thicc.getValuef();
    final Shape shape = this.shape.getEnum();

    switch (shape) {
    case CUBE:
      youreACubeHarry();
      break;
    case DODECAHEDRON:
      youreADodecahedronHarry();
      break;
    default:
    case OCTAHEDRON:
      youreAnOctahedronHarry();
      break;
    }

    pg.pushMatrix();
    applyBackground();
    pg.stroke(LXColor.WHITE);
    final float weight = (float) (Math.pow(10, (double) (2 * (thicc - 0.5) - 2)));
    applyScale();
    applyTranslation();
    applyShear();
    applyRotation();
    pg.noFill();
    pg.strokeWeight(weight);
    applyScale();
    for (int[] edge : edges) {
      final PVector from = vertices.get(edge[0]);
      final PVector to = vertices.get(edge[1]);
      pg.line(from.x, from.y, from.z, to.x, to.y, to.z);
    }
    pg.popMatrix();
  }
}

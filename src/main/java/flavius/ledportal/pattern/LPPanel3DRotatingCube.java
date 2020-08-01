package flavius.ledportal.pattern;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanel3DRotatingCube extends LPPanel3DGraphicsPattern {
  int lineLength;
  List<PVector> vertices = new ArrayList<PVector>();
  List<int[]> edges = new ArrayList<int[]>();

  public static float φ = (float)(1 + Math.sqrt(5))/2;

  public final CompoundParameter xOffset =
    new CompoundParameter("X-Off", 0, -1, 1)
    .setDescription("Sets the placement in the X axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yOffset =
    new CompoundParameter("Y-Off", 0, -1, 1)
    .setDescription("Sets the placement in the Y axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zOffset =
    new CompoundParameter("Z-Off", 0, -1, 1)
    .setDescription("Sets the placement in the Z axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter xRotate =
    new CompoundParameter("X-Rot", 0, -1, 1)
    .setDescription("Sets the rotation about the X axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yRotate =
    new CompoundParameter("Y-Rot", 0, -1, 1)
    .setDescription("Sets the rotation about the Y axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zRotate =
    new CompoundParameter("Z-Rot", 0, -1, 1)
    .setDescription("Sets the rotation about the Z axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter scale =
    new CompoundParameter("Size", 1, 0, 20)
    .setDescription("Sets the size");

  public final CompoundParameter thicc =
    new CompoundParameter("Thicc", 1, 0, 10)
    .setDescription("Sets the thiccness of the cube lines");

  public enum Shape {
    CUBE, OCTAHEDRON, DODECAHEDRON
  };

  public final EnumParameter<Shape> shape = new EnumParameter<LPPanel3DRotatingCube.Shape>("shape", Shape.CUBE);

  public LPPanel3DRotatingCube(LX lx) {
    super(lx);
    lineLength = (int)(Math.min(model.width, model.height) * 0.8);

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("size", this.scale);
    addParameter("thicc", this.thicc);
    addParameter("shape", this.shape);

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

  public void beforeDraw(final PGraphics pg) {
    super.beforeDraw(pg);
    pg.smooth(8);
  }

  @Override
  public void onDraw(final PGraphics pg) {
    final float xOffset = this.xOffset.getValuef();
    final float yOffset = this.yOffset.getValuef();
    final float zOffset = this.zOffset.getValuef();
    final float xRotate = this.xRotate.getValuef();
    final float yRotate = this.yRotate.getValuef();
    final float zRotate = this.zRotate.getValuef();
    final float scale = this.scale.getValuef();
    final float thicc = this.thicc.getValuef();
    final Shape shape = this.shape.getEnum();

    switch(shape) {
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
    pg.background(LXColor.BLACK);
    pg.stroke(LXColor.WHITE);
    pg.translate(xOffset * model.width, yOffset * model.height,
      zOffset * Math.max(model.width, model.height));
    pg.applyMatrix(new PMatrix3D(1.f, -0.5f, 0.f, 0.f, 0.f, 1.f, 0.f, 0.f, 0.f,
      0.f, 1.f, 0.f, 0.f, 0.f, 0.f, 1.f));
    pg.rotateX((float) Math.PI * xRotate);
    pg.rotateY((float) Math.PI * yRotate);
    pg.rotateZ((float) Math.PI * zRotate);
    pg.noFill();
    pg.scale(scale);
    final float weight = (float) Math.exp((double) (thicc - 5));
    pg.strokeWeight(weight);
    // pg.strokeWeight(thicc * thicc);
    for (int i = 0; i < edges.size(); i++) {
      final PVector from = vertices.get(edges.get(i)[0]);
      final PVector to = vertices.get(edges.get(i)[1]);
      pg.line(from.x, from.y, from.z, to.x, to.y, to.z);
      // logger.info(String.format("from %s to %s", from, to));
    }
    pg.popMatrix();
  }
}

package heronarts.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class Panel3DRotatingCube extends Panel3DGraphicsPattern {
  int lineLength;
  List<PVector> vertices = new ArrayList<PVector>();
  List<int[]> edges = new ArrayList<int[]>();

  public final CompoundParameter xOffset =
    new CompoundParameter("X-Off", 0, -1, 1)
    .setDescription("Sets the placement of the cube in the X axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yOffset =
    new CompoundParameter("Y-Off", 0, -1, 1)
    .setDescription("Sets the placement of the cube in the Y axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zOffset =
    new CompoundParameter("Z-Off", 0, -1, 1)
    .setDescription("Sets the placement of the cube in the Z axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter xRotate =
    new CompoundParameter("X-Rot", 0, -1, 1)
    .setDescription("Sets the rotation of the cube about the X axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yRotate =
    new CompoundParameter("Y-Rot", 0, -1, 1)
    .setDescription("Sets the rotation of the cube about the Y axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zRotate =
    new CompoundParameter("Z-Rot", 0, -1, 1)
    .setDescription("Sets the rotation of the cube about the Z axis")
    .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter scale =
    new CompoundParameter("Size", 1, 0, 10)
    .setDescription("Sets the size of the cube");

  public final CompoundParameter thicc =
    new CompoundParameter("Thicc", 1, 0, 10)
    .setDescription("Sets the thiccness of the cube lines");

  public Panel3DRotatingCube(LX lx) {
    super(lx);
    lineLength = (int)(Math.min(model.width, model.height) * 0.8);
    vertices.add(new PVector(-0.5f, -0.5f, -0.5f));
    vertices.add(new PVector(-0.5f, -0.5f, 0.5f));
    vertices.add(new PVector(-0.5f, 0.5f, -0.5f));
    vertices.add(new PVector(-0.5f, 0.5f, 0.5f));
    vertices.add(new PVector(0.5f, -0.5f, -0.5f));
    vertices.add(new PVector(0.5f, -0.5f, 0.5f));
    vertices.add(new PVector(0.5f, 0.5f, -0.5f));
    vertices.add(new PVector(0.5f, 0.5f, 0.5f));
    edges.add(new int[]{0, 1});
    edges.add(new int[]{1, 3});
    edges.add(new int[]{3, 2});
    edges.add(new int[]{2, 0});
    edges.add(new int[]{0, 4});
    edges.add(new int[]{1, 5});
    edges.add(new int[]{2, 6});
    edges.add(new int[]{3, 7});
    edges.add(new int[]{4, 5});
    edges.add(new int[]{5, 7});
    edges.add(new int[]{7, 6});
    edges.add(new int[]{6, 4});

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("size", this.scale);
    addParameter("thicc", this.thicc);
  }

  public void beforeDraw(PGraphics pg) {
    super.beforeDraw(pg);
    pg.smooth(8);
  }

  @Override
  public void onDraw(PGraphics pg) {
    final float xOffset = this.xOffset.getValuef();
    final float yOffset = this.yOffset.getValuef();
    final float zOffset = this.zOffset.getValuef();
    final float xRotate = this.xRotate.getValuef();
    final float yRotate = this.yRotate.getValuef();
    final float zRotate = this.zRotate.getValuef();
    final float scale = this.scale.getValuef();
    final float thicc = this.thicc.getValuef();

    pg.pushMatrix();
    pg.background(LXColor.BLACK);
    pg.stroke(LXColor.WHITE);
    pg.translate(xOffset * model.width, yOffset * model.height, zOffset * Math.max(model.width, model.height));
    pg.applyMatrix(new PMatrix3D(
      1.f, -0.5f, 0.f, 0.f,
      0.f, 1.f, 0.f, 0.f,
      0.f, 0.f, 1.f, 0.f,
      0.f, 0.f, 0.f, 1.f
    ));
    pg.rotateX((float)Math.PI* xRotate);
    pg.rotateY((float)Math.PI * yRotate);
    pg.rotateZ((float)Math.PI * zRotate);
    pg.noFill();
    pg.scale(scale);
    float weight = (float)Math.exp((double)(thicc-5));
    pg.strokeWeight(weight);
    // pg.strokeWeight(thicc * thicc);
    for(int i=0; i<edges.size(); i++) {
      PVector from = vertices.get(edges.get(i)[0]);
      PVector to = vertices.get(edges.get(i)[1]);
      pg.line(from.x, from.y, from.z, to.x, to.y, to.z);
    }
    pg.popMatrix();
  }
}

package heronarts.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

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
  }

  public void beforeDraw(PGraphics pg) {
    super.beforeDraw(pg);
    pg.smooth(8);
  }

  @Override
  public void onDraw(PGraphics pg) {
    pg.pushMatrix();
    pg.background(LXColor.BLACK);
    pg.stroke(LXColor.WHITE);
    pg.translate(3*model.width/4 - 1f, 2*model.height/3 + 1,0);
    pg.applyMatrix(new PMatrix3D(
      1.f, -0.5f, 0.f, 0.f,
      0.f, 1.f, 0.f, 0.f,
      0.f, 0.f, 1.f, 0.f,
      0.f, 0.f, 0.f, 1.f
    ));
    pg.rotateY((float) totalMs / 1000);
    pg.rotateZ((float) totalMs / 10000);
    pg.noFill();
    pg.scale(8);
    pg.strokeWeight(0.04f);
    for(int i=0; i<edges.size(); i++) {
      PVector from = vertices.get(edges.get(i)[0]);
      PVector to = vertices.get(edges.get(i)[1]);
      pg.line(from.x, from.y, from.z, to.x, to.y, to.z);
    }
    pg.popMatrix();
  }
}

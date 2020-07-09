package flavius.ledportal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import heronarts.lx.model.GridModel.Point;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 * Basically {@link heronarts.lx.model.GridModel} but each row
 * in the grid can have a different length
 */
public class LPPanelModel extends LXModel{
  public static int[][] getIndexBounds(int[][] indices) {
    int[][] result = new int[][]{
      new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE},
      new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE}
    };
    for(int[] coordinates: indices) {
      for(int axis = 0; axis < 2; axis++) {
        if(coordinates[axis] < result[axis][0]) {
          result[axis][0] = coordinates[axis];
        }
        if(coordinates[axis] > result[axis][1]) {
          result[axis][1] = coordinates[axis];
        }
      }
    }
    return result;
  }

  public static class PanelMetrics {
    public int xiMax;
    public int xiMin;
    public int yiMax;
    public int yiMin;
    public int width;
    public int height;
    int[][] indices;
    PMatrix3D transformation;

    public PanelMetrics(LXMatrix transformation, int[][] indices) {
      this(LPMeshable.lxMatrixToP3D(transformation), indices);
    }

    public PanelMetrics(PMatrix3D transformation, int[][] indices) {
      int[][] indexBounds = getIndexBounds(indices);
      this.xiMin = indexBounds[0][0];
      this.xiMax = indexBounds[0][1];
      this.yiMin = indexBounds[1][0];
      this.yiMax = indexBounds[1][1];
      this.width = xiMax - xiMin;
      this.height = yiMax - yiMin;
      this.transformation = transformation;
      this.indices = indices;
    }

    private List<LXPoint> toPoints() {
      List<LXPoint> points = new ArrayList<LXPoint>(indices.length);
      for(int[] coordinates: indices) {
        PVector worldVector = LPMeshable.coordinateTransform(
          transformation,
          new PVector((float)coordinates[0], (float)coordinates[1], 0.f));
        points.add(new Point(
          coordinates[0], coordinates[1], worldVector.x, worldVector.y, worldVector.z));
      }
      return points;
    }
  }

  public class Strip extends LXModel {

    public int index;
    public int xiMin = Integer.MAX_VALUE;
    public int xiMax = Integer.MIN_VALUE;
    public int yiMin = Integer.MAX_VALUE;
    public int yiMax = Integer.MIN_VALUE;

    public final Point[] points;

    public Strip(int index, List<LXPoint> pointList) {
      super(pointList);
      this.index = index;
      LXPoint[] points = ((LXModel) this).points;
      this.points = new Point[points.length];
      System.arraycopy(points, 0, this.points, 0, points.length);
      for(LXPoint point: pointList) {
        Point p = (Point)point;
        if(p.xi < xiMin) xiMin = p.xi;
        if(p.xi > xiMax) xiMax = p.xi;
        if(p.yi < yiMin) yiMin = p.yi;
        if(p.yi > yiMax) yiMax = p.yi;
      }
    }
  }

  /**
   * Points in the model
   */
  public final Point[] points;

  /**
   * All the rows in this model
   */
  public final List<Strip> rows;

  /**
   * All the columns in this model
   */
  public final List<Strip> columns;

  /**
   * Metrics for the grid
   */
  public final PanelMetrics metrics;

  /**
   * Width of the grid
   */
  public final int width;

  /**
   * Height of the grid
   */
  public final int height;

  public Strip getRowStrip(int yi) {
    Strip result = new Strip(0, new ArrayList<LXPoint>());
    for(Strip strip: rows) {
      if(strip.index == yi) {
        result = strip;
        break;
      }
    }
    return result;
  }

  public Strip getColumnStrip(int xi) {
    Strip result = new Strip(0, new ArrayList<LXPoint>());
    for(Strip strip: columns) {
      if(strip.index == xi) {
        result = strip;
        break;
      }
    }
    return result;
  }

  public LPPanelModel(PanelMetrics metrics) {
    super(metrics.toPoints(), LXModel.Key.GRID);
    this.metrics = metrics;
    this.width = metrics.width;
    this.height = metrics.height;

    // Shadow the parent class array and make our own strongly-typed one
    this.points = new Point[super.points.length];
    System.arraycopy(super.points, 0, this.points, 0, super.points.length);

    List<Strip> rows = new ArrayList<Strip>();
    for (int y = metrics.yiMin; y <= metrics.yiMax; y++) {
      List<LXPoint> row = new ArrayList<LXPoint>();
      for (int i = 0; i < points.length; i++) {
        if(points[i].yi == y) row.add(points[i]);
      }
      rows.add(new Strip(y, row));
    }
    this.rows = Collections.unmodifiableList(rows);

    List<Strip> columns = new ArrayList<Strip>();
    for (int x = metrics.xiMin; x <= metrics.xiMax; x++) {
      List<LXPoint> column = new ArrayList<LXPoint>();
      for (int i = 0; i < points.length; i++) {
        if(points[i].xi == x) column.add(points[i]);
      }
      columns.add(new Strip(x, column));
    }
    this.columns = Collections.unmodifiableList(columns);
  }

  /**
   * Constructs a panel model with specified grid indices and transformation matrix
   *
   */
  public LPPanelModel(PMatrix3D transformation, int[][] indices) {
    this(new PanelMetrics(transformation, indices));
  }
}

package flavius.ledportal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.SerialModel;
import heronarts.lx.transform.LXMatrix;

/**
 * Basically {@link heronarts.lx.model.GridModel} but each row in the grid can
 * have a different length
 */
public class LPPanelModel extends SerialModel {
  public static int[][] getIndexBounds(int[][] indices) {
    int[][] result = new int[][] {
      new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE },
      new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE } };
    for (int[] coordinates : indices) {
      for (int axis = 0; axis < 2; axis++) {
        if (coordinates[axis] < result[axis][0]) {
          result[axis][0] = coordinates[axis];
        }
        if (coordinates[axis] > result[axis][1]) {
          result[axis][1] = coordinates[axis];
        }
      }
    }
    return result;
  }

  public static int[][] extractIndices(List<Point> points) {
    int[][] result = new int[points.size()][2];
    for(int i = 0; i < points.size(); i++) {
      result[i][0] = points.get(i).xi;
      result[i][1] = points.get(i).yi;
    }
    return result;
  }

  public static List<Point> extractPoints(LPPanelModel[] models) {
    List<Point> result = new ArrayList<Point>();
    for (LPPanelModel model : models) {
      for (LXPoint point : model.points) {
        if (Point.class.isInstance(point)) {
          result.add((Point) point);
        }
      }
    }
    return result;
  }

  public static class Point extends LXPoint {
    // Global x index, used for structure level grid
    public int xi;
    // Global y index, used for structure level grid
    public int yi;
    // Local x index, used for local index transformations
    public int xil;
    // Local y index, used for local index transformations
    public int yil;

    public Point() {
      this(0, 0);
    }
    public Point(int xi, int yi) {
      this(xi, yi, (float)xi, (float)yi, 0.f);
    }
    public Point(int xi, int yi, float x, float y, float z) {
      this(xi, yi, xi, yi, x, y, z);
    }
    public Point(int xi, int yi, int xil, int yil, float x, float y, float z) {
      super(x, y, z);
      this.xi = xi;
      this.yi = yi;
      this.xil = xil;
      this.yil = yil;
    }

    public void localIndexTransform(LXMatrix matrix) {
      this.x = matrix.m11 * this.xil + matrix.m12 * this.yil + matrix.m13 * 1
        + matrix.m14;
      this.y = matrix.m21 * this.xil + matrix.m22 * this.yil + matrix.m23 * 1
        + matrix.m24;
      this.z = matrix.m31 * this.xil + matrix.m32 * this.yil + matrix.m33 * 1
        + matrix.m34;
    }

    public LXPoint set(Point that) {
      set((LXPoint)that);
      this.xi = that.xi;
      this.yi = that.yi;
      this.xil = that.xi;
      this.yil = that.yi;

      return (LXPoint)this;
    }
  }

  public static class PanelMetrics {
    public int xiMax;
    public int xiMin;
    public int yiMax;
    public int yiMin;
    public int width;
    public int height;
    int[][] indices;

    public PanelMetrics(int[][] indices) {
      int[][] indexBounds = getIndexBounds(indices);
      this.xiMin = indexBounds[0][0];
      this.xiMax = indexBounds[0][1];
      this.yiMin = indexBounds[1][0];
      this.yiMax = indexBounds[1][1];
      this.width = xiMax - xiMin;
      this.height = yiMax - yiMin;
      this.indices = indices;
    }

    private List<LXPoint> toPoints(LXMatrix transform) {
      List<LXPoint> points = new ArrayList<LXPoint>(indices.length);
      int index = 0;
      for (int[] coordinates : indices) {
        Point point = new Point(
          coordinates[0], coordinates[1],
          (float) coordinates[0], (float) coordinates[1], 0.f);
        point.index = index++;
        point.multiply(transform);
        points.add(point);
      }
      return points;
    }
  }

  public class Strip extends SerialModel {

    public int index;
    public int xiMin = Integer.MAX_VALUE;
    public int xiMax = Integer.MIN_VALUE;
    public int yiMin = Integer.MAX_VALUE;
    public int yiMax = Integer.MIN_VALUE;

    public final Point[] points;

    public Strip(int index, List<LXPoint> pointList) {
      super(pointList);
      this.index = index;
      LXPoint[] points = ((SerialModel) this).points;
      this.points = new Point[points.length];
      System.arraycopy(points, 0, this.points, 0, points.length);
      for (LXPoint point : pointList) {
        Point p = (Point) point;
        if (p.xi < xiMin)
          xiMin = p.xi;
        if (p.xi > xiMax)
          xiMax = p.xi;
        if (p.yi < yiMin)
          yiMin = p.yi;
        if (p.yi > yiMax)
          yiMax = p.yi;
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
    for (Strip strip : rows) {
      if (strip.index == yi) {
        result = strip;
        break;
      }
    }
    return result;
  }

  public Strip getColumnStrip(int xi) {
    Strip result = new Strip(0, new ArrayList<LXPoint>());
    for (Strip strip : columns) {
      if (strip.index == xi) {
        result = strip;
        break;
      }
    }
    return result;
  }

  protected List<Strip> generateRows() {
    List<Strip> rows = new ArrayList<Strip>();
    for (int y = metrics.yiMin; y <= metrics.yiMax; y++) {
      List<LXPoint> row = new ArrayList<LXPoint>();
      for (int i = 0; i < points.length; i++) {
        if (points[i].yi == y)
          row.add(points[i]);
      }
      rows.add(new Strip(y, row));
    }
    return Collections.unmodifiableList(rows);
  }

  protected List<Strip> generateColumns() {
    List<Strip> columns = new ArrayList<Strip>();
    for (int x = metrics.xiMin; x <= metrics.xiMax; x++) {
      List<LXPoint> column = new ArrayList<LXPoint>();
      for (int i = 0; i < points.length; i++) {
        if (points[i].xi == x)
          column.add(points[i]);
      }
      columns.add(new Strip(x, column));
    }
    return Collections.unmodifiableList(columns);
  }

  /**
   * Constructs a panel model with specified grid indices and transformation
   * matrix
   */

  public LPPanelModel(PanelMetrics metrics, LXMatrix transform) {
    super(metrics.toPoints(transform), SerialModel.Key.GRID);
    this.metrics = metrics;
    this.width = metrics.width;
    this.height = metrics.height;

    // Shadow the parent class array and make our own strongly-typed one
    this.points = new Point[super.points.length];
    System.arraycopy(super.points, 0, this.points, 0, super.points.length);

    this.rows = generateRows();
    this.columns = generateColumns();
  }

  public LPPanelModel(PanelMetrics metrics, List<Point> points) {
    super(points.stream().map(point -> (LXPoint) point)
      .collect(Collectors.toList()), SerialModel.Key.GRID);
    this.metrics = metrics;

    this.width = metrics.width;
    this.height = metrics.height;
    this.points = new Point[points.size()];
    points.toArray(this.points);

    this.rows = generateRows();
    this.columns = generateColumns();
  }

  /**
   * Constructs a panel model with specified grid indices and transformation
   * matrix
   */
  public LPPanelModel(int[][] indices, LXMatrix transform) {
    this(new PanelMetrics(indices), transform);
  }

  public LPPanelModel(List<Point> points) {
    this(new PanelMetrics(extractIndices(points)), points);
  }

  public LPPanelModel(LPPanelModel[] subModels) {
    this(extractPoints(subModels));
  }
}

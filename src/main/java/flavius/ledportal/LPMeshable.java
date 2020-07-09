package flavius.ledportal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import heronarts.lx.transform.LXMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

public abstract class LPMeshable {
  private static final Logger logger = Logger
    .getLogger(LPMeshable.class.getName());
  public List<PVector> vertices = new ArrayList<PVector>();
  public List<int[]> edges = new ArrayList<int[]>();
  public List<int[]> faces = new ArrayList<int[]>();
  public PMatrix3D matrix;
  public String name;

  /**
   * Whether to use right or left handed coordinate systems.
   *
   * The default LXStudio coordinate system is left handed, but a right handed coordinate system
   * is possible by setting `ui.setCoordinateSystem(CoordinateSystem.valueOf("RIGHT_HANDED"));` in
   * LXStudioApp.initializeUI.
   */
  public static final boolean useRightHandedCoordinates = false;

  /**
   * A matrix to convert from Cartesian to default P3D coordinate systems
   *
   * In typical Cartesian coordinate system conventions, The positive Z axis is pointing up, and
   * right-handed coordinates are used.
   *
   * In default Processing 3D, the negative Y axis is up, and a left-handed coordinate system is
   * used.
   */
  public static final PMatrix3D matCartToP3D = new PMatrix3D(
    1, 0, 0, 0,
    0, 0, -1, 0,
    0, -1, 0, 0,
    0, 0, 0, 1
  );

  /**
   * A matrix to convert from P3D to LX (left handed) coordinate systems.
   *
   * In the LXStudio UI, the camera is oriented with negative Y axis facing upwards.
   * This is equivalent to a 180 degree rotation about the X axis.
   */
  public static final PMatrix3D matP3DToLX = new PMatrix3D(
    1, 0, 0, 0,
    0, -1, 0, 0,
    0, 0, -1, 0,
    0, 0, 0, 1
  );

  /**
   * A matrix to convert from left to right handed coordinate systems
   */
  public static final PMatrix3D matLeftToRightHand = new PMatrix3D(
    1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, -1, 0,
    0, 0, 0, 1
  );

  /**
   * A matrix to convert from cartesian to LX coordinate system that takes handedness into account
   */
  public static final PMatrix3D matCartToLX = new PMatrix3D();
  {
    matCartToLX.preApply(matCartToP3D);
    matCartToLX.preApply(matP3DToLX);
    if (useRightHandedCoordinates) {
      matCartToLX.preApply(matLeftToRightHand);
    }
  }


  /**
   * A matrix to convert from cartesian to LX coordinate system that takes handedness into account
   */
  public static final PMatrix3D matLXToCart = new PMatrix3D();
  {
    matLXToCart.preApply(matCartToLX);
    matLXToCart.invert();
  }

  public static final PVector xAxis = new PVector(1, 0, 0);
  public static final PVector yAxis = new PVector(0, 1, 0);
  public static final PVector zAxis = new PVector(0, 0, 1);

  public static String floatFmt = "%7.3f";

  public LPMeshable() {
  }

  public static String formatPVector(PVector vector) {
    return String.format(
      "[" + String.join(", ", floatFmt, floatFmt, floatFmt) + "]", vector.x,
      vector.y, vector.z);
  }

  public static String formatPVectorList(List<PVector> vectors) {
    String output = "[";
    int size = vectors.size();
    if (size > 8) {
      for (PVector vector : vectors.subList(0, 3)) {
        output += "\n\t" + formatPVector(vector);
      }
      output += "\n\t" + "...";
      for (PVector vector : vectors.subList(size - 4, size - 1)) {
        output += "\n\t" + formatPVector(vector);
      }
    } else {
      for (PVector vector : vectors) {
        output += "\n\t" + formatPVector(vector);
      }
    }
    return output + "\n]";
  }

  public static String formatPMatrix3D(PMatrix3D matrix) {
    return String.format(
      String.format("[" + "\n\t[%1$s, %1$s, %1$s, %1$s]"
        + "\n\t[%1$s, %1$s, %1$s, %1$s]" + "\n\t[%1$s, %1$s, %1$s, %1$s]"
        + "\n\t[%1$s, %1$s, %1$s, %1$s]" + "\n]", floatFmt),
      matrix.m00, matrix.m01, matrix.m02, matrix.m03, matrix.m10, matrix.m11,
      matrix.m12, matrix.m13, matrix.m20, matrix.m21, matrix.m22, matrix.m23,
      matrix.m30, matrix.m31, matrix.m32, matrix.m33);
  }

  public List<PVector> getWorldVertices() {
    List<PVector> worldVertices = new ArrayList<PVector>();
    for (PVector vertex : this.vertices) {
      worldVertices.add(getWorldCoordinate(vertex));
    }
    logger.fine(
      String.format("world vertices: %s", formatPVectorList(worldVertices)));
    return worldVertices;
  }

  public static PVector getCentroid(List<PVector> points) {
    PVector result = new PVector();
    for (PVector point : points) {
      result = result.add(point);
    }
    result.div(points.size());
    logger.fine(String.format("centroid %s", LPMeshable.formatPVector(result)));
    return result;
  }

  /**
   * Get the normal of a plane defined by {@code points} Assumptions:
   * <ul>
   * <li>points are co-planar, so only the first 3 points need to be looked
   * at.</li>
   * <li>points are given in counter-clockwise order, from the direction
   * opposing the normal.</li></li>
   *
   * @param points A List of PVectors which define the plane (must be exactly 3
   *                 points)
   * @return PVector The normal of the plane
   */
  public static PVector getNormal(List<PVector> points) {
    PVector anticlockwise = PVector.sub(points.get(2), points.get(0));
    PVector clockwise = PVector.sub(points.get(1), points.get(0));
    PVector result = clockwise.cross(anticlockwise);
    logger.fine(String.format("normal %s", LPMeshable.formatPVector(result)));
    return result;
  }

  /**
   * Form a list of matrices which, when composed will transform all points on
   * the plane defined by `center` and `normal` onto the X-Y plane.
   *
   * @param center a center point on the plane
   * @param normal a normal to the plane
   * @return a list of {@link processing.core.PMatrix3D Matrices} to form a
   *         flattener matrix
   */
  private static List<PMatrix3D> getFlattenerComponents(PVector center,
    PVector normal) {
    PVector crossZ = normal.cross(zAxis);
    float zenith = PVector.angleBetween(normal, zAxis);
    float azimuth = PVector.angleBetween(crossZ, xAxis);
    logger.fine(String.format("zenith: %7.3f radians, azimuth: %7.3f radians",
      zenith, azimuth));
    List<PMatrix3D> result = new ArrayList<PMatrix3D>();
    PMatrix3D azimuthMatrix = new PMatrix3D();
    azimuthMatrix.rotate(-azimuth, zAxis.x, zAxis.y, zAxis.z);
    result.add(azimuthMatrix);
    logger
      .fine(String.format("azimuthMatrix: %s", formatPMatrix3D(azimuthMatrix)));
    PMatrix3D zenithMatrix = new PMatrix3D();
    zenithMatrix.rotate(zenith, crossZ.x, crossZ.y, crossZ.z);
    result.add(zenithMatrix);
    logger
      .fine(String.format("zenithMatrix: %s", formatPMatrix3D(zenithMatrix)));
    PMatrix3D translationMatrix = new PMatrix3D();
    translationMatrix.translate(-center.x, -center.y, -center.z);
    result.add(translationMatrix);
    logger.fine(String.format("translationMatrix: %s",
      formatPMatrix3D(translationMatrix)));
    return result;
  }

  public static PMatrix3D composeMatrices(List<PMatrix3D> matrices) {
    PMatrix3D result = new PMatrix3D();
    for (PMatrix3D matrix : matrices) {
      result.apply(matrix);
    }
    logger.fine(String.format("result: %s", formatPMatrix3D(result)));
    return result;
  }

  public static PMatrix3D inverseComposeMatrixArray(List<PMatrix3D> matrices) {
    PMatrix3D result = new PMatrix3D();
    Collections.reverse(matrices);
    for (PMatrix3D matrix : matrices) {
      Boolean success = matrix.invert();
      logger.fine(String.format("applying: %s, success %b",
        formatPMatrix3D(matrix), success));
      result.apply(matrix);
    }
    logger.fine(String.format("result: %s", formatPMatrix3D(result)));
    return result;
  }

  public static PMatrix3D getFlattener(PVector center, PVector normal) {
    return composeMatrices(getFlattenerComponents(center, normal));
  }

  public static PMatrix3D getUnflattener(PVector center, PVector normal) {
    return inverseComposeMatrixArray(getFlattenerComponents(center, normal));
  }

  public PVector getWorldCentroid() {
    return getCentroid(this.getWorldVertices());
  }

  public PVector getWorldNormal() {
    return getNormal(this.getWorldVertices());
  }

  public List<PVector[]> getWorldEdges() {
    List<PVector[]> worldEdges = new ArrayList<PVector[]>();
    for (int[] edge : this.edges) {
      PVector start = getWorldCoordinate(this.vertices.get(edge[0]));
      PVector end = getWorldCoordinate(this.vertices.get(edge[1]));
      worldEdges.add(new PVector[] { start, end });
    }
    return worldEdges;
  }

  public static PVector coordinateTransform(PMatrix3D matrix, PVector local) {
    PVector result = new PVector();
    if (matrix != null) {
      return matrix.mult(local.copy(), result);
    } else {
      return local.copy();
    }
  }

  public static PVector worldUITransform(PVector world) {
    return coordinateTransform(matCartToLX, world);
  }

  public static PVector pixelWorldTransform(PVector pixel) {
    return coordinateTransform(matLXToCart, pixel);
  }

  public static PVector worldPixelTransform(PVector world) {
    return coordinateTransform(matCartToLX, world);
  }

  public PVector getWorldCoordinate(PVector local) {
    return coordinateTransform(matrix, local);
  }

  public static LXMatrix p3DToLXMatrix(PMatrix3D m) {
    return new LXMatrix(
      m.m00, m.m01, m.m02, m.m03,
      m.m10, m.m11, m.m12, m.m13,
      m.m20, m.m21, m.m22, m.m23,
      m.m30, m.m31, m.m32, m.m33
    );
  }

  public static PMatrix3D lxMatrixToP3D(LXMatrix m) {
    return new PMatrix3D(
      m.m11, m.m12, m.m13, m.m14,
      m.m21, m.m22, m.m23, m.m24,
      m.m31, m.m32, m.m33, m.m34,
      m.m41, m.m42, m.m43, m.m44
    );
  }

  public PMatrix3D getUIMatrix() {
    List<PMatrix3D> matrices = new ArrayList<PMatrix3D>();
    matrices.add(matCartToLX);
    matrices.add(matrix);
    return composeMatrices(matrices);
  }

  public LPMeshable updateFromJSONObject(JSONObject jsonConfig) {
    if (jsonConfig.hasKey("name")) {
      String name = jsonConfig.getString("name");
      if (!name.equals("")) {
        logger.info(String.format("has name %s ", name));
        this.name = name;
      }
    }
    if (jsonConfig.hasKey("vertices")) {
      JSONArray vertexList = jsonConfig.getJSONArray("vertices");
      for (int i = 0; i < vertexList.size(); i++) {
        JSONArray vertex = vertexList.getJSONArray(i);
        this.vertices.add(new PVector(vertex.getFloat(0), vertex.getFloat(1),
          vertex.getFloat(2)));
      }
      logger.info(String.format("has %d vertices: %s", this.vertices.size(),
        formatPVectorList(this.vertices)));
    }
    if (jsonConfig.hasKey("matrix")) {
      JSONArray matrix = jsonConfig.getJSONArray("matrix");
      this.matrix = new PMatrix3D(matrix.getJSONArray(0).getFloat(0),
        matrix.getJSONArray(0).getFloat(1), matrix.getJSONArray(0).getFloat(2),
        matrix.getJSONArray(0).getFloat(3), matrix.getJSONArray(1).getFloat(0),
        matrix.getJSONArray(1).getFloat(1), matrix.getJSONArray(1).getFloat(2),
        matrix.getJSONArray(1).getFloat(3), matrix.getJSONArray(2).getFloat(0),
        matrix.getJSONArray(2).getFloat(1), matrix.getJSONArray(2).getFloat(2),
        matrix.getJSONArray(2).getFloat(3), matrix.getJSONArray(3).getFloat(0),
        matrix.getJSONArray(3).getFloat(1), matrix.getJSONArray(3).getFloat(2),
        matrix.getJSONArray(3).getFloat(3));
      logger
        .info(String.format("has matrix: %s", formatPMatrix3D(this.matrix)));
    }
    if (jsonConfig.hasKey("edges")) {
      JSONArray edgeList = jsonConfig.getJSONArray("edges");
      logger.info(String.format("has %d edges", edgeList.size()));
      for (int i = 0; i < edgeList.size(); i++) {
        JSONArray edge = edgeList.getJSONArray(i);
        this.edges.add(edge.getIntArray());
      }
    }
    if (jsonConfig.hasKey("faces")) {
      JSONArray faceList = jsonConfig.getJSONArray("faces");
      logger.info(String.format("has %d faces", faceList.size()));
      for (int i = 0; i < faceList.size(); i++) {
        JSONArray face = faceList.getJSONArray(i);
        int[] faceEdges = face.getIntArray();
        for (int j = 0; j < faceEdges.length; j++) {
          this.edges.add(new int[] { faceEdges[j],
            faceEdges[((j + 1) % (faceEdges.length))] });
        }
      }
    }

    return this;
  }

  /**
   * Get the bounds ({@code min}, {@code max}) in each axis of all
   * {@code points}
   *
   * @param points a List of {@link processing.core.PVector PVector}s
   * @return a list of bounds ({@code min}, {@code max}) for each axis ({@code
   *         x}, {@code y}, {@code z})
   */
  public static float[][] getAxisBounds(List<PVector> points) {

    float[][] axisBounds = new float[][] {
      new float[] { Float.MAX_VALUE, Float.MIN_VALUE },
      new float[] { Float.MAX_VALUE, Float.MIN_VALUE },
      new float[] { Float.MAX_VALUE, Float.MIN_VALUE } };
    for (PVector point : points) {
      float[] coordinates = point.array();
      for (int i = 0; i < 3; i++) {
        if (coordinates[i] < axisBounds[i][0]) {
          axisBounds[i][0] = coordinates[i];
        }
        if (coordinates[i] > axisBounds[i][1]) {
          axisBounds[i][1] = coordinates[i];
        }
      }
    }

    logger.info(String.format(
      "axisBounds: %7.3f <= X <= %7.3f ; %7.3f <= Y <= %7.3f; %7.3f <= Z <= %7.3f",
      axisBounds[0][0], axisBounds[0][1], axisBounds[1][0], axisBounds[1][1],
      axisBounds[2][0], axisBounds[2][1]));

    return axisBounds;
  }

}

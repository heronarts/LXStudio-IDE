package flavius.ledportal.structure;

import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import flavius.ledportal.LPMeshable;
import flavius.ledportal.LPPanelModel;
import flavius.ledportal.LPPanelModel.Point;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXBufferOutput;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.structure.LXFixture;
import heronarts.lx.structure.LXProtocolFixture;
import heronarts.lx.transform.LXMatrix;
import processing.core.PMatrix2D;
import processing.data.JSONArray;
import java.lang.Math;

/**
 * A fixture based around an `LPPanelModel`, with a grid layout and support for
 * network protocols.
 */
public class LPPanelFixture extends LXProtocolFixture {

  protected static final Logger logger = Logger
    .getLogger(LPPanelFixture.class.getName());

  public final StringParameter pointIndicesJSON = new StringParameter(
    "Point Indices", "[[0,0]]")
      .setDescription("A JSON array of integer points that make up this panel");

  public final BoundedParameter rowSpacing = new BoundedParameter("Row Spacing",
    1, 0, Integer.MAX_VALUE).setDescription("Spacing between rows in the grid");

  public final BoundedParameter columnSpacing = new BoundedParameter(
    "Column Spacing", 1, 0, Integer.MAX_VALUE)
      .setDescription("Spacing between columns in the grid");

  public final BoundedParameter rowShear = new BoundedParameter("Row Shear", 0,
    0, Integer.MAX_VALUE)
      .setDescription("Offset to add to each additional row");

  public final DiscreteParameter globalGridOriginX = new DiscreteParameter(
    "Global Grid Origin X", 0, Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2)
      .setDescription(
        "The X coordinate in the global grid of the origin index, [0, 0]");

  public final DiscreteParameter globalGridOriginY = new DiscreteParameter(
    "Global Grid Origin Y", 0, Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2)
      .setDescription(
        "The Y coordinate in the global grid of the origin index, [0, 0]");

  public final StringParameter globalGridMatrix = new StringParameter(
    "Global Grid Matrix", "[[1,0],[0,1]]").setDescription(
      "A JSON matrix of floats which transforms local indices to global grid indices");

  /**
   * A transformation to apply to the local grid indices in this fixture to give
   * the global grid indices. This is applied after the local grid indices have
   * been used to position the points.
   *
   */
  PMatrix2D gridTransform = new PMatrix2D();

  /**
   * Array of (x,y) local grid coordinates from which this fixtures points in 3d
   * are derived.
   */
  int[][] gridIndices;

  /**
   * Array of (x,y) global grid coordinates which are used to place the points
   * from this fixture onto a world grid containing other fixtures.
   */
  int[][] worldGridIndices;

  public LPPanelFixture(LX lx) {
    super(lx, "Panel");
    addOutputParameter("protocol", this.protocol);
    addOutputParameter("byteOrder", this.byteOrder);
    addOutputParameter("transport", this.transport);
    addOutputParameter("reverse", this.reverse);
    addOutputParameter("host", this.host);
    addOutputParameter("port", this.port);
    addOutputParameter("dmxChannel", this.dmxChannel);
    addOutputParameter("artNetUniverse", this.artNetUniverse);
    addOutputParameter("opcChannel", this.opcChannel);
    addOutputParameter("opcOffset", this.opcOffset);
    addOutputParameter("ddpDataOffset", this.ddpDataOffset);
    addOutputParameter("kinetPort", this.kinetPort);

    addMetricsParameter("pointIndicesJSON", this.pointIndicesJSON);
    addMetricsParameter("globalGridOriginX", this.globalGridOriginX);
    addMetricsParameter("globalGridOriginY", this.globalGridOriginY);
    addMetricsParameter("globalGridMatrix", this.globalGridMatrix);
    addMetricsParameter("rowSpacing", this.rowSpacing);
    addMetricsParameter("columnSpacing", this.columnSpacing);
    addMetricsParameter("rowShear", this.rowShear);
  }

  /**
   * regenerate gridTransform. this creates a 2×3 transformation which applies
   * the fixtures `globalGridMatrix` to a pixel's 2D (integer) coordinate, then
   * translates it by it's `globalGridOrigin{X|Y}`
   */
  public void regenerateGridTransform() {
    JSONArray parsed = JSONArray.parse(this.globalGridMatrix.getString());
    float[] row0 = parsed.getJSONArray(0).getFloatArray();
    float[] row1 = parsed.getJSONArray(1).getFloatArray();
    this.gridTransform.set( //
      row0[0], row0[1], this.globalGridOriginX.getValuei(), //
      row1[0], row1[1], this.globalGridOriginY.getValuei());
  }

  /**
   * regenerate `gridIndices` (local) and `worldGridIndices` by applying
   * `gridTransform` to the fixture's `pointIndicesJson`
   */
  public void regenerateGridIndices() {
    regenerateGridTransform();

    JSONArray parsed = JSONArray.parse(this.pointIndicesJSON.getString());
    int newSize = parsed.size();
    if (this.gridIndices == null || this.gridIndices.length != newSize) {
      this.gridIndices = new int[newSize][2];
    }
    if (this.worldGridIndices == null
      || this.worldGridIndices.length != newSize) {
      this.worldGridIndices = new int[newSize][2];
    }
    for (int i = 0; i < newSize; i++) {
      this.gridIndices[i] = parsed.getJSONArray(i).getIntArray();
      this.worldGridIndices[i][0] = Math
        .round(this.gridTransform.multX(gridIndices[i][0], gridIndices[i][1]));
      this.worldGridIndices[i][1] = Math
        .round(this.gridTransform.multY(gridIndices[i][0], gridIndices[i][1]));
      // logger.info(String.format("%s, %d, %d, %d, %d, %d",
      // this.label.getString(), i,
      // this.gridIndices[i][0], this.gridIndices[i][1],
      // this.worldGridIndices[i][0], this.worldGridIndices[i][1]));
    }
  }

  /**
   * Override to use LPPanelModel.Point as point type
   *
   * @param localIndex Index of the point relative to this fixture
   * @return LXPoint cast from LPPanelModel.Point subclass
   */
  protected LXPoint constructPoint(int localIndex) {
    int[] local = this.gridIndices[localIndex];
    int[] world = this.worldGridIndices[localIndex];
    return (LXPoint) new Point(world[0], world[1], local[0], local[1], 0, 0, 0);
  }

  /**
   * Override to use LPPanelModel.Point as point type
   *
   * @param copy Point to make a copy of
   * @return LXPoint cast from LPPanelModel.Point subclass
   */
  protected LXPoint copyPoint(LXPoint copy) {
    if (!Point.class.isInstance(copy)) {
      return (LXPoint) new Point().set(copy);
    }
    return (LXPoint) new Point().set((Point) copy);
  }

  /**
   * Override to use LPPanelModel as model type
   *
   * @param modelPoints Points in the model
   * @param childModels Child models
   * @param modelKeys   Model keys
   * @return LPPanelModel Instance
   */
  protected LXModel constructModel(List<LXPoint> modelPoints,
    List<? extends LXModel> childModels, String[] modelKeys) {
    LPPanelModel model = new LPPanelModel(modelPoints.stream()
      .map(point -> (Point) (point)).collect(Collectors.toList()));
    logger
      .info(String.format("constructed model for %s: %d x %d (%d) X(%d,%d), Y(%d,%d)",
        label.getString(),
        model.width, model.height, model.size, model.metrics.xiMin,
        model.metrics.xiMax, model.metrics.yiMin, model.metrics.yiMax));
    return model;
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    regenerateGridIndices();
    super.onParameterChanged(p);
  }

  @Override
  protected int size() {
    regenerateGridIndices();
    return this.gridIndices.length;
  }
  /**
   * Overrides LXFixture.BuildOutputs to produce the necessary set of outputs
   * for this fixture to be sent. calls {@link #addOutputDefinition(OutputDefinition)} or
   * {@link #addOutputDirect(LXOutput)} for each output.
   */
  @Override
  protected void buildOutputs() {
    Protocol protocol = this.protocol.getEnum();
    if (protocol == Protocol.NONE) {
      return;
    }
    InetAddress address = resolveHostAddress();
    if (address == null) {
      return;
    }

    addOutputDefinition(new OutputDefinition(
      protocol,
      getProtocolTransport(),
      address,
      getProtocolPort(),
      getProtocolUniverse(),
      getProtocolChannel(),
      (LXFixture.Segment) buildSegment()
    ));
  }

  @Override
  protected void computeGeometryMatrix(LXMatrix geometryMatrix) {
    float degreesToRadians = (float) Math.PI / 180;
    float rowSpacing = this.rowSpacing.getValuef();
    float columnSpacing = this.columnSpacing.getValuef();
    float rowShear = this.rowShear.getValuef();
    geometryMatrix.multiply(LPMeshable.p3DToLXMatrix(LPMeshable.matCartToLX));
    geometryMatrix.translate(this.x.getValuef(), this.y.getValuef(),
      this.z.getValuef());
    geometryMatrix.rotateZ(this.yaw.getValuef() * degreesToRadians);
    geometryMatrix.rotateY(this.pitch.getValuef() * degreesToRadians);
    geometryMatrix.rotateX(this.roll.getValuef() * degreesToRadians);
    geometryMatrix.multiply(LPMeshable.p3DToLXMatrix(
      LPMeshable.getRowColShearMatrix(rowSpacing, columnSpacing, rowShear)));
  }

  @Override
  protected void computePointGeometry(LXMatrix matrix, List<LXPoint> points) {
    regenerateGridIndices();
    for (LXPoint p : points) {
      ((Point) p).localIndexTransform(matrix);
    }
  }

  protected class Segment extends LXFixture.Segment {
    protected Segment(int start, int num, int stride, boolean reverse, LXBufferOutput.ByteOrder byteOrder) {
      super(start, num, stride, reverse, byteOrder);
    }
  }

  protected Segment buildSegment() {
    return new Segment(0, size(), 1, this.reverse.isOn(), this.byteOrder.getEnum());
  }

}

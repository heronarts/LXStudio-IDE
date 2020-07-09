package flavius.ledportal;

import java.util.List;
import java.util.logging.Logger;

import flavius.ledportal.LPPanelModel.PanelMetrics;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.structure.LXProtocolFixture;
import heronarts.lx.structure.GridFixture.PositionMode;
import heronarts.lx.structure.GridFixture.Wiring;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import processing.data.JSONArray;

public class LPPanelFixture extends LXProtocolFixture {

  private static final Logger logger = Logger
    .getLogger(LPPanelFixture.class.getName());

  /**
   * Output protocols
   */
  public static enum LPProtocol {
    /**
     * No network output
     */
    NONE("None"),

    /**
     * Art-Net - <a href="https://art-net.org.uk/">https://art-net.org.uk/</a>
     */
    ARTNET("Art-Net"),

    /**
     * E1.31 Streaming ACN - <a href="https://opendmx.net/index.php/E1.31">https://opendmx.net/index.php/E1.31/</a>
     */
    SACN("E1.31 Streaming ACN"),

    /**
     * Open Pixel Control - <a href="http://openpixelcontrol.org/">http://openpixelcontrol.org/</a>
     */
    OPC("OPC"),

    /**
     * Distributed Display Protocol - <a href="http://www.3waylabs.com/ddp/">http://www.3waylabs.com/ddp/</a>
     */
    DDP("DDP"),

    /**
     * Color Kinetics KiNET - <a href="https://www.colorkinetics.com/">https://www.colorkinetics.com/</a>
     */
    KINET("KiNET"),

    /**
     * <a href="github.com/simap/pixelblaze_output_expander">Pixelblaze Output Expander Serial Protocol</a>
     */
    PBX("PixelBlaze Output Expander");

    private final String label;

    LPProtocol(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return this.label;
    }
  };

  public final EnumParameter<LPProtocol> protocol =
    new EnumParameter<LPProtocol>("Protocol", LPProtocol.NONE)
    .setDescription("Which lighting data protocol this fixture uses");

  public final EnumParameter<PositionMode> positionMode =
    new EnumParameter<PositionMode>("Mode", PositionMode.CORNER)
    .setDescription("Whether the arc is positioned by its starting point or center");

  public final EnumParameter<Wiring> wiring =
    new EnumParameter<Wiring>("Wiring", Wiring.ROWS_L2R_B2T)
    .setDescription("How the strips in the grid are sequentially wired");

  public final BooleanParameter splitPacket =
    new BooleanParameter("Split Packet", false)
    .setDescription("Whether to break a large grid into multiple datagrams on separate channels");

  public final DiscreteParameter pointsPerPacket =
    new DiscreteParameter("Points Per Packet", 170, 1, 21845)
    .setDescription("Number of LED points per packet");

  public final StringParameter pointIndicesJSON =
    new StringParameter("Point Indices", "[[0,0]]")
    .setDescription("A JSON array of integer points that make up this panel");

  public final BoundedParameter rowSpacing =
    new BoundedParameter("Row Spacing", 10, 0, 1000000)
    .setDescription("Spacing between rows in the grid");

  public final BoundedParameter columnSpacing =
    new BoundedParameter("Column Spacing", 10, 0, 1000000)
    .setDescription("Spacing between columns in the grid");

  public final BoundedParameter rowShear =
    new BoundedParameter("Row Shear", 0, 0, 1000000)
    .setDescription("Offset to add to each additional row");

  int[][] indices;

  public LPPanelFixture(LX lx) {
    super(lx, "Panel");
    addParameter("host", this.host);
    addDatagramParameter("protocol", this.protocol);
    addDatagramParameter("artNetUniverse", this.artNetUniverse);
    addDatagramParameter("opcChannel", this.opcChannel);
    addDatagramParameter("ddpDataOffset", this.ddpDataOffset);
    addDatagramParameter("kinetPort", this.kinetPort);

    addMetricsParameter("pointIndicesJSON", this.pointIndicesJSON);
    addGeometryParameter("rowSpacing", this.rowSpacing);
    addGeometryParameter("columnSpacing", this.columnSpacing);
    addGeometryParameter("rowShear", this.rowShear);
    addGeometryParameter("positionMode", this.positionMode);
    addDatagramParameter("wiring", this.wiring);
    addDatagramParameter("splitPacket", this.splitPacket);
    addDatagramParameter("pointsPerPacket", this.pointsPerPacket);
  }

  public void updateIndices(){
    JSONArray parsed = JSONArray.parse(this.pointIndicesJSON.getString());
    // logger.info(String.format("Parsing JSON: %s", parsed.toString()));
    int newSize = parsed.size();
    logger.info(String.format("newSize: %d", newSize));
    if(this.indices == null || this.indices.length != newSize) {
      this.indices = new int[newSize][];
    }
    for(int i = 0; i < newSize; i++) {
      this.indices[i] = parsed.getJSONArray(i).getIntArray();
    }
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    if( p == this.pointIndicesJSON ){
      updateIndices();
    }
    super.onParameterChanged(p);
  }

  @Override
  protected int size() {
    if(this.indices == null) updateIndices();
    return this.indices.length;
  }

  @Override
  protected void buildDatagrams() {
    // TODO: protected void buildDatagrams()
  }

  @Override
  protected void computeGeometryMatrix(LXMatrix geometryMatrix) {
    float degreesToRadians = (float) Math.PI / 180;
    geometryMatrix.multiply(LPMeshable.p3DToLXMatrix(LPMeshable.matCartToLX));
    geometryMatrix.translate(this.x.getValuef(), this.y.getValuef(), this.z.getValuef());
    geometryMatrix.rotateZ(this.yaw.getValuef() * degreesToRadians);
    geometryMatrix.rotateY(this.pitch.getValuef() * degreesToRadians);
    geometryMatrix.rotateX(this.roll.getValuef() * degreesToRadians);
  }

  @Override
  protected void computePointGeometry(LXMatrix matrix, List<LXPoint> points) {
    if(this.indices == null) updateIndices();
    // PanelMetrics metrics = new PanelMetrics(matrix, this.indices);
    LXTransform transform = new LXTransform(matrix);
    float rowSpacing = this.rowSpacing.getValuef();
    float columnSpacing = this.columnSpacing.getValuef();
    float rowShear = this.rowShear.getValuef();
    for(int i = 0; i<this.indices.length; i++ ){
      transform.push();
      int[] coordinates = this.indices[i];
      transform.translateX((rowSpacing * coordinates[0]) + (rowShear * coordinates[1]));
      transform.translateY(columnSpacing * coordinates[1]);
      points.get(i).set(transform);
      transform.pop();
    }
  }
}

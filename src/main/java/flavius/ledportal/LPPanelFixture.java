package flavius.ledportal;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.FieldUtils;

import flavius.ledportal.LPPanelModel.PanelMetrics;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.DDPDatagram;
import heronarts.lx.output.KinetDatagram;
import heronarts.lx.output.LXBufferDatagram;
import heronarts.lx.output.OPCDatagram;
import heronarts.lx.output.StreamingACNDatagram;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.structure.LXFixture;
import heronarts.lx.structure.LXFixtureContainer;
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
   * Serial protocols
   */
  public static enum SerialProtocol {
    /**
     * No network output
     */
    NONE("None"),

    /**
     * <a href="github.com/simap/pixelblaze_output_expander">Pixelblaze Output Expander Serial Protocol</a>
     */
    PBX("PixelBlaze Output Expander");

    private final String label;

    SerialProtocol(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return this.label;
    }
  };

  public final EnumParameter<SerialProtocol> serialProtocol =
    new EnumParameter<SerialProtocol>("Protocol", SerialProtocol.NONE)
    .setDescription("Which Serial lighting data protocol this fixture uses");

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

  public final StringParameter serialPort =
    new StringParameter("Serial Port", "")
    .setDescription("Serial Port this fixture connects to");

  public final BooleanParameter unknownSerialPort =
    new BooleanParameter("Unknown Serial Port", false);

  public final DiscreteParameter pixelBlazeChannel = (DiscreteParameter)
    new DiscreteParameter("PixelBlaze Expander Channel", 0, 0, 8)
    .setUnits(LXParameter.Units.INTEGER)
    .setDescription("Which physical PixelBlaze output channel is used");

  private final Set<LXParameter> serialParameters = new HashSet<LXParameter>();

  int[][] indices;


  public LPPanelFixture(LX lx) {
    super(lx, "Panel");
    addParameter("host", this.host);
    addDatagramParameter("protocol", this.protocol);
    addDatagramParameter("artNetUniverse", this.artNetUniverse);
    addDatagramParameter("opcChannel", this.opcChannel);
    addDatagramParameter("ddpDataOffset", this.ddpDataOffset);
    addDatagramParameter("kinetPort", this.kinetPort);
    // addDatagramParameter("opcPort", this.opcPort);
    addDatagramParameter("wiring", this.wiring);
    addDatagramParameter("splitPacket", this.splitPacket);
    addDatagramParameter("pointsPerPacket", this.pointsPerPacket);

    addSerialParameter("serialPort", this.serialPort);
    addSerialParameter("unknownSerialPort", this.unknownSerialPort);
    addSerialParameter("pixelBlazeChannel", this.pixelBlazeChannel);
    addSerialParameter("serialProtocol", this.serialProtocol);

    addMetricsParameter("pointIndicesJSON", this.pointIndicesJSON);
    addGeometryParameter("rowSpacing", this.rowSpacing);
    addGeometryParameter("columnSpacing", this.columnSpacing);
    addGeometryParameter("rowShear", this.rowShear);
    addGeometryParameter("positionMode", this.positionMode);
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

  private void regenerateSerial() {
    // TODO: private void regenerateSerial()
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    if( p == this.pointIndicesJSON ){
      updateIndices();
    }
    super.onParameterChanged(p);
    LXFixtureContainer container = null;
    try {
      container = (LXFixtureContainer) (FieldUtils.readField(this, "container", true));
    } catch (Exception e) {
      logger.warning(e.toString());
      return;
    }
    boolean isLoading = false;
    try {
      isLoading = (boolean) (FieldUtils.readField(this, "isLoading", true));
    } catch (Exception e) {
      logger.warning(e.toString());
      return;
    }
    if ((container != null) && !isLoading) {
      if (this.serialParameters.contains(p)) {
        regenerateSerial();
      }
    }
  }

  @Override
  protected int size() {
    if(this.indices == null) updateIndices();
    return this.indices.length;
  }

  private void addDatagram(InetAddress address, int[] indexBuffer, int channel) {
    LXBufferDatagram datagram = null;
    switch (this.protocol.getEnum()) {
    case ARTNET:
      datagram = new ArtNetDatagram(indexBuffer, channel);
      break;
    case SACN:
      datagram = new StreamingACNDatagram(indexBuffer, channel);
      break;
    case DDP:
      datagram = new DDPDatagram(indexBuffer, channel);
      break;
    case KINET:
      datagram = new KinetDatagram(indexBuffer, channel);
      break;
    case OPC:
      datagram = new OPCDatagram(indexBuffer, (byte) channel);
      datagram = (LXBufferDatagram)datagram.setPort(42069);
      break;
    default:
      LX.error("Undefined datagram protocol in GridFixture: " + this.protocol.getEnum());
      break;
    }
    if (datagram != null) {
      datagram.enabled.setValue(address != null);
      if (address != null) {
        datagram.setAddress(address);
      }
      addDatagram(datagram);
    }
  }

  @Override
  protected void buildDatagrams() {
    // TODO: protected void buildDatagrams()
    Protocol protocol = this.protocol.getEnum();
    if (protocol == Protocol.NONE) {
      return;
    }
    InetAddress address = resolveHostAddress();
    int[] wiringIndexBuffer = getWiringIndexBuffer();
    int pointsPerPacket = this.pointsPerPacket.getValuei();
    if (this.splitPacket.isOn() && (wiringIndexBuffer.length > pointsPerPacket)) {
      int i = 0;
      int channel = getProtocolChannel();
      while (i < wiringIndexBuffer.length) {
        int chunkSize = Math.min(pointsPerPacket, wiringIndexBuffer.length - i);
        int chunkIndexBuffer[] = new int[chunkSize];
        System.arraycopy(wiringIndexBuffer, i, chunkIndexBuffer, 0, chunkSize);
        addDatagram(address, chunkIndexBuffer, channel++);
        i += chunkSize;
      }
    } else {
      addDatagram(address, wiringIndexBuffer, getProtocolChannel());
    }
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
    int size = this.size();
    // PanelMetrics metrics = new PanelMetrics(matrix, this.indices);
    LXTransform transform = new LXTransform(matrix);
    float rowSpacing = this.rowSpacing.getValuef();
    float columnSpacing = this.columnSpacing.getValuef();
    float rowShear = this.rowShear.getValuef();
    for(int i = 0; i<size; i++ ){
      transform.push();
      int[] coordinates = this.indices[i];
      transform.translateX((rowSpacing * coordinates[0]) + (rowShear * coordinates[1]));
      transform.translateY(columnSpacing * coordinates[1]);
      points.get(i).set(transform);
      transform.pop();
    }
  }

  /**
   * Adds a parameter which impacts the serial outputs of the fixture. Whenever
   * one is changed, the serial parameters will be regenerated.
   *
   * @param path Path to parameter
   * @param parameter Parameter
   * @return this
   */
  protected LXFixture addSerialParameter(String path, LXParameter parameter) {
    addParameter(path, parameter);
    this.serialParameters.add(parameter);
    return this;
  }

  @Override
  protected String getModelKey() {
    return "panel";
  }

  private int[] getWiringIndexBuffer() {
    int size = this.size();
    int[] indexBuffer = new int[size];

    for(int i = 0; i<size; i++ ){
      indexBuffer[i] = this.points.get(i).index;
    }

    return indexBuffer;
  }

}

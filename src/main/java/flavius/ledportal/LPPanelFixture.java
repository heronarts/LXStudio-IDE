package flavius.ledportal;

import java.net.InetAddress;
import java.util.List;

import flavius.pixelblaze.PBColorOrder;
import flavius.pixelblaze.PBRecordType;
import flavius.pixelblaze.output.PBExpanderDataPacket;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.ArtNetDatagram;
import heronarts.lx.output.DDPDatagram;
import heronarts.lx.output.KinetDatagram;
import heronarts.lx.output.LXBufferDatagram;
import heronarts.lx.output.OPCDatagram;
import heronarts.lx.output.SerialDefinition;
import heronarts.lx.output.SerialPacket;
import heronarts.lx.output.StreamingACNDatagram;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.structure.GridFixture.PositionMode;
import heronarts.lx.structure.GridFixture.Wiring;
import heronarts.lx.structure.SerialProtocolFixture;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import processing.data.JSONArray;

public class LPPanelFixture extends SerialProtocolFixture {

  public final EnumParameter<PositionMode> positionMode =
    new EnumParameter<PositionMode>("Mode", PositionMode.CORNER)
    .setDescription("Whether the arc is positioned by its starting point or center");

  public final EnumParameter<Wiring> wiring =
    new EnumParameter<Wiring>("Wiring", Wiring.ROWS_L2R_B2T)
    .setDescription("How the strips in the grid are sequentially wired");

  public final BooleanParameter splitPacket =
    new BooleanParameter("Split Packet", false)
    .setDescription("Whether to break a large grid into multiple packets on separate channels");

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
    // TODO: specify OPC port
    // addDatagramParameter("opcPort", this.opcPort);
    addDatagramParameter("splitPacket", this.splitPacket);
    addDatagramParameter("pointsPerPacket", this.pointsPerPacket);

    addSerialParameter("serialPort", this.serialPort);
    addSerialParameter("baudRate", this.baudRate);
    addSerialParameter("unknownSerialPort", this.unknownSerialPort);
    addSerialParameter("pixelBlazeChannel", this.pixelBlazeChannel);
    addSerialParameter("serialProtocol", this.serialProtocol);

    addMetricsParameter("pointIndicesJSON", this.pointIndicesJSON);
    addMetricsParameter("wiring", this.wiring);
    addMetricsParameter("reverse", this.reverse);

    addGeometryParameter("rowSpacing", this.rowSpacing);
    addGeometryParameter("columnSpacing", this.columnSpacing);
    addGeometryParameter("rowShear", this.rowShear);
    addGeometryParameter("positionMode", this.positionMode);
  }

  public void updateIndices(){
    JSONArray parsed = JSONArray.parse(this.pointIndicesJSON.getString());
    int newSize = parsed.size();
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

  private void addPacket(SerialDefinition definition, int[] indexBuffer, int channel) {
    SerialPacket packet = null;
    // TODO: implement PixelBlaze color order
    PBColorOrder order = PBColorOrder.RGB;
    switch (this.serialProtocol.getEnum()) {
    case PBX_WS281X:
      packet = new PBExpanderDataPacket(PBRecordType.SET_CHANNEL_WS2812, order, indexBuffer, channel);
      break;
    case PBX_APA102:
      packet = new PBExpanderDataPacket(PBRecordType.SET_CHANNEL_APA102_DATA, order, indexBuffer, channel);
      break;
    default:
      LX.error("Undefined serial protocol in LPPanelFixture: " + this.serialProtocol.getEnum());
      break;
    }
    if (packet != null) {
      packet.enabled.setValue(definition != null);
      if (definition != null) {
        packet.setDefinition(definition);
      }
      addPacket(packet);
    }
  }

  SerialDefinition getSerialDefinition() {
    String portName = this.serialPort.getString();
    if(portName == "") {
      return null;
    }
    return new SerialDefinition(portName, (int)baudRate.getValue());
  }

  @Override
  protected void buildPackets() {
    SerialProtocol serialProtocol = this.serialProtocol.getEnum();
    if (serialProtocol == SerialProtocol.NONE) {
      return;
    }
    int[] wiringIndexBuffer = getWiringIndexBuffer();
    addPacket(getSerialDefinition(), wiringIndexBuffer, getSerialProtocolChannel());
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

  @Override
  protected int[] toDynamicIndexBuffer() {
    if (this.reverse.isOn()) {
      return super.toDynamicIndexBuffer(size() - 1, size(), -1);
    } else {
      return super.toDynamicIndexBuffer();
    }
  }

  @Override
  protected String getModelKey() {
    return "panel";
  }

  private int[] getWiringIndexBuffer() {
    int size = this.size();
    int[] indexBuffer = new int[size];

    if (this.reverse.isOn()) {
      for (int i = 0; i < size; i++) {
        indexBuffer[size-i-1] = this.points.get(i).index;
      }
    } else {
      for(int i = 0; i<size; i++ ){
        indexBuffer[i] = this.points.get(i).index;
      }
    }

    return indexBuffer;
  }

}

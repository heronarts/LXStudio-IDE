package flavius.pixelblaze.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.FieldUtils;
import jssc.SerialPort;

import flavius.pixelblaze.output.SerialPacket;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.structure.LXFixtureContainer;
import heronarts.lx.structure.LXProtocolFixture;
import heronarts.lx.transform.LXMatrix;

/**
 * Extends {@link heronarts.lx.structure.LXProtocolFixture} to allow for serial
 * protocols in addition to datagram protocols
 */
public abstract class SerialProtocolFixture extends LXProtocolFixture {

  private final Set<LXParameter> serialParameters = new HashSet<LXParameter>();

  private final List<SerialPacket> mutablePackets = new ArrayList<SerialPacket>();

  /**
   * Publicly accessible list of the packets that should be sent to this fixture
   */
  public final List<SerialPacket> packets = Collections.unmodifiableList(this.mutablePackets);

  private static final Logger logger = Logger
    .getLogger(SerialProtocolFixture.class.getName());

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
     * with WS281X
     */
    PBX_WS281X("PixelBlaze Expander WS281X"),

    /**
     * <a href="github.com/simap/pixelblaze_output_expander">Pixelblaze Output Expander Serial Protocol</a>
     * with WS281X
     */
    PBX_APA102("PixelBlaze Expander APA102");

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

  public final StringParameter serialPort =
    new StringParameter("Serial Port", "")
    .setDescription("Device name of the serial port this fixture connects to");

  public final BoundedParameter baudRate =
    new BoundedParameter("Serial Baud Rate", SerialPort.BAUDRATE_9600, SerialPort.BAUDRATE_110, SerialPort.BAUDRATE_256000)
    .setDescription("Baud rate of the serial port this fixture connects to");

  public final BooleanParameter unknownSerialPort =
    new BooleanParameter("Unknown Serial Port", false);

  public final DiscreteParameter pixelBlazeChannel = (DiscreteParameter)
    new DiscreteParameter("PixelBlaze Expander Channel", 0, 0, 8)
    .setUnits(LXParameter.Units.INTEGER)
    .setDescription("Which physical PixelBlaze output channel is used");

  protected SerialProtocolFixture(LX lx, String label) {
    super(lx, label);
  }

  /**
   * Adds a parameter which impacts the serial outputs of the fixture. Whenever
   * one is changed, the serial parameters will be regenerated.
   *
   * @param path Path to parameter
   * @param parameter Parameter
   * @return this
   */
  protected SerialProtocolFixture addSerialParameter(String path, LXParameter parameter) {
    addParameter(path, parameter);
    this.serialParameters.add(parameter);
    return this;
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    logger.info(String.format("parameter: %s", p.getLabel()));
    super.onParameterChanged(p);
    boolean isLoading = false;
    try {
      isLoading = (boolean) (FieldUtils.readField(this, "isLoading", true));
    } catch (Exception e) {
      logger.warning(e.toString());
      return;
    }
    if (!isLoading) {
      if (this.serialParameters.contains(p)) {
        regenerateSerialPackets();
      }
    }
  }

  private void regenerateSerialPackets() {
    logger.info("");
    // Dispose of all these packets
    for (SerialPacket packet : this.packets) {
      packet.dispose();
    }
    this.mutablePackets.clear();

    // Rebuild
    this.isInBuildSerialPackets = true;
    buildPackets();
    this.isInBuildSerialPackets = false;
  }


  private boolean isInBuildSerialPackets = false;

  /**
   * Subclasses must override this method to provide an implementation that
   * produces the necessary set of packets for this fixture to be sent.
   * The subclass should call {@link #addPacket(SerialPacket)} for each packet.
   */
  protected abstract void buildPackets();

  /**
   * Subclasses may override this method to update their packets in the
   * case that the point indexing of this fixture has changed. Packets
   * may be removed and readded inside this method if necessary. If the
   * {@link DynamicIndexBuffer} class has been used to construct indices for
   * packets, then no action should typically be necessary.
   */
  protected void reindexPackets() {}

  /**
   * Subclasses call this method to add a packet to thix fixture. This may only
   * be called from within the buildPackets() function.
   *
   * @param packet Packet to add
   */
  protected void addPacket(SerialPacket packet) {
    if (!this.isInBuildSerialPackets) {
      throw new IllegalStateException("May not add packets from outside buildPackets() method");
    }
    Objects.requireNonNull(packet, "Cannot add null packet to LXFixture.addPacket");
    if (this.mutablePackets.contains(packet)) {
      throw new IllegalStateException("May not add duplicate SerialPacket to LXFixture: " + packet);
    }
    this.mutablePackets.add(packet);
  }

  /**
   * Subclasses call this method to remove a packet from the fixture. This may only
   * be performed from within the reindexPackets or buildPackets methods.
   *
   * @param packet Packet to remove
   */
  protected void removePacket(SerialPacket packet) {
    if (!this.isInBuildSerialPackets) {
      throw new IllegalStateException("May not remove packets from outside reindexPackets() method");
    }
    if (!this.mutablePackets.contains(packet)) {
      throw new IllegalStateException("May not remove non-existent SerialPacket from LXFixture: " + packet + " " + this);
    }
    this.mutablePackets.remove(packet);
  }
}

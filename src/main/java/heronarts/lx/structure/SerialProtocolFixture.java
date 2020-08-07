package heronarts.lx.structure;

import java.net.InetAddress;
import java.util.logging.Logger;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;
import jssc.SerialPort;

/**
 * Extends {@link heronarts.lx.structure.LXProtocolFixture} to allow for serial
 * protocols in addition to datagram protocols
 */
public abstract class SerialProtocolFixture extends LXProtocolFixture {

  /**
   * Serial protocols
   */
  public static enum SerialProtocol {
    /**
     * No network output
     */
    NONE("None"),

    /**
     * <a href="github.com/simap/pixelblaze_output_expander">Pixelblaze Output
     * Expander Serial Protocol</a> with WS281X
     */
    PBX_WS281X("PixelBlaze Expander WS281X"),

    /**
     * <a href="github.com/simap/pixelblaze_output_expander">Pixelblaze Output
     * Expander Serial Protocol</a> with WS281X
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

  public final EnumParameter<SerialProtocol> serialProtocol = new EnumParameter<SerialProtocol>(
    "Protocol", SerialProtocol.NONE)
      .setDescription("Which Serial lighting data protocol this fixture uses");

  public final StringParameter serialPort = new StringParameter("Serial Port",
    "").setDescription(
      "Device name of the serial port this fixture connects to");

  public final DiscreteParameter baudRate = new DiscreteParameter(
    "Serial Baud Rate", SerialPort.BAUDRATE_9600, 1, 3000000)
      .setDescription("Baud rate of the serial port this fixture connects to");

  public final BooleanParameter unknownSerialPort = new BooleanParameter(
    "Unknown Serial Port", false);

  public final DiscreteParameter pixelBlazeChannel = (DiscreteParameter) new DiscreteParameter(
    "PixelBlaze Expander Channel", 0, 0, 8).setUnits(LXParameter.Units.INTEGER)
      .setDescription("Which physical PixelBlaze output channel is used");

  protected SerialProtocolFixture(LX lx, String label) {
    super(lx, label);
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    super.onParameterChanged(p);

    if (p == this.host) {
      InetAddress address = resolveHostAddress();
      for (LXOutput output : this.outputs) {
        if (output instanceof LXOutput.InetOutput) {
          output.enabled.setValue(address != null);
          if (address != null) {
            ((LXOutput.InetOutput) output).setAddress(address);
          }
        }
      }
    }
    // TODO(Dev): The serial equivalent of this
  }

  public int getSerialProtocolChannel() {
    switch (this.serialProtocol.getEnum()) {
    case PBX_WS281X:
    case PBX_APA102:
      return this.pixelBlazeChannel.getValuei();
    case NONE:
    default:
      return 0;
    }
  }
}

package flavius.pixelblaze.output;

import processing.serial.Serial;
import heronarts.lx.output.LXOutput;
import heronarts.lx.LX;
// import java.util.logging.Logger;

abstract class SerialOutput extends LXOutput {
  // private static final Logger logger =
  // Logger.getLogger(SerialOutput.class.getName());
  protected Serial serialPort;

  public SerialOutput(LX lx, Serial serialPort) {
    super(lx, serialPort.port.getPortName());
    this.serialPort = serialPort;
  }

  public void write(byte[] message) {
    // int i = 0;
    // for (byte b: message) {
    // logger.fine(String.format("message[%03d] = 0x%02x\n", i++, b));
    // }
    this.serialPort.write(message);
  }
}

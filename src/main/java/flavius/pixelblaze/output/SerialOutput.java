package flavius.pixelblaze.output;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;
import processing.serial.Serial;

abstract class SerialOutput extends LXOutput {
  protected Serial serialPort;
  protected String portName;
  protected int baudRate;
  protected int dataBits;
  protected int stopBits;
  protected int parity = SerialPort.PARITY_NONE;
  protected boolean disconnected = false;
  private static final Logger logger = Logger.getLogger(SerialOutput.class.getName());

  public SerialOutput(final LX lx, final Serial serialPort, final int baudRate, int dataBits, int stopBits, int parity) {
    super(lx, serialPort.port.getPortName());
    this.portName = serialPort.port.getPortName();
    this.serialPort = serialPort;
    this.baudRate = baudRate;
    this.dataBits = dataBits;
    this.stopBits = stopBits;
    this.parity = parity;
  }
  public SerialOutput(final LX lx, final Serial serialPort, final int baudRate) {
    this(lx, serialPort, baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
  }

  public void write(final byte[] message) {
    if(this.disconnected) {
      if (Files.exists(FileSystems.getDefault().getPath(this.portName))) {
        try {
          if(this.serialPort.port.isOpened()) this.serialPort.port.closePort();
          this.serialPort.port = new SerialPort(this.portName);
          if(!this.serialPort.port.isOpened()) this.serialPort.port.openPort();
          this.serialPort.port.setParams(baudRate, dataBits, stopBits, parity);
          this.disconnected = false;
          logger.warning(String.format("reopened serial port: %s", this.portName));
        } catch (SerialPortException e) {
          logger.warning(String.format("can't reopen serial port: %s", e.toString()));
        }
      }
    }
    if (! this.serialPort.active()) {
      if(! this.disconnected) {
        logger.warning(String.format("port %s is not active!", this.portName));
        this.disconnected = true;
      }
      return;
    }
    if (! this.serialPort.port.isOpened()) {
      if(! this.disconnected) {
        logger.warning(String.format("port %s is not opened!", this.portName));
        this.disconnected = true;
      }
      return;
    }
    if (! Files.exists(FileSystems.getDefault().getPath(this.portName))) {
      if(! this.disconnected) {
        logger.warning(String.format("port %s does not exist!", this.portName));
        this.disconnected = true;
      }
      return;
    }
    this.serialPort.write(message);
  }
}

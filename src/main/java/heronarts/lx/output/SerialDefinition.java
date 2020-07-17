package heronarts.lx.output;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import heronarts.lx.app.LXStudioApp;
import jssc.SerialPort;
import jssc.SerialPortException;
import processing.serial.Serial;

/**
 * Defines the Serial parameters required for a connection.
 *
 * Retains a mapping of serial port names to {@link processing.serial.Serial}
 * objects, so that it can enforce a single object per name, to prevent port
 * busy errors.
 */
public class SerialDefinition {
  // TODO(dev): implement reconnect
  public static int defaultDataBits = SerialPort.DATABITS_8;
  public static int defaultStopBits = SerialPort.STOPBITS_1;
  public static int defaultParity = SerialPort.PARITY_NONE;

  public String portName;
  public int baudRate;
  public int dataBits;
  public int stopBits;
  public int parity;

  public static Map<String, Serial> ports = new HashMap<String, Serial>();
  public static Map<String, Boolean> status = new HashMap<String, Boolean>();

  private static final Logger logger = Logger
    .getLogger(SerialDefinition.class.getName());

  public SerialDefinition(String portName, int baudRate, int dataBits,
    int stopBits, int parity) throws IllegalArgumentException {
    requireValidPortName(portName);
    this.portName = portName;
    this.baudRate = baudRate;
    this.dataBits = dataBits;
    this.stopBits = stopBits;
    this.parity = parity;
    // TODO(dev): Something better thatn this
    if (ports.containsKey(portName)) {
      ports.get(portName).dispose();
    }
    ports.put(portName, new Serial(LXStudioApp.instance, portName, baudRate));
  }

  public SerialDefinition(String portName, int baudRate)
    throws IllegalArgumentException {
    this(portName, baudRate, defaultDataBits, defaultStopBits, defaultParity);
  }

  public SerialDefinition(Serial serialPort, int baudRate) {
    // TODO: update ports and status.
    this(serialPort.port.getPortName(), baudRate);
  }

  public static List<String> validPortNames() {
    return Arrays.asList(Serial.list());
  }

  public static boolean isValidPortName(String portName) {
    return validPortNames().contains(portName);
  }

  public static void requireValidPortName(String portName)
    throws IllegalArgumentException {
    if (!isValidPortName(portName)) {
      throw new IllegalArgumentException(
        "a valid portNames is required. Valid portNames: "
          + String.join(",", validPortNames()));
    }
  }

  public static boolean isConnected(String portName) {
    return status.getOrDefault(portName, true);
  }

  public static void setDisconnected(String portName) {
    status.put(portName, false);
  }

  public static void setConnected(String portName) {
    status.put(portName, true);
  }

  /**
   * Write {@code message} to the serial port. Attempts to reconnect if
   * connection is interrupted.
   *
   * On unix, if a port is connected, the isRING flag will be zero, but if it's
   * disconnected, isRING will return random values. This is most reliable way
   * that i could find to detect loss of connection.
   *
   * @param message the message to write to the serial port
   */
  public void write(final byte[] message) throws IOException {
    Serial serialPort = ports.get(this.portName);
    // TODO(dev): Actually raise errors
    // String debug_msg = "";
    // for(byte b: message) {
    // debug_msg += String.format("%02x", b);
    // }
    // logger.info(String.format("this %s, message: %s", this, debug_msg));
    if (!isConnected(this.portName)) {
      if (isValidPortName(this.portName)) {
        try {
          if (serialPort.port.isOpened())
            serialPort.port.closePort();
          serialPort.port = new SerialPort(this.portName);
          if (!serialPort.port.isOpened())
            serialPort.port.openPort();
          serialPort.port.setParams(this.baudRate, this.dataBits, this.stopBits,
            this.parity);
          setConnected(this.portName);
          logger
            .warning(String.format("reopened serial port: %s", this.portName));
        } catch (SerialPortException e) {
          logger.warning(
            String.format("can't reopen serial port: %s", e.toString()));
        }
      } else {
        return;
      }
    }
    try {
      if (!serialPort.active()) {
        setDisconnected(this.portName);
        throw new IOException(
          String.format("serial port %s is disconnected", this.portName));
      }
      serialPort.port.writeBytes(message);
    } catch (SerialPortException e) {
      setDisconnected(this.portName);
      throw new IOException(
        String.format("can't write message: %s", e.toString()));
    }
  }
}

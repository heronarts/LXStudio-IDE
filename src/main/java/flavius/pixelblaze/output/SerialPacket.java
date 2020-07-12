package flavius.pixelblaze.output;

import java.util.HashMap;
import java.util.Map;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;

/**
 * A buffer that can be used as a message analogous to
 * {@link heronarts.lx.output.LXDatagram}
 */
public abstract class SerialPacket {
  protected static class ErrorState {
    // Destination address
    final String destination;

    // Number of failures sending to this packet address
    int failureCount = 0;

    // Timestamp to re-try sending to this address again after
    long sendAfter = 0;

    private ErrorState(String destination) {
      this.destination = destination;
    }
  }

  private static final Map<String, ErrorState> _packetErrorState =
    new HashMap<String, ErrorState>();

  private static ErrorState getDatagramErrorState(SerialPacket packet) {
    String destination = packet.definition.portName + ":" + packet.definition.baudRate;
    ErrorState packetErrorState = _packetErrorState.get(destination);
    if (packetErrorState == null) {
      _packetErrorState.put(destination, packetErrorState = new ErrorState(destination));
    }
    return packetErrorState;
  }

  protected final byte[] buffer;

  ErrorState errorState = null;

  protected SerialDefinition definition;

  /**
   * Whether this packet is active
   */
  public final BooleanParameter enabled =
    new BooleanParameter("Enabled", true)
    .setDescription("Whether this packet is active");

  /**
   * Whether this packet is in an error state
   */
  public final BooleanParameter error =
    new BooleanParameter("Error", false)
    .setDescription("Whether there have been errors sending to this packet address");

  /**
   * Brightness of the packet
   */
  public final BoundedParameter brightness =
    new BoundedParameter("Brightness", 1)
    .setDescription("Level of the output");

  protected SerialPacket(int size) {
    this.buffer = new byte[size];
    for (int i = 0; i < size; ++i) {
      this.buffer[i] = 0;
    }
  }

  protected ErrorState getErrorState() {
    if (this.errorState != null) {
      return this.errorState;
    }
    return this.errorState = getDatagramErrorState(this);
  }

  /**
   * Sets the destination serial configuration of this packet
   *
   * @param definition Destination serial configuration
   * @return this
   */
  public SerialPacket setDefinition(SerialDefinition definition) {
    this.definition = definition;
    return this;
  }

  /**
   * Gets the serial configuration this packet sends to
   *
   * @return Destination serial configuration
   */
  public SerialDefinition getDefinition() {
    return this.definition;
  }

  /**
   * Invoked by engine to send this packet when new color data is available. The
   * SerialPacket should update the buffer accordingly.
   *
   * @param colors Color buffer
   * @param glut Look-up table with gamma-adjusted brightness values
   */
  public abstract void onSend(int[] colors, byte[] glut);

  /**
   * Invoked when the packet is no longer needed. Typically a no-op, but subclasses
   * may override if cleanup work is necessary.
   */
  public void dispose() {}

}

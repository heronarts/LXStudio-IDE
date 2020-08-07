package heronarts.lx.output;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;

/**
 * A buffer-based LXOutput that can be used to send Serial messages.
 *
 * The serial message equivalent to {@link heronarts.lx.output.LXDatagram}
 */
public abstract class SerialMessage extends LXBufferOutput {
  protected static class ErrorState {
    // Destination address
    final String destination;

    // Number of failures sending to this message address
    int failureCount = 0;

    // Timestamp to re-try sending to this address again after
    long sendAfter = 0;

    private ErrorState(String destination) {
      this.destination = destination;
    }
  }

  private static final Map<String, ErrorState> _messageErrorState =
    new HashMap<String, ErrorState>();

  private static ErrorState getMessageErrorState(SerialMessage message) {
    String destination = message.definition.portName + ":" + message.definition.baudRate;
    ErrorState messageErrorState = _messageErrorState.get(destination);
    if (messageErrorState == null) {
      _messageErrorState.put(destination, messageErrorState = new ErrorState(destination));
    }
    return messageErrorState;
  }

  protected final byte[] buffer;

  ErrorState errorState = null;

  protected SerialDefinition definition;

  /**
   * Whether this message is in an error state
   */
  public final BooleanParameter error =
    new BooleanParameter("Error", false)
    .setDescription("Whether there have been errors sending to this message address");

  protected SerialMessage(LX lx, int[] indexBuffer, int size) {
    this(lx, indexBuffer, ByteOrder.RGB, size);
  }

  protected SerialMessage(LX lx, int[] indexBuffer, ByteOrder byteOrder, int size) {
    super(lx, indexBuffer, byteOrder);

    this.buffer = new byte[size];
    for (int i = 0; i < size; ++i) {
      this.buffer[i] = 0;
    }
  }

  /**
   * Size of the end section of the buffer after colour data is written (e.g. checksum)
   *
   * @return Offset into raw byte buffer for color data
   */
  protected abstract int getFooterIndex();

  /**
   * Check that the data size on this thing is valid
   */
  protected void validateBufferSize() {
    int dataSize = getFooterIndex() - getDataBufferOffset();
    if (dataSize < this.indexBuffer.length * this.byteOrder.getNumBytes()) {
      String cls = getClass().getSimpleName();
      throw new IllegalArgumentException(cls + " dataSize " + dataSize + " is insufficient for indexBuffer of length " + this.indexBuffer.length + " with ByteOrder " + this.byteOrder.toString());
    }
  }

  protected ErrorState getErrorState() {
    if (this.errorState != null) {
      return this.errorState;
    }
    return this.errorState = getMessageErrorState(this);
  }

  /**
   * Sets the destination serial configuration of this message
   *
   * @param definition Destination serial configuration
   * @return this
   */
  public SerialMessage setDefinition(SerialDefinition definition) {
    this.definition = definition;
    return this;
  }

  /**
   * Gets the serial configuration this message sends to
   *
   * @return Destination serial configuration
   */
  public SerialDefinition getDefinition() {
    return this.definition;
  }

  @Override
  public byte[] getDataBuffer() {
    return this.buffer;
  }

  @Override
  protected void onSend(int[] colors, byte[] glut) {
    if (!enabled.isOn()) {
      return;
    }

    // Check for error state on this datagram's output
    ErrorState errorState = getErrorState();
    if (errorState.sendAfter >= this.lx.engine.nowMillis) {
      // This datagram can't be sent now... mark its error state
      this.error.setValue(true);
      return;
    }

    // Update the data buffer and sequence number
    updateDataBuffer(colors, glut);

    // TODO(Dev): Implement reconnect

    try {
      definition.write(buffer);
      // TODO(Dev): Implement sendAll properly
      // if (PBExpanderMessage.class.isInstance(message)
      //   && !this.pbDefsSent.contains(definition)) {
      //   this.pbDefsSent.add(definition);
      // }

      if (errorState.failureCount > 0) {
        LXOutput.log("Recovered connectivity to " + errorState.destination);
      }
      // Sent fine! All good here...
      errorState.failureCount = 0;
      errorState.sendAfter = 0;
      this.error.setValue(false);
    } catch (IOException iox) {
      error.setValue(true);

      if (errorState.failureCount == 0) {
        LXOutput.error("IOException sending to "
            + errorState.destination + " (" + iox.getLocalizedMessage()
            + "), will initiate backoff after 3 consecutive failures");
      }
      ++errorState.failureCount;
      if (errorState.failureCount >= 3) {
        int pow = Math.min(5, errorState.failureCount - 3);
        long waitFor = (long) (50 * Math.pow(2, pow));
        LXOutput.error("Retrying " + errorState.destination
            + " in " + waitFor + "ms" + " (" + errorState.failureCount
            + " consecutive failures)");
        errorState.sendAfter = this.lx.engine.nowMillis + waitFor;
      }
    }
  }

  /**
   * Invoked when the message is no longer needed. Typically a no-op, but subclasses
   * may override if cleanup work is necessary.
   */
  public void dispose() {}

}

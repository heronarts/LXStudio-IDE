package flavius.pixelblaze.output;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutput;

/**
 * An output stage that functions by sending serial packets, similar to how
 * {@link heronarts.lx.output.LXDatagramOutput} sends {@link heronarts.lx.output.LXDatagram}
 */
public class SerialPacketOutput extends LXOutput {

  protected final List<SerialPacket> packets = new ArrayList<SerialPacket>();

  private final SimpleDateFormat date = new SimpleDateFormat("[HH:mm:ss]");

  private static final Logger logger = Logger
  .getLogger(SerialPacketOutput.class.getName());

  public SerialPacketOutput(LX lx) {
    super(lx);
  }

  public SerialPacketOutput addPacket(SerialPacket packet) {
    logger.info("");
    Objects.requireNonNull(packet, "May not add null packet to SerialPacketOutput");
    if (this.packets.contains(packet)) {
      throw new IllegalStateException("May not add duplicate packet to SerialPacketOutput: " + packet);
    }
    this.packets.add(packet);
    return this;
  }

  public SerialPacketOutput addPackets(SerialPacket[] packets) {
    for (SerialPacket packet : packets) {
      addPacket(packet);
    }
    return this;
  }

  public SerialPacketOutput addPackets(List<SerialPacket> packets) {
    for (SerialPacket packet : packets) {
      addPacket(packet);
    }
    return this;
  }

  /**
   * Sets the destination serial configuration of all packets on this output
   *
   * @param definition Destination serial configuration definition
   * @return this
   */
  public SerialPacketOutput setDefinition(SerialDefinition definition) {
    for (SerialPacket packet : this.packets) {
      packet.setDefinition(definition);
    }
    return this;
  }

  /**
   * Subclasses may override. Invoked before packets are sent.
   *
   * @param colors Color values
   */
  protected /* abstract */ void beforeSend(int[] colors) {}

  /**
   * Subclasses may override. Invoked after packets are sent.
   *
   * @param colors Color values
   */
  protected /* abstract */ void afterSend(int[] colors) {}

  /**
   * Core method which sends the packets.
   */
  @Override
  protected void onSend(int[] colors, double brightness) {
    long now = System.currentTimeMillis();
    beforeSend(colors);
    for (SerialPacket packet : this.packets) {
      onSendPacket(packet, now, colors, brightness);
    }
    afterSend(colors);
    // logger.info("");
  }

  protected void onSendPacket(SerialPacket packet, long nowMillis, int[] colors, double brightness) {
    if (!packet.enabled.isOn()) {
      return;
    }

    SerialPacket.ErrorState packetErrorState = packet.getErrorState();
    if (packetErrorState.sendAfter >= nowMillis) {
      // This packet can't be sent now... mark its error state
      packet.error.setValue(true);
      return;
    }

    byte[] glut = this.gammaLut[(int) Math.round(brightness * packet.brightness.getValue() * 255.f)];
    packet.onSend(colors, glut);
    try {
      packet.definition.write(packet.buffer);
      if (packetErrorState.failureCount > 0) {
        LXOutput.log(this.date.format(nowMillis) + " Recovered connectivity to " + packetErrorState.destination);
      }
      // Sent fine! All good here...
      packetErrorState.failureCount = 0;
      packetErrorState.sendAfter = 0;
      packet.error.setValue(false);
    } catch (IOException iox) {
      packet.error.setValue(true);
      if (packetErrorState.failureCount == 0) {
        LXOutput.error(this.date.format(nowMillis) + " IOException sending to "
            + packetErrorState.destination + " (" + iox.getLocalizedMessage()
            + "), will initiate backoff after 3 consecutive failures");
      }
      ++packetErrorState.failureCount;
      if (packetErrorState.failureCount >= 3) {
        int pow = Math.min(5, packetErrorState.failureCount - 3);
        long waitFor = (long) (50 * Math.pow(2, pow));
        LXOutput.error(this.date.format(nowMillis) + " Retrying " + packetErrorState.destination
            + " in " + waitFor + "ms" + " (" + packetErrorState.failureCount
            + " consecutive failures)");
        packetErrorState.sendAfter = nowMillis + waitFor;

      }
    }
  }

  @Override
  protected void onSend(int[] colors, byte[] glut) {
    throw new UnsupportedOperationException("SerialPacketOutput does not implement onSend by glut");
  }
}

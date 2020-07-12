package flavius.pixelblaze;

import flavius.pixelblaze.util.PBConstants;

// import java.util.logging.Logger;

/**
 * A representation of the header section of a pixelblaze message.
 *
 * Headers are static for the same recordType and channel.
 *
 * @author <a href="https://dev.laserphile.com/">Derwent McElhinney</a>
 */
public class PBHeader implements PBConstants {
  // private static final Logger logger =
  // Logger.getLogger(PBHeader.class.getName());
  public int channel;
  public PBRecordType recordType;

  public PBHeader(final int channel, final PBRecordType recordType) {
    this.channel = channel;
    this.recordType = recordType;
  }

  public byte[] toBytes() {
    final byte[] message = new byte[HEADER_SIZE];
    int i = 0;
    // logger.fine(String.format("MAGIC: %s\n", MAGIC));
    for (char c : MAGIC.toCharArray()) {
      message[i++] = (byte) c;
    }
    // logger.fine(String.format("channel: %d\n", this.channel));
    message[i++] = (byte) this.channel;
    // logger.fine(String.format(
    // "recordtype: %d (%s)\n", this.recordType.value, this.recordType.name()));
    message[i++] = (byte) this.recordType.value;

    return message;
  }
}

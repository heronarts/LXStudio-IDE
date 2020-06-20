package flavius.pixelblaze;

import flavius.pixelblaze.util.ByteUtils;
// import java.util.logging.Logger;

/**
 * PBMessageFactoryAPA102Clock
 */
public class PBMessageFactoryAPA102Clock extends PBMessageFactory {
  // private static final Logger logger = Logger.getLogger(
  // PBMessageFactoryAPA102Clock.class.getName());
  public final long freq;

  public PBMessageFactoryAPA102Clock(int channel, long freq) {
    super(PBRecordType.SET_CHANNEL_APA102_CLOCK, channel, new int[] {}, 4, 0);
    this.freq = freq;
  }

  @Override
  protected int writeBody(byte[] message, int offset, int[] indexBuffer,
    int[] colors) {
    int i = offset;
    // logger.fine(String.format("freq: 0x%08x (%dd)\n", this.freq, this.freq));
    for (byte b : ByteUtils.uint32LEBytes(this.freq)) {
      message[i++] = b;
    }
    return (i - offset);
  }
}

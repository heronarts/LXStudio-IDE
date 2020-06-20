package flavius.pixelblaze;

import flavius.pixelblaze.util.ByteUtils;

/**
 * A message factory for setting the channel to be an APA102 clock at a given frequency
 *
 * @author <a href="https://dev.laserphile.com/">Derwent McElhinney</a>
 */
public class PBMessageFactoryAPA102Clock extends PBMessageFactory {
  public final long freq;

  public PBMessageFactoryAPA102Clock(int channel, long freq) {
    super(PBRecordType.SET_CHANNEL_APA102_CLOCK, channel, new int[] {}, 4, 0);
    this.freq = freq;
  }

  @Override
  protected int writeBody(byte[] message, int offset, int[] colors, byte[] glut) {
    int i = offset;
    for (byte b : ByteUtils.uint32LEBytes(this.freq)) {
      message[i++] = b;
    }
    return (i - offset);
  }
}

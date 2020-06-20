package flavius.pixelblaze;

import flavius.pixelblaze.util.ByteUtils;
// import java.util.logging.Logger;

/**
 * PBMessageFactoryAPA102Data
 */
public class PBMessageFactoryAPA102Data extends PBMessageFactoryData {
  // private static final Logger logger = Logger.getLogger(
  // PBMessageFactoryAPA102Data.class.getName());
  public final long freq;
  public byte brightness = 0x1f;

  public PBMessageFactoryAPA102Data(final PBColorOrder colorOrder, int channel,
    int[] indexBuffer, long freq) {
    super(PBRecordType.SET_CHANNEL_APA102_DATA, channel, indexBuffer, 8,
      colorOrder);
    this.freq = freq;
  }

  @Override
  protected int writeBody(byte[] message, int offset, int[] indexBuffer,
    int[] colors) {
    this.validate(indexBuffer);
    int i = offset;
    // logger.fine(String.format("freq: 0x%08x (%dd)\n", this.freq, this.freq));
    for (byte b : ByteUtils.uint32LEBytes(this.freq)) {
      message[i++] = b;
    }
    // logger.fine(String.format("colorOrders: 0x%02x \n",
    // this.colorOrder.colorOrder));
    message[i++] = (byte) this.colorOrder.colorOrder;
    // logger.fine(String.format("struct padding: 0x00 \n"));
    message[i++] = 0x00;
    // logger.fine(String.format(
    // "pixels: 0x%04x (%dd)\n", indexBuffer.length, indexBuffer.length));
    for (byte b : ByteUtils.uint16LEBytes(indexBuffer.length)) {
      message[i++] = b;
    }
    // logger.fine(String.format("global brightness: 0x%02x\n", brightness));
    for (int colorIdx : indexBuffer) {
      int c = 0;
      for (byte b : this.colorOrder.colorBytes(colors[colorIdx])) {
        message[i++] = b;
        // overwrite the 4th byte with the brightness register
        if (c == 3)
          message[i - 1] = this.brightness;
        c++;
      }
    }
    return (i - offset);
  }

  @Override
  public int bufferSpace(int[] indexBuffer) {
    return 4 * (indexBuffer.length + 2);
  }

  @Override
  public void validate(int[] indexBuffer) throws RuntimeException {
    if (this.bufferSpace(indexBuffer) > bytesPerChannel) {
      throw new RuntimeException(
        "too many pixels for a single channel! indexBuffer.length="
          + String.valueOf(indexBuffer.length) + "; colorSize="
          + String.valueOf(this.colorSize) + "; bytesPerChannel="
          + String.valueOf(bytesPerChannel));
    }
    if (this.colorOrder.numElements != 4) {
      throw new RuntimeException(
        "APA102-Type leds must have a colorOrder of length 4, e.g. RGBV. Instead found"
          + String.valueOf(this.colorOrder.numElements) + ", "
          + this.colorOrder.name());
    }
  }
}

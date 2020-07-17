package flavius.pixelblaze.output;

import java.util.logging.Logger;

import flavius.pixelblaze.PBColorOrder;
import flavius.pixelblaze.PBRecordType;
import flavius.pixelblaze.util.ByteUtils;

public class PBExpanderDataPacket extends PBExpanderPacket {
  public final PBColorOrder order;
  public final int[] indexBuffer;

  private static final Logger logger = Logger
  .getLogger(PBExpanderDataPacket.class.getName());

  /**
   * Calculate the number of bytes required to store these colors on the
   * PixelBlaze buffer.
   *
   * @return size
   */
  public static int bufferSpace(PBRecordType type, PBColorOrder order, int[] indexBuffer) {
    switch(type) {
      case SET_CHANNEL_WS2812: return order.numElements * indexBuffer.length;
      case SET_CHANNEL_APA102_DATA: return 4 * (indexBuffer.length + 2);
      default: return 0;
    }
  }

  /**
   * Calculate the number of bytes that the body section of this packet occupies
   *
   * @return size
   */
  public static int bodySize(PBRecordType type, PBColorOrder order, int[] indexBuffer) {
    switch(type) {
      case SET_CHANNEL_APA102_DATA:
      case SET_CHANNEL_WS2812: return baseSize(type) + order.numElements * indexBuffer.length;
      default: return baseSize(type);
    }
  }

  public PBExpanderDataPacket(PBRecordType type, PBColorOrder order, int[] indexBuffer, int channel) {
    super(HEADER_SIZE + bodySize(type, order, indexBuffer) + CRC_SIZE, type, channel);
    this.order = order;
    this.indexBuffer = indexBuffer;
  }

  @Override
  protected void writeBody(int[] colors, byte[] glut) {
    this.validate(this.indexBuffer);
    int i = HEADER_SIZE;
    buffer[i++] = (byte) this.order.numElements;
    buffer[i++] = (byte) this.order.colorOrder;
    for (byte b : ByteUtils.uint16LEBytes(this.indexBuffer.length)) {
      buffer[i++] = b;
    }
    for (int colorIdx : indexBuffer) {
      for (byte b : this.order.colorBytes(colors[colorIdx], glut)) {
        buffer[i++] = b;
      }
    }
  }

  public void validate(int[] indexBuffer) throws RuntimeException {
    if (bufferSpace(type, order, indexBuffer) > MAX_CHANNEL_BYTES) {
      throw new RuntimeException(String.format(
        "too many pixels for a single channel! indexBuffer.length=%d; order.size=%d; max_bytes=%d",
        indexBuffer.length, this.order.numElements, MAX_CHANNEL_BYTES));
    }
    if(type == PBRecordType.SET_CHANNEL_APA102_DATA && order.numElements != 4) {
      throw new RuntimeException(String.format(
        "APA102-Type leds must have a colorOrder of length 4, e.g. RGBV. Instead found %d, %s",
        order.numElements, order.name()));
    }
  }

}

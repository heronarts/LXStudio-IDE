package flavius.pixelblaze.output;

import flavius.pixelblaze.PBColorOrder;
import flavius.pixelblaze.PBRecordType;
import flavius.pixelblaze.util.ByteUtils;
import heronarts.lx.LX;
import heronarts.lx.output.LXBufferOutput;

public class PBExpanderDataMessage extends PBExpanderMessage {
  public final PBColorOrder order;

  /**
   * Calculate the number of bytes that the body section of this message occupies
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

  public PBExpanderDataMessage(LX lx, int[] indexBuffer, PBRecordType type, PBColorOrder order, int channel) {
    // TODO(Dev): Convert PBColorOrder to lx.output.LXBuffer.ByteOrder
    super(lx, indexBuffer, HEADER_SIZE + bodySize(type, order, indexBuffer) + CRC_SIZE, type, channel);
    this.order = order;
    validateBufferSize();
    String cls = getClass().getSimpleName();
    if (order != PBColorOrder.RGB) {
      throw new IllegalArgumentException(String.format(
          "%s number of pixels extends max for single PixelBlaze channel! indexBuffer.length=%d; "
          + "byteOrder=%d; MAX_CHANNEL_BYTES=%d", cls, indexBuffer.length,
        byteOrder.toString(), MAX_CHANNEL_BYTES));
    }
    if(type == PBRecordType.SET_CHANNEL_APA102_DATA && byteOrder.getNumBytes() != 4) {
      throw new RuntimeException(String.format(
        "APA102-Type leds must have a colorOrder of length 4, e.g. RGBV. Instead found %d, %s",
        order.numElements, byteOrder.toString()));
    }
    writeHeader();
  }

  @Override
  protected int getDataBufferOffset() {
    return HEADER_SIZE + baseSize(type);
  }

  @Override
  protected LXBufferOutput updateDataBuffer(int[] colors, byte[] glut) {
    int i = HEADER_SIZE;
    buffer[i++] = (byte) this.order.numElements;
    buffer[i++] = (byte) this.order.colorOrder;
    for (byte b : ByteUtils.uint16LEBytes(this.indexBuffer.length)) {
      buffer[i++] = b;
    }
    return super.updateDataBuffer(colors, glut);
  }
}

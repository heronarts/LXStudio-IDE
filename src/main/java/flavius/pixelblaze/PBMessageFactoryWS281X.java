package flavius.pixelblaze;

import flavius.pixelblaze.util.ByteUtils;
// import java.util.logging.Logger;

/**
 * A factory for generating the WS281X data messages
 *
 * @author <a href="https://dev.laserphile.com/">Derwent McElhinney</a>
 */
public class PBMessageFactoryWS281X extends PBMessageFactoryData {
  // private static final Logger logger =
  // Logger.getLogger(PBMessageFactoryWS281X.class.getName());
  public PBMessageFactoryWS281X(final PBColorOrder colorOrder, int channel,
    int[] indexBuffer) {
    super(PBRecordType.SET_CHANNEL_WS2812, channel, indexBuffer, 4, colorOrder);
  }

  @Override
  protected int writeBody(byte[] message, int offset, int[] colors, byte[] glut) {
    this.validate(this.indexBuffer);
    int i = offset;
    // logger.fine(String.format(
    // "numElements: 0x%02x\ncolorOrders: 0x%02x\n",
    // this.colorOrder.numElements,
    // this.colorOrder.colorOrder
    // ));
    message[i++] = (byte) this.colorOrder.numElements;
    message[i++] = (byte) this.colorOrder.colorOrder;
    // logger.fine(String.format("pixels: 0x%04x\n", indexBuffer.length));
    for (byte b : ByteUtils.uint16LEBytes(this.indexBuffer.length)) {
      message[i++] = b;
    }
    for (int colorIdx : indexBuffer) {
      for (byte b : this.colorOrder.colorBytes(colors[colorIdx], glut)) {
        message[i++] = b;
      }
    }
    return (i - offset);
  }
}

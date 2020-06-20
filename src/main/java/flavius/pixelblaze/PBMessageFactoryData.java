package flavius.pixelblaze;

// import java.util.logging.Logger;

/**
 * An abstract class for all Data channel message factories (WS2812, APA102_DATA)
 *
 * @author <a href="https://dev.laserphile.com/">Derwent McElhinney</a>
 */
public abstract class PBMessageFactoryData extends PBMessageFactory {
  // private static final Logger logger =
  // Logger.getLogger(PBMessageFactoryData.class.getName());
  public final PBColorOrder colorOrder;
  public static final int bytesPerChannel = 2048;

  public PBMessageFactoryData(PBRecordType recordType, int channel,
    int[] indexBuffer, int baseSize, PBColorOrder colorOrder) {
    super(recordType, channel, indexBuffer, baseSize, colorOrder.numElements);
    this.colorOrder = colorOrder;
    // logger.fine(String.format("colorOrder: %s\n", this.colorOrder.name()));
  }

  /**
   * Calculate the number of bytes required to store these colors on the
   * PixelBlaze buffer.
   *
   * Override in subclasses.
   *
   * @return
   */
  public int bufferSpace(int[] indexBuffer) {
    return this.colorSize * indexBuffer.length;
  }

  public void validate(int[] indexBuffer) throws RuntimeException {
    if (this.bufferSpace(indexBuffer) > bytesPerChannel) {
      throw new RuntimeException(
        "too many pixels for a single channel! indexBuffer.length="
          + String.valueOf(indexBuffer.length) + "; colorSize="
          + String.valueOf(this.colorSize) + "; bytesPerChannel="
          + String.valueOf(bytesPerChannel));
    }
  }
}

package flavius.pixelblaze.output;

import flavius.pixelblaze.PBColorOrder;
import flavius.pixelblaze.PBHeader;
import flavius.pixelblaze.PBRecordType;
import flavius.pixelblaze.util.PBCRC;
import flavius.pixelblaze.util.PBConstants;

public abstract class PBExpanderPacket extends SerialPacket implements PBConstants {
  public final PBRecordType type;
  private final PBCRC crc = new PBCRC();
  public final int channel;

  /**
   * Calculate the number of bytes between the header and CRC when there are zero colors being
   * sent
   *
   * @return
   */
  public static int baseSize(PBRecordType type) {
    switch(type) {
      case SET_CHANNEL_WS2812:
      case SET_CHANNEL_APA102_CLOCK: return 4;
      case SET_CHANNEL_APA102_DATA: return 8;
      default: return 0;
    }
  }

  /**
   * Calculate the number of bytes between the header and CRC when there are zero colors being
   * sent
   *
   * @return
   */
  public static int colorSize(PBRecordType type) {
    switch(type) {
      case SET_CHANNEL_WS2812:
      case SET_CHANNEL_APA102_CLOCK: return 4;
      case SET_CHANNEL_APA102_DATA: return 8;
      default: return 0;
    }
  }

  protected PBExpanderPacket(int size, PBRecordType type) {
    this(size, type, 0);
  }

  protected PBExpanderPacket(int size, PBRecordType type, int channel) {
    super(size);
    this.type = type;
    this.channel = channel;
    writeHeader();
  }

  /**
   * Write the header to this packet's buffer
   */
  private void writeHeader() {
    int i = 0;
    for (byte b : new PBHeader(this.channel, this.type).toBytes()) {
      buffer[i++] = b;
    }
  }

  /**
   * Write the body section of the message into the message buffer at offset
   *
   * @param colors  an array of colours to write
   * @param glut    the gamma lookup table
   * @return the number of bytes written to the buffer
   */
  protected abstract void writeBody(int[] colors, byte[] glut);

  protected int getCRCIndex() {
    return buffer.length - CRC_SIZE;
  }

  private void writeCRC() {
    int i = getCRCIndex();
    for (byte b : this.crc.toBytes())
      buffer[i++] = b;
  }

  @Override
  public void onSend(int[] colors, byte[] glut) {
    writeBody(colors, glut);
    this.crc.updateBytes(buffer, 0, getCRCIndex());
    writeCRC();
  }
}

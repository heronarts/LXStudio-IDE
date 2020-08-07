package flavius.pixelblaze.output;

import flavius.pixelblaze.PBHeader;
import flavius.pixelblaze.PBRecordType;
import flavius.pixelblaze.util.PBCRC;
import flavius.pixelblaze.util.PBConstants;
import heronarts.lx.LX;
import heronarts.lx.output.LXBufferOutput;
import heronarts.lx.output.SerialDefinition;
import heronarts.lx.output.SerialMessage;

public abstract class PBExpanderMessage extends SerialMessage implements PBConstants {
  public final PBRecordType type;
  private final PBCRC crc = new PBCRC();
  public final int channel;

  /**
   * Calculate the number of bytes between the end of the header and the start of the CRC when there
   * are zero colors being sent
   *
   * @return
   */
  public static int baseSize(PBRecordType type) {
    switch(type) {
      case SET_CHANNEL_WS2812:
      case SET_CHANNEL_APA102_CLOCK: return 4;
      case SET_CHANNEL_APA102_DATA: return 8;
      case DRAW_ALL:
      default: return 0;
    }
  }

  protected PBExpanderMessage(LX lx, int[] indexBuffer, int size, PBRecordType type) {
    this(lx, indexBuffer, size, type, 0);
  }

  protected PBExpanderMessage(LX lx, int[] indexBuffer, int size, PBRecordType type, int channel) {
    this(lx, indexBuffer, ByteOrder.RGB, size, type, channel);
  }

  protected PBExpanderMessage(LX lx, int[] indexBuffer, ByteOrder byteOrder, int size, PBRecordType type, int channel) {
    super(lx, indexBuffer, size);
    this.type = type;
    this.channel = channel;

    String cls = getClass().getSimpleName();
    if (size < HEADER_SIZE + CRC_SIZE) {
      throw new IllegalArgumentException(String.format(
          "%s Buffer size should be larger than HEADER_SIZE (%d) + CRC_SIZE(%d)!", cls, HEADER_SIZE,
          CRC_SIZE));
    }
    // TODO(Dev): Calculate channel_bytes correctly
    int channel_bytes = size - HEADER_SIZE - CRC_SIZE - baseSize(type);
    if (channel_bytes > MAX_CHANNEL_BYTES) {
      throw new IllegalArgumentException(String.format(
          "buffer size exceeds PixelBlaze maximum! channel_bytes=%d; MAX_CHANNEL_BYTES=%d", cls,
          channel_bytes, MAX_CHANNEL_BYTES));
    }

    if( type != PBRecordType.DRAW_ALL) {
      this.sendAll = new PBExpanderDrawAllMessage(lx);
    }
  }

  @Override
  public SerialMessage setDefinition(SerialDefinition definition) {
    super.setDefinition(definition);
    if( type != PBRecordType.DRAW_ALL) {
      sendAll.setDefinition(definition);
    }
    return this;
  }

  /**
   * Write the header to this message's buffer
   */
  protected void writeHeader() {
    int i = 0;
    for (byte b : new PBHeader(this.channel, this.type).toBytes()) {
      buffer[i++] = b;
    }
  }

  @Override
  protected LXBufferOutput updateDataBuffer(int[] colors, byte[] glut) {
    super.updateDataBuffer(colors, glut);
    // Update CRC and write to footer
    crc.reset();
    crc.updateBytes(buffer, 0, getFooterIndex());
    byte[] buffer = getDataBuffer();
    int footerOffset = getFooterIndex();
    for (byte b : this.crc.toBytes())
      buffer[footerOffset++] = b;
    return this;
  }

  @Override
  protected int getFooterIndex() {
    return buffer.length - CRC_SIZE;
  }

  private PBExpanderDrawAllMessage sendAll;

  @Override
  protected void onSend(int[] colors, byte[] glut) {
    super.onSend(colors, glut);
    if( type != PBRecordType.DRAW_ALL) {
      sendAll.onSend(colors, glut);
    }
  }
}

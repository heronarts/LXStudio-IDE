package flavius.pixelblaze;

import flavius.pixelblaze.util.PBCRC;
import flavius.pixelblaze.util.PBConstants;

/**
 * Abstract class which generates messages for the <a href="github.com/simap/pixelblaze_output_expander">
 * Pixelblaze Output Expander</a> serial protocol for various types of records.
 *
 * @author <a href="https://dev.laserphile.com/">Derwent McElhinney</a>
 */
public abstract class PBMessageFactory implements PBConstants {
  private final PBRecordType recordType;
  private final int channel;
  private final PBCRC crc = new PBCRC();

  /**
   * An array of pixel indices into the global lx model that we care about
   */
  public final int[] indexBuffer;

  /**
   * Number of bytes between the header and CRC when there are zero colors being
   * sent.
   */
  public final int baseSize;

  /**
   * Number of bytes occupied by a single color
   */
  public final int colorSize;

  /**
   * Creates a PBMessageFactory for the given recordType and channel
   *
   * @param recordType  what type of messages to generate
   * @param channel     the channel number on the pixelblaze
   * @param indexBuffer an array of pixel indices that these messages will care about
   * @param baseSize    Number of bytes between header and CRC when there are
   *                      zero colors being sent for this specific message.
   * @param colorSize   Number of bytes occupied by a single color
   */
  public PBMessageFactory(PBRecordType recordType, int channel,
    int[] indexBuffer, int baseSize, int colorSize) {
    this.recordType = recordType;
    this.channel = channel;
    this.indexBuffer = indexBuffer;
    this.baseSize = baseSize;
    this.colorSize = colorSize;
  }

  /**
   * Creates a PBMessageFactory for the given recordType for a recordType that
   * doesn't care about colours
   *
   * @param recordType what type of messages to generate
   */
  public PBMessageFactory(PBRecordType recordType) {
    this(recordType, 0, new int[] {}, 0, 0);
  }

  /**
   * Write the header to the message buffer at offset
   *
   * @param message the message buffer
   * @param offset  the current offset into the buffer
   * @return the number of bytes written
   */
  public int writeHeader(byte[] message, int offset) {
    int i = offset;
    for (byte b : new PBHeader(this.channel, this.recordType).toBytes()) {
      message[i++] = b;
    }
    return (i - offset);
  }

  /**
   * Get the size of the body section of the message
   *
   * @param indexBuffer an array of pixel indices that the message will
   * @return the number of bytes that the body will occupy
   */
  public int getBodySize() {
    return baseSize + (indexBuffer.length * colorSize);
  }

  /**
   * Write the body section of the message into the message buffer at offset
   *
   * @param message the message buffer
   * @param offset  the current offset into the message buffer
   * @param colors  an array of colours to write
   * @param glut    the gamma lookup table
   * @return the number of bytes written to the buffer
   */
  protected int writeBody(byte[] message, int offset, int[] colors, byte[] glut) {
    return 0;
  }

  /**
   * Write the CRC section of the message into the message buffer at offset
   * @param message the message buffer
   * @param offset  the current offset into the message buffer
   * @return the number of bytes written to the buffer
   */
  protected int writeCRC(byte[] message, int offset) {
    int i = offset;
    for (byte b : this.crc.toBytes())
      message[i++] = b;
    return (i - offset);
  }

  /**
   * @return the number of bytes that the entire message will occupy
   */
  public int getMessageSize() {
    return PBHeader.size + this.getBodySize() + CRC_SIZE;
  }

  /**
   * Get the message to write the colours to the device
   *
   * @param colors an array of colours to send
   * @param glut   the gamma lookup table
   * @return the entire message including header, body and CRC
   */
  public byte[] getMessage(int[] colors, byte[] glut) {
    this.crc.reset();
    final byte[] message = new byte[this.getMessageSize()];
    int i = 0;
    i += this.writeHeader(message, i);
    i += this.writeBody(message, i, colors, glut);
    this.crc.updateBytes(message, 0, i);
    i += this.writeCRC(message, i);
    return message;
  }

  /**
   * @return the entire message including header, body and CRC
   */
  public byte[] getMessage() {
    return getMessage(new int[] {}, new byte[] {});
  }
}

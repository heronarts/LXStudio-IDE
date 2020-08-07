package flavius.pixelblaze.output;

import flavius.pixelblaze.PBRecordType;
import heronarts.lx.LX;

public class PBExpanderDrawAllMessage extends PBExpanderMessage {
  public PBExpanderDrawAllMessage(LX lx) {
    super(lx, new int[]{}, HEADER_SIZE + CRC_SIZE, PBRecordType.DRAW_ALL, 0);
    validateBufferSize();
    writeHeader();
  }

  @Override
  protected int getDataBufferOffset() {
    return HEADER_SIZE;
  }
}

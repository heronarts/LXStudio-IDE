package flavius.pixelblaze.output;

import flavius.pixelblaze.PBRecordType;

public class PBExpanderDrawAllPacket extends PBExpanderPacket {
  public PBExpanderDrawAllPacket() {
    super(HEADER_SIZE + CRC_SIZE, PBRecordType.DRAW_ALL, 0);
  }
}

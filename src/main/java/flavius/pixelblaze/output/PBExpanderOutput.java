package flavius.pixelblaze.output;

import flavius.pixelblaze.PBColorOrder;
import flavius.pixelblaze.PBMessageFactory;
import flavius.pixelblaze.PBMessageFactoryAPA102Clock;
import flavius.pixelblaze.PBMessageFactoryAPA102Data;
import flavius.pixelblaze.PBMessageFactoryDrawAll;
import flavius.pixelblaze.PBMessageFactoryWS281X;
import flavius.pixelblaze.util.PBConstants;
import heronarts.lx.LX;
import java.util.ArrayList;
import java.util.List;
import processing.serial.Serial;

public class PBExpanderOutput extends SerialOutput implements PBConstants {
  public final List<PBMessageFactory> messageFactories;

  public PBExpanderOutput(LX lx, Serial serialPort) {
    super(lx, serialPort);
    this.messageFactories = new ArrayList<PBMessageFactory>();
    this.messageFactories.add(new PBMessageFactoryDrawAll());
  }

  @Override
  protected void onSend(int[] colors, byte[] glut) {
    for (PBMessageFactory messageFactory : this.messageFactories) {
      this.write(messageFactory.getMessage(colors, glut));
    }
  }

  public void addWS281XChannel(int channelNumber, int[] indexBuffer) {
    this.messageFactories.add(0,
      new PBMessageFactoryWS281X(PBColorOrder.RGB, channelNumber, indexBuffer));
  }

  public void addAPA102DataChannel(int channelNumber, int[] indexBuffer,
    long freq) {
    this.messageFactories.add(0, new PBMessageFactoryAPA102Data(
      PBColorOrder.RGBV, channelNumber, indexBuffer, freq));
  }

  public void addAPA102ClockChannel(int channelNumber, long freq) {
    this.messageFactories.add(0,
      new PBMessageFactoryAPA102Clock(channelNumber, freq));
  }
}

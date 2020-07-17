package flavius.pixelblaze.output;

import java.util.ArrayList;
import java.util.List;

import flavius.pixelblaze.PBColorOrder;
import flavius.pixelblaze.PBMessageFactory;
import flavius.pixelblaze.PBMessageFactoryAPA102Clock;
import flavius.pixelblaze.PBMessageFactoryAPA102Data;
import flavius.pixelblaze.PBMessageFactoryDrawAll;
import flavius.pixelblaze.PBMessageFactoryWS281X;
import flavius.pixelblaze.util.PBConstants;
import heronarts.lx.LX;
import heronarts.lx.output.SerialOutput;
import processing.serial.Serial;

/**
 * Extends {@link SerialOutput} to drive a <a href="github.com/simap/pixelblaze_output_expander">
 * Pixelblaze Output Expander</a> through a serial port.
 *
 * <p><strong>Example: create a {@code WS281X} output for all points on channel 1</strong><pre>
 *    LXModel model = lx.getModel();
 *    Serial serialPort = new Serial(this, SERIAL_PORT, PBExpanderOutput.BAUD_RATE);
 *    int[] indexBuffer = new int[model.size];
 *    for (int i = 0; i &lt; model.size; i++) indexBuffer[i] = i;
 *    PBExpanderOutput output = new PBExpanderOutput(lx, serialPort);
 *    output.addWS281XChannel(1, indexBuffer);
 *    lx.addOutput(output);
 * </pre>
 *
 * <p><strong>Example: create an {@code APA102} output for all points on channel 1, using Channel 7
 * as clock</strong><pre>
 *    LXModel model = lx.getModel();
 *    Serial serialPort = new Serial(this, SERIAL_PORT, PBExpanderOutput.BAUD_RATE);
 *    int[] indexBuffer = new int[model.size];
 *    for (int i = 0; i &lt; model.size; i++) indexBuffer[i] = i;
 *    PBExpanderOutput output = new PBExpanderOutput(lx, serialPort);
 *    output.addAPA102DataChannel(1, indexBuffer);
 *    output.addAPA102ClockChannel(7, 800000);
 *    lx.addOutput(output);
 * </pre>
 *
 *
 * @author <a href="https://dev.laserphile.com/">Derwent McElhinney</a>
 * @see heronarts.lx.output.LXOutput
 * @see flavius.pixelblaze.PBMessageFactory
 * @see SerialOutput
 */
public class PBExpanderOutput extends SerialOutput implements PBConstants {
  public final List<PBMessageFactory> messageFactories;

  /**
   * Constructs a {@code PBExpanderOutput} which writes to the device connected
   * to {@code serialPort}
   *
   * @param lx         the {@link heronarts.lx.LX} instance hosting this output
   * @param serialPort the <a href=
   *     "http://processing.github.io/processing-javadocs/libraries/processing/serial/Serial.html">
   *     {@code processing.serial.Serial}</a> object which is
   *     connected to the <a href="github.com/simap/pixelblaze_output_expander">Pixelblaze Output
   *     Expander</a>
   */
  public PBExpanderOutput(LX lx, Serial serialPort) {
    super(lx, serialPort, BAUD_RATE);
    this.messageFactories = new ArrayList<PBMessageFactory>();
    this.messageFactories.add(new PBMessageFactoryDrawAll());
  }

  @Override
  protected void onSend(int[] colors, byte[] glut) {
    for (PBMessageFactory messageFactory : this.messageFactories) {
      this.write(messageFactory.getMessage(colors, glut));
    }
  }

  /**
   * Adds a WS281X Data Channel to the output
   *
   * @param channelNumber the Pixelblaze channel number
   * @param indexBuffer   the pixel indices that this channel handles
   */
  public void addWS281XChannel(int channelNumber, int[] indexBuffer) {
    this.messageFactories.add(0,
      new PBMessageFactoryWS281X(PBColorOrder.RGB, channelNumber, indexBuffer));
  }

  /**
   * Adds an APA102 Data Channel to the output
   *
   * @param channelNumber the Pixelblaze channel number
   * @param indexBuffer   the pixel indices that this channel handles
   * @param freq          the clock frequency of this output
   */
  public void addAPA102DataChannel(int channelNumber, int[] indexBuffer,
    long freq) {
    this.messageFactories.add(0, new PBMessageFactoryAPA102Data(
      PBColorOrder.RGBV, channelNumber, indexBuffer, freq));
  }

  /**
   * Adds an APA102 Clock Channel to the output
   *
   * @param channelNumber the Pixelblaze channel number
   * @param freq          the clock frequency of this output
   */
  public void addAPA102ClockChannel(int channelNumber, long freq) {
    this.messageFactories.add(0,
      new PBMessageFactoryAPA102Clock(channelNumber, freq));
  }
}

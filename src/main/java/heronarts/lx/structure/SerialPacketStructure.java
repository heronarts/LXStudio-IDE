package heronarts.lx.structure;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.SerialPacket;
import heronarts.lx.output.SerialPacketOutput;

/**
 * Serial equivalent of {@link heronarts.lx.structure.LXStructure} which is used to create a
 * {@link SerialPacketOutput}
 */
public class SerialPacketStructure extends ShadowStructure {

  public class SerialOutput extends SerialPacketOutput {

    public SerialOutput(LX lx) {
      super(lx);
    }

    @Override
    protected void onSend(int[] colors, double brightness) {
      long now = System.currentTimeMillis();
      beforeSend(colors);
      for (LXFixture fixture : fixtures) {
        onSendFixture(colors, now, brightness, fixture);
      }
      afterSend(colors);
    }

    private void onSendFixture(int[] colors, long now, double brightness,
    LXFixture fixture) {
      // Check enabled state of fixture
      if (fixture.enabled.isOn()) {
        // Adjust by fixture brightness
        brightness *= fixture.brightness.getValue();

        // Recursively send all the fixture's children
        for (LXFixture child : fixture.children) {
          onSendFixture(colors, now, brightness, child);
        }

        // Then send the fixture's own direct packets
        if(!SerialProtocolFixture.class.isInstance(fixture)){
          return;
        }
        for (SerialPacket packet : ((SerialProtocolFixture)fixture).packets) {
          onSendPacket(packet, now, colors, brightness);
        }
      }
    }
  }

  public final SerialOutput serialOutput;

  public SerialPacketStructure(LX lx) {
    this(lx, null);
  }

  public SerialPacketStructure(LX lx, LXModel immutable) {
    super(lx, immutable);
    this.serialOutput = new SerialOutput(lx);
  }
}

package heronarts.lx.structure;

import java.util.List;
import java.util.logging.Logger;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.output.SerialPacket;
import heronarts.lx.output.SerialPacketOutput;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Serial equivalent of {@link heronarts.lx.structure.LXStructure} which does
 * serial packets as well as datagrams
 */
public class SerialPacketStructure extends LXStructure implements LXStructure.Listener {

  private static final Logger logger = Logger
    .getLogger(SerialPacketStructure.class.getName());

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
    this.lx.structure.addListener(this);
    for(LXFixture fixture: this.lx.structure.fixtures) {
      logger.info(String.format("adding fixture %s", fixture));
      addFixture(fixture);
    }
  }

  @Override
  public void fixtureAdded(LXFixture fixture) {
    List<LXFixture> mutableFixtures = null;
    try {
      mutableFixtures = (List<LXFixture>) (FieldUtils.readField(this,
      "mutableFixtures",
      true));
    } catch (Exception e) {
      logger.warning(e.toString());
      return;
    }
    int index = this.fixtures.size();
    mutableFixtures.add(index, fixture);
  }

  @Override
  public void fixtureRemoved(LXFixture fixture) {
    removeFixture(fixture);
  }

  @Override
  public void fixtureMoved(LXFixture fixture, int index) {
    List<LXFixture> mutableFixtures = null;
    try {
      mutableFixtures = (List<LXFixture>) (FieldUtils.readField(this,
        "mutableFixtures", true));
    } catch (Exception e) {
      logger.warning(e.toString());
      return;
    }
    mutableFixtures.remove(fixture);
  }


}

package heronarts.lx.app.ui;

import java.util.logging.Logger;

import org.apache.commons.lang3.reflect.FieldUtils;

import flavius.ledportal.LPPanelFixture;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.structure.LXFixture;
import heronarts.lx.structure.LXProtocolFixture;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.fixture.UIFixture;
import heronarts.lx.studio.ui.fixture.UIFixtureControls;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDropMenu;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UITheme;

public class UIPanelFixture implements UIFixtureControls<LPPanelFixture> {

  private static final int GRID_CONTROL_WIDTH_SMALL = 51;
  private static final int GRID_CONTROL_WIDTH_MEDIUM = 104;
  private static final int GRID_CONTROL_WIDTH_FULL = 168;
  private static final int GRID_LABEL_WIDTH = 64;
  private static final int GRID_HEIGHT = 16;

  private static final Logger logger = Logger
    .getLogger(UIPanelFixture.class.getName());

  public UI2dComponent[][] buildFixtureSection(UIFixture uiFixture,
    LPPanelFixture fixture) {
    return new UI2dComponent[][] {
      { (UI2dComponent) uiFixture.newParameterLabel("Position",
        (float) GRID_LABEL_WIDTH),
        (UI2dComponent) uiFixture.newControlButton(
          (EnumParameter<?>) fixture.positionMode,
          (float) GRID_CONTROL_WIDTH_MEDIUM) },
      { (UI2dComponent) uiFixture.newParameterLabel("Row Spacing",
        (float) GRID_LABEL_WIDTH),
        (UI2dComponent) uiFixture.newControlBox(fixture.rowSpacing,
          (float) GRID_CONTROL_WIDTH_SMALL) },
      { (UI2dComponent) uiFixture.newParameterLabel("Column Spacing",
        (float) GRID_LABEL_WIDTH),
        (UI2dComponent) uiFixture.newControlBox(fixture.columnSpacing,
          (float) GRID_CONTROL_WIDTH_SMALL) },
      { (UI2dComponent) uiFixture.newParameterLabel("Row Shear",
        (float) GRID_LABEL_WIDTH),
        (UI2dComponent) uiFixture.newControlBox(fixture.rowShear,
          (float) GRID_CONTROL_WIDTH_SMALL) },
      { (UI2dComponent) uiFixture.newParameterLabel("Point Indices (JSON)",
        (float) GRID_CONTROL_WIDTH_FULL) },
      { (UI2dComponent) uiFixture.newControlTextBox(fixture.pointIndicesJSON,
        (float) (GRID_CONTROL_WIDTH_FULL)) } };
  }

  public UI2dComponent[][] buildWiringSection(UIFixture uiFixture,
    LPPanelFixture fixture) {
    return new UI2dComponent[][] {
      { (UI2dComponent) new UIDropMenu(0.0f, 0.0f, uiFixture.getContentWidth(),
        (float)GRID_HEIGHT, (DiscreteParameter) fixture.wiring) },
      { (UI2dComponent) new UIButton(0.0f, 0.0f, 96.0f, (float)GRID_HEIGHT)
        .setParameter(fixture.splitPacket).setLabel("Multi-Packet"),
        (UI2dComponent) uiFixture.newControlIntBox(fixture.pointsPerPacket,
          (float) (int) (uiFixture.getContentWidth() - 98.0f)) } };
  }

  public UI2dComponent[][] buildDatagramProtocolSection(UIFixture uiFixture,
    LPPanelFixture fixture) {
    return buildDatagramProtocolSection(uiFixture, fixture, false);
  }

  public UI2dComponent[][] buildDatagramProtocolSection(UIFixture uiFixture,
    LPPanelFixture fixture, final boolean includeReverseOption) {
    final UITextBox outputHost = new UITextBox(0.0f, 0.0f, 106.0f, (float)GRID_HEIGHT)
      .setParameter(fixture.host);
    final UIIntegerBox outputUniverse = new UIIntegerBox(0.0f, 0.0f, (float) GRID_LABEL_WIDTH,
      (float)GRID_HEIGHT);
    final UILabel outputUniverseLabel = uiFixture.newControlLabel("Universe",
      (float) GRID_LABEL_WIDTH);
    final UIButton reverseButton = includeReverseOption
      ? new UIButton(0.0f, 0.0f, 24.0f, (float)GRID_HEIGHT).setParameter(fixture.reverse)
        .setActiveLabel("\u2190").setInactiveLabel("\u2192")
      : null;
    UI ui = null;
    try {
      ui = (UI) (FieldUtils.readField(uiFixture, "ui", true));
    } catch (Exception e) {
      logger.warning(e.toString());
    }
    if (ui == null) {
      return new UI2dComponent[][] {};
    }
    final UITheme theme = ui.theme;

    fixture.unknownHost.addListener(p -> outputHost
      .setFontColor(fixture.unknownHost.isOn() ? theme.getAttentionColor()
        : theme.getControlTextColor()));
    final LXParameterListener protocolListener = p -> {
      outputHost.setEnabled(
        fixture.protocol.getEnum() != LXFixture.Protocol.NONE);
      switch ((LXFixture.Protocol) fixture.protocol.getEnum()) {
      case ARTNET:
      case SACN: {
        outputUniverse.setParameter(fixture.artNetUniverse).setEnabled(true);
        outputUniverseLabel.setLabel("Universe");
        break;
      }
      case DDP: {
        outputUniverse.setParameter(fixture.ddpDataOffset).setEnabled(true);
        outputUniverseLabel.setLabel("Offset");
        break;
      }
      case OPC: {
        outputUniverse.setParameter(fixture.opcChannel).setEnabled(true);
        outputUniverseLabel.setLabel("Channel");
        break;
      }
      case KINET: {
        outputUniverse.setParameter(fixture.kinetPort).setEnabled(true);
        outputUniverseLabel.setLabel("Port");
        break;
      }
      case NONE: {
        outputUniverse.setParameter((DiscreteParameter) null).setEnabled(false);
        break;
      }
      }
    };
    fixture.protocol.addListener(protocolListener);
    protocolListener.onParameterChanged((LXParameter) fixture.protocol);
    return new UI2dComponent[][] {
      { (UI2dComponent) new UIDropMenu(0.0f, 0.0f,
        (reverseButton == null) ? uiFixture.getContentWidth()
          : (uiFixture.getContentWidth() - 2.0f - reverseButton.getWidth()),
        (float)GRID_HEIGHT, (DiscreteParameter) fixture.protocol),
        (UI2dComponent) reverseButton },
      { (UI2dComponent) outputHost, (UI2dComponent) outputUniverse },
      { (UI2dComponent) uiFixture.newControlLabel("Host", 106.0f),
        (UI2dComponent) outputUniverseLabel } };
  }
  public UI2dComponent[][] buildSerialProtocolSection(UIFixture uiFixture,
    LPPanelFixture fixture) {
    return buildSerialProtocolSection(uiFixture, fixture, false);
  }

  public UI2dComponent[][] buildSerialProtocolSection(UIFixture uiFixture,
    LPPanelFixture fixture, final boolean includeReverseOption) {
    final UITextBox outputSerialPort = new UITextBox(0.0f, 0.0f, 106.0f, (float)GRID_HEIGHT)
      .setParameter(fixture.serialPort);
    final UIIntegerBox outputChannel = new UIIntegerBox(0.0f, 0.0f, (float) GRID_LABEL_WIDTH,
      (float)GRID_HEIGHT);
    final UILabel outputChannelLabel = uiFixture.newControlLabel("Channel",
      (float) GRID_LABEL_WIDTH);
    final UIButton reverseButton = includeReverseOption
      ? new UIButton(0.0f, 0.0f, 24.0f, (float)GRID_HEIGHT).setParameter(fixture.reverse)
        .setActiveLabel("\u2190").setInactiveLabel("\u2192")
      : null;
    UI ui = null;
    try {
      ui = (UI) (FieldUtils.readField(uiFixture, "ui", true));
    } catch (Exception e) {
      logger.warning(e.toString());
    }
    if (ui == null) {
      return new UI2dComponent[][] {};
    }
    final UITheme theme = ui.theme;

    fixture.unknownSerialPort.addListener(p -> outputSerialPort
      .setFontColor(fixture.unknownSerialPort.isOn() ? theme.getAttentionColor()
        : theme.getControlTextColor()));
    final LXParameterListener protocolListener = p -> {
      outputSerialPort.setEnabled(
        fixture.serialProtocol.getEnum() != LPPanelFixture.SerialProtocol.NONE);
      switch ((LPPanelFixture.SerialProtocol) fixture.serialProtocol.getEnum()) {
      case PBX_WS281X:
      case PBX_APA102: {
        outputChannel.setParameter(fixture.pixelBlazeChannel).setEnabled(true);
        outputChannelLabel.setLabel("Channel");
        break;
      }
      case NONE: {
        outputChannel.setParameter((DiscreteParameter) null).setEnabled(false);
        break;
      }
      }
    };
    fixture.serialProtocol.addListener(protocolListener);
    protocolListener.onParameterChanged((LXParameter) fixture.serialProtocol);
    return new UI2dComponent[][] {
      { (UI2dComponent) new UIDropMenu(0.0f, 0.0f,
        (reverseButton == null) ? uiFixture.getContentWidth()
          : (uiFixture.getContentWidth() - 2.0f - reverseButton.getWidth()),
        (float)GRID_HEIGHT, (DiscreteParameter) fixture.serialProtocol),
        (UI2dComponent) reverseButton },
      { (UI2dComponent) outputSerialPort, (UI2dComponent) outputChannel },
      { (UI2dComponent) uiFixture.newControlLabel("Serial Port", 106.0f),
        (UI2dComponent) outputChannelLabel } };
  }

  @Override
  public void buildFixtureControls(LXStudio.UI ui, UIFixture uiFixture,
    LPPanelFixture fixture) {
    uiFixture.addGeometrySection();
    uiFixture.addSection("fixture",
      this.buildFixtureSection(uiFixture, fixture));
    uiFixture.addSection("Wiring", this.buildWiringSection(uiFixture, fixture));
    // uiFixture.addProtocolSection((LXProtocolFixture) fixture);
    uiFixture.addSection("Datagram Protocol",
      this.buildDatagramProtocolSection(uiFixture, fixture, true));
    uiFixture.addSection("Serial Protocol",
      this.buildSerialProtocolSection(uiFixture, fixture, true));
  }
}

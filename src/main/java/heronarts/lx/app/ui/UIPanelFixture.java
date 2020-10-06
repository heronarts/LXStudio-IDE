package heronarts.lx.app.ui;

import java.util.logging.Logger;

import flavius.ledportal.structure.LPPanelFixture;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.structure.LXFixture;
import heronarts.lx.structure.LXProtocolFixture;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.fixture.UIFixture;
import heronarts.lx.studio.ui.fixture.UIFixture.Section;
import heronarts.lx.studio.ui.fixture.UIFixtureControls;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UITheme;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIDropMenu;
import heronarts.p3lx.ui.component.UIIntegerBox;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.component.UITextBox;
import org.apache.commons.lang3.reflect.FieldUtils;

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
      { (UI2dComponent) uiFixture.newParameterLabel("Row,Col Spacing",
        (float) GRID_LABEL_WIDTH),
        (UI2dComponent) uiFixture.newControlBox(fixture.rowSpacing,
          (float) GRID_CONTROL_WIDTH_SMALL),
        (UI2dComponent) uiFixture.newControlBox(fixture.columnSpacing,
          (float) GRID_CONTROL_WIDTH_SMALL) },
      { (UI2dComponent) uiFixture.newParameterLabel("Row Shear",
        (float) GRID_LABEL_WIDTH),
        (UI2dComponent) uiFixture.newControlBox(fixture.rowShear,
          (float) GRID_CONTROL_WIDTH_SMALL) },
      { (UI2dComponent) uiFixture.newParameterLabel("Point Indices (JSON)",
        (float) GRID_CONTROL_WIDTH_FULL) },
      { (UI2dComponent) uiFixture.newControlTextBox(fixture.pointIndicesJSON,
        (float) (GRID_CONTROL_WIDTH_FULL)) },
      { (UI2dComponent) uiFixture.newParameterLabel("X,Y: Glb. Orig.",
        (float) GRID_LABEL_WIDTH),
        (UI2dComponent) uiFixture.newControlIntBox(fixture.globalGridOriginX,
          (float) (GRID_CONTROL_WIDTH_SMALL)),
        (UI2dComponent) uiFixture.newControlIntBox(fixture.globalGridOriginY,
          (float) (GRID_CONTROL_WIDTH_SMALL)) },
      { (UI2dComponent) uiFixture.newParameterLabel("Grid Matrix (JSON)",
        (float) GRID_CONTROL_WIDTH_FULL) },
      { (UI2dComponent) uiFixture.newControlTextBox(fixture.globalGridMatrix,
        (float) (GRID_CONTROL_WIDTH_FULL)) }, };
  }

  public UI2dComponent[][] buildWiringSection(UIFixture uiFixture,
    LPPanelFixture fixture) {
    return new UI2dComponent[][] {
      { (UI2dComponent) new UIDropMenu(0.0f, 0.0f, uiFixture.getContentWidth(),
        (float) GRID_HEIGHT, (DiscreteParameter) fixture.wiring) },
      { (UI2dComponent) new UIButton(0.0f, 0.0f, 96.0f, (float) GRID_HEIGHT)
        .setParameter(fixture.splitPacket).setLabel("Multi-Packet"),
        (UI2dComponent) uiFixture.newControlIntBox(fixture.pointsPerPacket,
          (float) (uiFixture.getContentWidth() - 98.0f)) } };
  }

  public UI2dComponent[][] buildSerialProtocolSection(UIFixture uiFixture,
    LPPanelFixture fixture) {
    return buildSerialProtocolSection(uiFixture, fixture, false);
  }

  public UI2dComponent[][] buildSerialProtocolSection(UIFixture uiFixture,
    LPPanelFixture fixture, final boolean includeReverseOption) {
    final UITextBox outputSerialPort = new UITextBox(0.0f, 0.0f, 106.0f,
      (float) GRID_HEIGHT).setParameter(fixture.serialPort);
    final UIIntegerBox outputChannel = new UIIntegerBox(0.0f, 0.0f,
      (float) GRID_LABEL_WIDTH, (float) GRID_HEIGHT);
    final UILabel outputChannelLabel = uiFixture.newControlLabel("Channel",
      (float) GRID_LABEL_WIDTH);
    final UIIntegerBox outputBaudRate = new UIIntegerBox(0.0f, 0.0f,
      (float) GRID_LABEL_WIDTH, (float) GRID_HEIGHT);
    final UIButton reverseButton = includeReverseOption
      ? new UIButton(0.0f, 0.0f, 24.0f, (float) GRID_HEIGHT)
        .setParameter(fixture.reverse).setActiveLabel("\u2190")
        .setInactiveLabel("\u2192")
      : null;
    outputBaudRate.setParameter(fixture.baudRate).setEnabled(true);
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
      switch ((LPPanelFixture.SerialProtocol) fixture.serialProtocol
        .getEnum()) {
      case PBX_WS281X:
      case PBX_APA102: {
        outputChannel.setParameter(fixture.pixelBlazeChannel).setEnabled(true);
        // outputChannelLabel.setLabel("Channel");
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
        (float) GRID_HEIGHT, (DiscreteParameter) fixture.serialProtocol),
        (UI2dComponent) reverseButton },
      { (UI2dComponent) uiFixture.newControlLabel("Serial Port",
        GRID_LABEL_WIDTH), (UI2dComponent) outputSerialPort, },
      { (UI2dComponent) outputChannelLabel, (UI2dComponent) outputChannel, },
      { (UI2dComponent) uiFixture.newControlLabel("Baud Rate",
        GRID_LABEL_WIDTH), (UI2dComponent) outputBaudRate } };
  }

  @Override
  public void buildFixtureControls(LXStudio.UI ui, UIFixture uiFixture,
    LPPanelFixture fixture) {
    uiFixture.addGeometrySection();
    uiFixture.addSection("fixture",
      this.buildFixtureSection(uiFixture, fixture));
    uiFixture.addSection("Wiring", this.buildWiringSection(uiFixture, fixture));
    // uiFixture.addProtocolSection((LXProtocolFixture) fixture);
    uiFixture.addProtocolSection((LXProtocolFixture) fixture, true);
    uiFixture.addSection("Serial Protocol",
      this.buildSerialProtocolSection(uiFixture, fixture, true));
  }
}

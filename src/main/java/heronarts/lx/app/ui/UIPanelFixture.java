package heronarts.lx.app.ui;

import java.util.logging.Logger;

import flavius.ledportal.structure.LPPanelFixture;
import heronarts.lx.structure.LXProtocolFixture;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.fixture.UIFixture;
import heronarts.lx.studio.ui.fixture.UIFixtureControls;
import heronarts.p4lx.ui.UI2dComponent;

public class UIPanelFixture implements UIFixtureControls<LPPanelFixture> {

  private static final int GRID_CONTROL_WIDTH_SMALL = 51;
  // private static final int GRID_CONTROL_WIDTH_MEDIUM = 104;
  private static final int GRID_CONTROL_WIDTH_FULL = 168;
  private static final int GRID_LABEL_WIDTH = 64;
  // private static final int GRID_HEIGHT = 16;

  protected static final Logger logger = Logger
    .getLogger(UIPanelFixture.class.getName());

  public UI2dComponent[][] buildFixtureSection(UIFixture uiFixture,
    LPPanelFixture fixture) {
    return new UI2dComponent[][] {
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

  @Override
  public void buildFixtureControls(LXStudio.UI ui, UIFixture uiFixture,
    LPPanelFixture fixture) {
    uiFixture.addTagSection();
    uiFixture.addGeometrySection();
    uiFixture.addSection("fixture",
      this.buildFixtureSection(uiFixture, fixture));
    uiFixture.addProtocolSection((LXProtocolFixture) fixture, true);
  }
}

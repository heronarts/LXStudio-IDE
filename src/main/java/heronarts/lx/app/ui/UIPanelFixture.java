package heronarts.lx.app.ui;

import java.util.logging.Logger;

import flavius.ledportal.structure.LPPanelFixture;
import heronarts.lx.structure.LXProtocolFixture;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.fixture.UIFixture;
import heronarts.lx.studio.ui.fixture.UIFixtureControls;

public class UIPanelFixture implements UIFixtureControls<LPPanelFixture> {

  protected static final Logger logger = Logger
    .getLogger(UIPanelFixture.class.getName());

  @Override
  public void buildFixtureControls(LXStudio.UI ui, UIFixture uiFixture,
    LPPanelFixture fixture) {
    uiFixture.addTagSection();
    uiFixture.addGeometrySection();
    uiFixture.addProtocolSection((LXProtocolFixture) fixture, true);
  }
}

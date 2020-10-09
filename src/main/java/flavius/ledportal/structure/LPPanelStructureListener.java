package flavius.ledportal.structure;

import java.util.logging.Logger;

import flavius.ledportal.LPPanelModel;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.structure.LXFixture;
import heronarts.lx.structure.LXStructure;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * A shadow structure used to keep track of {@link LPPanelFixture} fixtures and
 * stitch multiple {@ LPPanelModel}s together to use in grid patterns
 *
 * TODO(dev): is this class necessary? Does it need to be a shadow structure
 */
public class LPPanelStructureListener extends LXComponent
  implements LXStructure.Listener {

  private static final Logger logger = Logger
    .getLogger(LPPanelStructureListener.class.getName());

  private LPPanelModel panelModel;
  private boolean needsRegenerateModel = true;

  // TODO(dev): modelListener?
  public LPPanelStructureListener(LX lx) {
    super(lx);
  }

  private void regenerateModel() {
    LPPanelModel[] submodels = new LPPanelModel[this.lx.structure.fixtures
      .size()];
    int fixtureIndex = 0;
    for (LXFixture fixture : this.lx.structure.fixtures) {
      if (!LPPanelFixture.class.isInstance(fixture)) {
        continue;
      }
      LPPanelFixture panelFixture = (LPPanelFixture) fixture;
      LPPanelModel fixtureModel = null;
      try {
        fixtureModel = (LPPanelModel) (MethodUtils
          .invokeMethod(panelFixture, "toModel"));
      } catch (Exception e) {
        logger.warning(e.toString());
      }
      submodels[fixtureIndex++] = fixtureModel;
    }
    panelModel = new LPPanelModel(submodels);
    needsRegenerateModel = false;
  }

  public LPPanelModel getLPPanelModel() {
    if (needsRegenerateModel) {
      regenerateModel();
    }
    return panelModel;
  }

  @Override
  public void fixtureAdded(LXFixture fixture) {
    needsRegenerateModel = true;
  }

  @Override
  public void fixtureRemoved(LXFixture fixture) {
    needsRegenerateModel = true;
  }

  @Override
  public void fixtureMoved(LXFixture fixture, int index) {
    needsRegenerateModel = true;
  }
}

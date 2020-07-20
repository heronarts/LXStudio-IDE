package flavius.ledportal.pattern;

import flavius.ledportal.LPPanelModel;
import flavius.ledportal.structure.LPPanelStructureListener;
import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXPattern;

/**
 * Templatized version of the LXPattern class, which strongly types a particular fixture.
 *
 * @param <T> Type of LXModel class that is always expected
 */
public abstract class LPPanelStructurePattern extends LXPattern {

  protected LPPanelStructureListener structure;
  protected LPPanelModel model;

  protected LPPanelStructurePattern(LX lx) {
    super(lx);
    this.structure = LXStudioApp.panelStructure;
  }

  @Override
  public LXModel getModel() {
    return (LXModel)(this.model = this.structure.getLPPanelModel());
  }
}

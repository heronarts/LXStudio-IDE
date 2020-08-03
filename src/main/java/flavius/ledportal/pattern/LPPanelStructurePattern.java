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
    this.getModel();
  }

  public void beforeUpdateModel(LPPanelModel newModel) {}

  @Override
  public LXModel getModel() {
    LPPanelModel newModel = this.structure.getLPPanelModel();
    if (newModel != this.model) {
      this.beforeUpdateModel(newModel);
      this.model = newModel;
    }
    return this.model;
  }
}

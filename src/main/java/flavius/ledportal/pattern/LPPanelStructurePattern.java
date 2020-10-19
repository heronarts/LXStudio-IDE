package flavius.ledportal.pattern;

import java.util.Arrays;

import flavius.ledportal.LPPanelModel;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXModelPattern;

/**
 * Templatized version of the LXPattern class, which strongly types a particular
 * fixture.
 *
 * @param <T> Type of LXModel class that is always expected
 */
// public abstract class LPPanelStructurePattern extends LXPattern {
public abstract class LPPanelStructurePattern
  extends LXModelPattern<LPPanelModel> {
  protected LXModel structureModel;

  protected LPPanelStructurePattern(LX lx) {
    super(lx);
    this.getModel();
  }

  public void beforeUpdateModel(LPPanelModel newModel) {
  }

  public boolean isLPPanelModel(LXModel model) {
    return LPPanelModel.class.isInstance(model);
  }

  @Override
  public LPPanelModel getModel() {
    LXModel newStructureModel = lx.structure.getModel();
    if (isLPPanelModel(this.model)
      && newStructureModel == this.structureModel) {
      return this.model;
    }
    structureModel = newStructureModel;
    LXModel[] fixtureModels = structureModel.children;
    LPPanelModel[] childModels = Arrays.stream(fixtureModels)
      .filter(model -> LPPanelModel.class.isInstance(model))
      .map(model -> (LPPanelModel) (model)).toArray(LPPanelModel[]::new);
    LPPanelModel newModel = new LPPanelModel(childModels);
    if (!(newModel.width > 0 && newModel.height > 0)) {
      throw new IllegalArgumentException(
        "model must have nonzero width and height");
    }
    if (newModel != this.model) {
      this.beforeUpdateModel(newModel);
      this.model = newModel;
    }
    return this.model;
  }
}

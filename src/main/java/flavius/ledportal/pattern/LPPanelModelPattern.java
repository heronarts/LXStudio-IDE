package flavius.ledportal.pattern;

import java.util.Arrays;
import java.util.logging.Logger;

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
public abstract class LPPanelModelPattern
  extends LXModelPattern<LPPanelModel> {
  protected LXModel structureModel;

  protected static final Logger logger = Logger
    .getLogger(LPPanelModelPattern.class.getName());

  protected LPPanelModelPattern(LX lx) {
    super(lx);
    getModel();
  }

  public void beforeUpdateModel(LPPanelModel newModel) {
  }

  public boolean isLPPanelModel(LXModel model) {
    return LPPanelModel.class.isInstance(model);
  }

  @Override
  public LPPanelModel getModel() {
    LXModel newStructureModel = lx.structure.getModel();
    if (isLPPanelModel(model) && newStructureModel == structureModel) {
      return model;
    }
    LPPanelModel[] childModels = Arrays.stream(newStructureModel.children)
      .filter(model -> LPPanelModel.class.isInstance(model))
      .map(model -> (LPPanelModel) (model)).toArray(LPPanelModel[]::new);
    if (childModels.length == 0 && newStructureModel.children.length != 0) {
      logger.warning(String.format(
        "This pattern only works with LPPanelModels, none found in %s",
        newStructureModel.toString()));
    }
    structureModel = newStructureModel;
    LPPanelModel newModel = new LPPanelModel(childModels);
    if (!(newModel.width > 0 && newModel.height > 0)) {
      throw new IllegalArgumentException(
        "model must have nonzero width and height");
    }
    if (newModel != this.model) {
      beforeUpdateModel(newModel);
      model = newModel;
    }
    return model;
  }
}

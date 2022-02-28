package flavius.ledportal.pattern;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Logger;

import flavius.ledportal.LPPanelModel;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXModelPattern;

/**
 * An `LXModelPattern` which is designed to work with an `LPPanelModel`.
 *
 * There must be at least one LPPanelModel in `lx.structure` for this to work.
 */
public abstract class LPPanelModelPattern
  extends LXModelPattern<LPPanelModel> {

  /**
   * Keep track of the model returned by `lx.structure.getModel()` each time
   * `this.getModel` is called to determine if things have changed since last
   * time.
   */
  protected LXModel structureModel;

  protected static final Logger logger = Logger
    .getLogger(LPPanelModelPattern.class.getName());

  protected LPPanelModelPattern(LX lx) {
    super(lx);
    getModel();
  }

  /**
   * Hook which is called before a model update occurs.
   */
  public void beforeUpdateModel(LPPanelModel newModel) {
  }

  public boolean isLPPanelModel(LXModel model) {
    return model.meta("type") == "LPPanelModel";
  }

  /**
   * Assemble fixtures from `LXStructure` into an `LPPanelModel`
   */
  @Override
  public LPPanelModel getModel() {
    LXModel newStructureModel = lx.structure.getModel();
    if (isLPPanelModel(model) && newStructureModel == structureModel) {
      return model;
    }
    LPPanelModel[] childModels = Arrays.stream(newStructureModel.children)
      .map(model -> {
        if (!isLPPanelModel(model)) {
          return null;
        }
        try {
          return (LPPanelModel) model;
        } catch (Exception e) {
          logger.warning(e.toString());
          return null;
        }
      })
      .filter(model -> model != null)
      .toArray(LPPanelModel[]::new);
    if (childModels.length == 0 && newStructureModel.children.length != 0) {
      logger.warning(String.format(
        "This pattern only works with LPPanelModels, none found in %s",
        newStructureModel.toString()));
      return model;
    }
    structureModel = newStructureModel;
    LPPanelModel newModel = new LPPanelModel(childModels);
    if (!(newModel.width > 0 && newModel.height > 0)) {
      throw new IllegalArgumentException(
        "model must have nonzero width and height");
    } else {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      try {
        newModel.debugPrint(new PrintStream(os));
      } catch (Exception e) {
        logger.warning(e.toString());
      }
      logger.info(String.format("new model: %d x %d (%d). X(%d,%d), Y(%d,%d) %s", newModel.width, newModel.height, newModel.size, newModel.metrics.xiMin, newModel.metrics.xiMax, newModel.metrics.yiMin, newModel.metrics.yiMax, os.toString()));
    }
    if (newModel != this.model) {
      beforeUpdateModel(newModel);
      model = newModel;
    }
    return model;
  }
}

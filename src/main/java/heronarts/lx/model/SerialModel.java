package heronarts.lx.model;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.output.SerialMessage;

/**
 * Extends {@link SerialModel} to allow for sending serial messages in addition to datagrams
 */
public class SerialModel extends LXModel {
  // TODO(dev): is SerialModel necessary?

  /**
   * An ordered list of messages that should be sent for this model.
   */
  public final List<SerialMessage> messages = new ArrayList<SerialMessage>();

  /**
   * Constructs a null model with no points
   */
  public SerialModel() {
    this(new ArrayList<LXPoint>());
  }

  /**
   * Constructs a model from a list of points
   *
   * @param points Points in the model
   */
  public SerialModel(List<LXPoint> points) {
    this(points, new SerialModel[0]);
  }

  /**
   * Constructs a model from a list of points
   *
   * @param points Points in the model
   * @param keys Key identifiers for the model type
   */
  public SerialModel(List<LXPoint> points, String ... keys) {
    this(points, new SerialModel[0], keys);
  }

  /**
   * Constructs a model with a given set of points and pre-constructed children. In this case, points
   * from the children are not added to the points array, they are assumed to already be contained by
   * the points list.
   *
   * @param points Points in this model
   * @param children Pre-built direct child model array
   */
  public SerialModel(List<LXPoint> points, LXModel[] children) {
    this(points, children, SerialModel.Key.MODEL);
  }

  /**
   * Constructs a model with a given set of points and pre-constructed submodels. In this case, points
   * from the submodels are not added to the points array, they are assumed to already be contained by
   * the points list.
   *
   * @param points Points in this model
   * @param children Pre-built direct submodel child array
   * @param keys Key identifier for this model
   */
  public SerialModel(List<LXPoint> points, LXModel[] children, String ... keys) {
    super(points, children, keys);
  }
}

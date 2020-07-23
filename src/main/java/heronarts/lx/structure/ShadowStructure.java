package heronarts.lx.structure;

import java.util.List;
import java.util.logging.Logger;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * A {@link LXStructure} which listens to {@link hernarts.lx.LX.structure} for
 * changes in fixtures
 */
public class ShadowStructure extends LXStructure implements LXStructure.Listener {

  private static final Logger logger = Logger
    .getLogger(ShadowStructure.class.getName());

  public ShadowStructure(LX lx) {
    this(lx, null);
  }

  public ShadowStructure(LX lx, LXModel immutable) {
    this(lx, immutable, new ModelListener(){
      @Override
      public void modelChanged(LXModel model) { /* noop */ }
    });
  }

  public ShadowStructure(LX lx, LXModel immutable, ModelListener modelListener) {
    super(lx, immutable);
    this.lx.structure.addListener(this);
    this.setModelListener(modelListener);
    for(LXFixture fixture: this.lx.structure.fixtures) {
      fixtureAdded(fixture);
    }
  }

  public void onFixtureChanged() {}

  @Override
  public void fixtureAdded(LXFixture fixture) {
    List<LXFixture> mutableFixtures = null;
    try {
      mutableFixtures = (List<LXFixture>) (FieldUtils.readField(this,
        "mutableFixtures", true));
    } catch (Exception e) {
      logger.warning(e.toString());
      return;
    }
    int index = this.fixtures.size();
    mutableFixtures.add(index, fixture);
    onFixtureChanged();
  }

  @Override
  public void fixtureRemoved(LXFixture fixture) {
    List<LXFixture> mutableFixtures = null;
    try {
      mutableFixtures = (List<LXFixture>) (FieldUtils.readField(this,
        "mutableFixtures", true));
    } catch (Exception e) {
      logger.warning(e.toString());
      return;
    }
    mutableFixtures.remove(fixture);
    onFixtureChanged();
  }

  @Override
  public void fixtureMoved(LXFixture fixture, int index) {
    List<LXFixture> mutableFixtures = null;
    try {
      mutableFixtures = (List<LXFixture>) (FieldUtils.readField(this,
        "mutableFixtures", true));
    } catch (Exception e) {
      logger.warning(e.toString());
      return;
    }
    mutableFixtures.remove(fixture);
    mutableFixtures.add(index, fixture);
    onFixtureChanged();
  }
}
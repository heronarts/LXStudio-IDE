package flavius.pixelblaze;

/**
 * A factory for generating the DRAW_ALL message
 *
 * @author <a href="https://dev.laserphile.com/">Derwent McElhinney</a>
 */
public class PBMessageFactoryDrawAll extends PBMessageFactory {
  public PBMessageFactoryDrawAll() {
    super(PBRecordType.DRAW_ALL);
  }
}

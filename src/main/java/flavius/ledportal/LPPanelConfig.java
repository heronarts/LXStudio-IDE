package flavius.ledportal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import processing.data.JSONObject;
import processing.data.JSONArray;
import processing.core.PMatrix3D;
import processing.core.PVector;

public class LPPanelConfig extends LPMeshable {
  private static final Logger logger = Logger
    .getLogger(LPPanelConfig.class.getName());
  public int[][] leds;

  public LPPanelConfig updateFromJSONObject(JSONObject jsonConfig) {
    super.updateFromJSONObject(jsonConfig);
    if (jsonConfig.hasKey("pixels")) {
      JSONArray ledList = jsonConfig.getJSONArray("pixels");
      this.leds = new int[ledList.size()][2];
      for (int i = 0; i < ledList.size(); i++) {
        JSONArray led = ledList.getJSONArray(i);
        this.leds[i][0] = led.getInt(0);
        this.leds[i][1] = led.getInt(1);
      }
      logger.info(String.format("has %d pixels", this.leds.length));
    }
    return this;
  }

  public List<PVector> getWorldPixels() {
    List<PVector> worldPixels = new ArrayList<PVector>();
    for (int i=0; i<this.leds.length; i++) {
      PVector led = new PVector((float)this.leds[i][0], (float)this.leds[i][1]);
      worldPixels.add(worldPixelTransform(getWorldCoordinate(led)));
    }
    logger
      .fine(String.format("world pixels: %s", formatPVectorList(worldPixels)));
    return worldPixels;
  }

  public LPPanelModel getModel() {
    List<PMatrix3D> matrices = new ArrayList<PMatrix3D>();
    matrices.add(worldToUI);
    matrices.add(matrix);
    return new LPPanelModel(composeMatrices(matrices), leds);
  }

}

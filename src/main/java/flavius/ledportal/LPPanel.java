package flavius.ledportal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import processing.data.JSONObject;
import processing.data.JSONArray;
import processing.core.PVector;

public class LPPanel extends LPMeshable {
  private static final Logger logger = Logger
    .getLogger(LPPanel.class.getName());
  public List<PVector> leds;

  public LPPanel() {
    leds = new ArrayList<PVector>();
  }

  public LPPanel(List<PVector> leds) {
    this();
    this.leds = leds;
  }

  public LPPanel updateFromJSONObject(JSONObject jsonConfig) {
    super.updateFromJSONObject(jsonConfig);
    if (jsonConfig.hasKey("pixels")) {
      JSONArray ledList = jsonConfig.getJSONArray("pixels");
      for (int i = 0; i < ledList.size(); i++) {
        JSONArray led = ledList.getJSONArray(i);
        this.leds.add(new PVector(led.getInt(0), led.getInt(1), 0));
      }
      logger.info(String.format("has %d pixels: %s", this.leds.size(),
        formatPVectorList(this.leds)));
    }
    return this;
  }

  public String toString() {
    String out = "Panel:\n";
    out += "-> LEDs:\n\t";
    for (PVector led : this.leds) {
      out += led.toString() + "\n\t";
    }
    return out;
  }

  public List<PVector> getWorldPixels() {
    List<PVector> worldPixels = new ArrayList<PVector>();
    for (PVector led : this.leds) {
      worldPixels.add(worldPixelTransform(getWorldCoordinate(led)));
    }
    logger
      .fine(String.format("world pixels: %s", formatPVectorList(worldPixels)));
    return worldPixels;
  }

}

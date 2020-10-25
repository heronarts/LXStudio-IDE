package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.ObjectParameter;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanelTexture extends LPPanel3DGraphicsPattern {

  PImage foreground;

  public final ObjectParameter<String> texture;

  public LPPanelTexture(LX lx) {
    super(lx);

    String[] textureNames = LXStudioApp.instance.imageLibrary.getNames();
    this.texture = new ObjectParameter<String>("texture", textureNames);

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("xShear", this.xShear);
    addParameter("size", this.scale);
    addParameter("texture", this.texture);
    addParameter("fov", this.fov);
    addParameter("depth", this.depth);

    refreshForeground();
  }

  public void onParameterChanged(LXParameter p) {
    super.onParameterChanged(p);
    if (p == this.texture) {
      refreshForeground();
    }
  }

  public void refreshForeground() {
    String textureName = (String) this.texture.getObject();
    foreground = LXStudioApp.instance.imageLibrary.prepareMedia(textureName);
  }

  @Override
  public void beforeDraw(final PGraphics pg) {
    super.beforeDraw(pg);
    pg.smooth(8);
  }

  @Override
  public void onDraw(final PGraphics pg) {
    pg.pushMatrix();
    applyBackground();
    applyScale();
    applyTranslation();
    applyShear();
    applyRotation();
    applyForeground(foreground);
    pg.popMatrix();
  }
}

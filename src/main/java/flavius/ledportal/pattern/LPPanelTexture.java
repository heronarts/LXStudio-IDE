package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIDropMenu;
import heronarts.p4lx.ui.component.UIKnob;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanelTexture extends LPPanel3DGraphicsPattern
  implements UIDeviceControls<LPPanelTexture> {

  PImage foreground;

  public final ObjectParameter<String> texture;
  protected final UIDropMenu uiDrop;

  public LPPanelTexture(LX lx) {
    super(lx);

    String[] textureNames = LXStudioApp.instance.imageLibrary.getNames();
    if (textureNames.length == 0) {
      logger.warning("no gifs available");
      texture = new ObjectParameter<String>("texture", new String[] { "" });
    } else {
      texture = new ObjectParameter<String>("texture", textureNames);
    }

    uiDrop = new UIDropMenu(COL_WIDTH * 3, texture);
    uiDrop.setDirection(UIDropMenu.Direction.UP);

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("alphaCurve", this.alphaCurve);
    addParameter("size", this.scale);
    addParameter("texture", this.texture);

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
    applyTranslation();
    applyScale();
    applyShear();
    applyRotation();
    applyForeground(foreground);
    pg.popMatrix();
  }

  @Override
  public void buildDeviceControls(UI ui, UIDevice uiDevice,
    LPPanelTexture pattern) {
    uiDrop.setParameter(pattern.texture);
    uiDevice.setContentWidth(COL_WIDTH * 3);
    // float height = UIDropMenu.DEFAULT_HEIGHT;
    // UITextBox searchUI = new UITextBox(0, 0, COL_WIDTH * 2, height);
    // searchUI.setParameter(pattern.query);
    addColumn(uiDevice, COL_WIDTH * 3, //
      // searchUI, //
      uiDrop, //
      UI2dContainer.newHorizontalContainer(UIKnob.HEIGHT, 0, //
        new UIKnob(pattern.xOffset), //
        new UIKnob(pattern.yOffset), //
        new UIKnob(pattern.xRotate), //
        new UIKnob(pattern.yRotate) //
      ), //
      UI2dContainer.newHorizontalContainer(UIKnob.HEIGHT, 0, //
        new UIKnob(pattern.zRotate), //
        new UIKnob(pattern.alphaCurve), //
        new UIKnob(pattern.scale), //
        new UIKnob(pattern.texture) //
      ) //
    );
  }
}

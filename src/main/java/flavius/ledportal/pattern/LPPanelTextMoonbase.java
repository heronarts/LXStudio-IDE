package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.app.media.FontLibrary;
import heronarts.lx.color.LXColor;
// import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.p4lx.ui.UI2dContainer;
import heronarts.p4lx.ui.component.UIDropMenu;
import heronarts.p4lx.ui.component.UIKnob;
import heronarts.p4lx.ui.component.UITextBox;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

public class LPPanelTextMoonbase extends LPPanel3DGraphicsPattern
  implements UIDeviceControls<LPPanelTextMoonbase> {

  protected FontLibrary library;
  PFont font;
  protected final StringParameter text = new StringParameter("text",
    "MOONBASE");
  // protected final ObjectParameter<String> fontName;
  // protected final UIDropMenu uiDrop;

  public LPPanelTextMoonbase(LX lx) {
    super(lx);

    library = LXStudioApp.instance.fontLibrary;

    // String[] fontNames = library.getNames();
    // if (fontNames.length == 0) {
    // logger.warning("no fonts available");
    // fontName = new ObjectParameter<String>("gif", new String[] { "" });
    // } else {
    // fontName = new ObjectParameter<String>("gif", fontNames);
    // }

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("size", this.scale);
    addParameter("beats", this.beats);
    addParameter("alpha", this.alphaCurve);
    addParameter("xScanFuckery", this.xScanFuckery);
    addParameter("yScanFuckery", this.yScanFuckery);
    addParameter("pScanFuckery", this.pScanFuckery);

    refreshFont();
  }

  public void refreshFont() {
    font = library.prepareMedia("Exo2-Black.ttf", 96);
  }

  @Override
  public void onDraw(final PGraphics pg) {
    final int beats = this.beats.getValuei();
    String marqueeText = this.text.getString();
    final int marqueeIndex = (this.lx.engine.tempo.beatCount() / beats)
      % marqueeText.length();

    pg.pushMatrix();
    if (font != null)
      pg.textFont(font);
    pg.textAlign(PConstants.CENTER, PConstants.CENTER);
    applyBackground();
    applyTranslation();
    applyShear();
    applyRotation();
    applyScale();
    pg.noStroke();
    pg.fill(LXColor.rgb(255, 255, 255));
    char marqueeChar = marqueeText.charAt(marqueeIndex);
    pg.textMode(PConstants.CENTER);
    pg.text(marqueeChar, 0.f, 0.f);
    pg.popMatrix();
  }

  @Override
  public void buildDeviceControls(UI ui, UIDevice uiDevice,
    LPPanelTextMoonbase pattern) {
    // uiDrop.setParameter(pattern.fontName);
    float height = UIDropMenu.DEFAULT_HEIGHT;
    uiDevice.setContentWidth(COL_WIDTH * 3);
    UITextBox uiText = new UITextBox(0, 0, COL_WIDTH * 2, height);
    uiText.setParameter(pattern.text);
    addColumn(uiDevice, COL_WIDTH * 3, //
      uiText, //
      UI2dContainer.newHorizontalContainer(UIKnob.HEIGHT, 0, //
        new UIKnob(pattern.xOffset), //
        new UIKnob(pattern.yOffset), //
        new UIKnob(pattern.xRotate), //
        new UIKnob(pattern.yRotate) //
      ), //
      UI2dContainer.newHorizontalContainer(UIKnob.HEIGHT, 0, //
        new UIKnob(pattern.zRotate), //
        new UIKnob(pattern.scale), //
        new UIKnob(pattern.beats), //
        new UIKnob(pattern.alphaCurve) //
      ), //
      UI2dContainer.newHorizontalContainer(UIKnob.HEIGHT, 0, //
        new UIKnob(pattern.xScanFuckery), //
        new UIKnob(pattern.yScanFuckery), //
        new UIKnob(pattern.pScanFuckery) //
      ) //
    );
  }
}

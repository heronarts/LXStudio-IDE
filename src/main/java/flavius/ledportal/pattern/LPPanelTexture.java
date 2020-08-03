package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.ObjectParameter;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix3D;

/**
 * Draw an SVG pattern directly to a panel where pixels are arranged in a fixed
 * grid pattern
 */
public class LPPanelTexture extends LPPanel3DGraphicsPattern {

  PImage foreground;
  int foreSize;
  int foreHeight;
  int foreWidth;

  public final CompoundParameter xOffset = new CompoundParameter("X-Off", 0, -1,
    1).setDescription("Sets the placement in the X axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yOffset = new CompoundParameter("Y-Off", 0, -1,
    1).setDescription("Sets the placement in the Y axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zOffset = new CompoundParameter("Z-Off", 0, -1,
    1).setDescription("Sets the placement in the Z axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter xRotate = new CompoundParameter("X-Rot", 0, -1,
    1).setDescription("Sets the rotation about the X axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter yRotate = new CompoundParameter("Y-Rot", 0, -1,
    1).setDescription("Sets the rotation about the Y axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter zRotate = new CompoundParameter("Z-Rot", 0, -1,
    1).setDescription("Sets the rotation about the Z axis")
      .setPolarity(LXParameter.Polarity.BIPOLAR);

  public final CompoundParameter scale = new CompoundParameter("Size", 1, -1, 1)
    .setDescription("Sets the size");

  public final ObjectParameter<String> texture;

  public LPPanelTexture(LX lx) {
    super(lx);

    String[] textureNames = new String[LXStudioApp.textures.size()];
    LXStudioApp.textures.keySet().toArray(textureNames);
    this.texture = new ObjectParameter<String>("texture", textureNames);

    addParameter("xOffset", this.xOffset);
    addParameter("yOffset", this.yOffset);
    addParameter("zOffset", this.zOffset);
    addParameter("xRotate", this.xRotate);
    addParameter("yRotate", this.yRotate);
    addParameter("zRotate", this.zRotate);
    addParameter("size", this.scale);
    addParameter("texture", this.texture);

    refreshForeground();
    this.texture.addListener(p -> this.refreshForeground());
  }

  public void refreshForeground() {
    String textureName = (String) this.texture.getObject();
    foreground = LXStudioApp.textures.get(textureName);
    if(foreground == null) {
      foreSize = 0;
      foreHeight = 0;
      foreWidth = 0;
    } else {
      foreSize = Math.max(foreground.width, foreground.height);
      foreHeight = foreground.height;
      foreWidth = foreground.width;
    }
  }

  public void beforeDraw(final PGraphics pg) {
    super.beforeDraw(pg);
    pg.smooth(8);
  }

  @Override
  public void onDraw(final PGraphics pg) {

    final float xOffset = this.xOffset.getValuef();
    final float yOffset = this.yOffset.getValuef();
    final float zOffset = this.zOffset.getValuef();
    final float xRotate = this.xRotate.getValuef();
    final float yRotate = this.yRotate.getValuef();
    final float zRotate = this.zRotate.getValuef();
    final float scale = (float) Math.pow(this.scale.getValue(), 2.d);

    pg.pushMatrix();
    pg.background(LXColor.BLACK);
    pg.translate( //
      /**/ xOffset * model.width, //
      /**/ yOffset * model.height, //
      /**/ zOffset * Math.max(model.width, model.height));
    pg.applyMatrix(new PMatrix3D( //
      /**/ 1.f, -0.5f, 0.f, 0.f, //
      /**/ 0.f, 1.f, 0.f, 0.f, //
      /**/ 0.f, 0.f, 1.f, 0.f, //
      /**/ 0.f, 0.f, 0.f, 1.f)); //
    pg.rotateX((float) Math.PI * xRotate);
    pg.rotateY((float) Math.PI * yRotate);
    pg.rotateZ((float) Math.PI * zRotate);
    pg.noStroke();
    pg.beginShape();
    pg.texture(this.foreground);
    float foreX = scale * this.frameSize * foreWidth / foreSize;
    float foreY = scale * this.frameSize * foreHeight / foreSize;
    pg.vertex(-foreX, -foreY, 0, 0, 0);
    pg.vertex(foreX, -foreY, 0, foreWidth, 0);
    pg.vertex(foreX, foreY, 0, foreWidth, foreHeight);
    pg.vertex(-foreX, foreY, 0, 0, foreHeight);
    pg.endShape();
    pg.scale(scale * this.frameSize);
    pg.popMatrix();
  }
}

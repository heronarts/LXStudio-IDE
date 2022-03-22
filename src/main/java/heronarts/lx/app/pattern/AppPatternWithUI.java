/**
 * Copyright 2022- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.app.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio.UI;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;

@LXCategory(LXCategory.TEST)
public class AppPatternWithUI extends LXPattern implements UIDeviceControls<AppPatternWithUI> {

  public final DiscreteParameter number =
    new DiscreteParameter("Number", 100)
    .setDescription("A numeric parameter");

  public final ObjectParameter<String> string =
    new ObjectParameter<String>("String", new String[] { "One", "Two", "Three" })
    .setDescription("A string parameter");

  public final CompoundParameter knob =
    new CompoundParameter("Knob", 0, 1)
    .setDescription("A knob parameter");

  public AppPatternWithUI(LX lx) {
    super(lx);
    addParameter("number", this.number);
    addParameter("string", this.string);
    addParameter("knob", this.knob);
  }

  @Override
  protected void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = LXColor.hsb(240, 100, 100);
    }
  }

  /**
   * Implement this method from the UIDeviceControls interface to build a custom UI for
   * your pattern device, rather than relying upon the in-built default UI.
   */
  @Override
  public void buildDeviceControls(UI ui, UIDevice uiDevice, AppPatternWithUI pattern) {
    uiDevice.setContentWidth(COL_WIDTH);
    addColumn(uiDevice, "Custom",
      newIntegerBox(pattern.number),
      controlLabel(ui, "Number"),
      newDropMenu(pattern.string),
      controlLabel(ui, "String"),
      newKnob(pattern.knob)
    );
  }

}

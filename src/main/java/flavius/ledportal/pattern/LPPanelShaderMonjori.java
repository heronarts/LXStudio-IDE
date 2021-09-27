package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.app.media.MediaLibrary;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * 
 * Stolen from https://github.com/genekogan/Processing-Shader-Examples
 */
public class LPPanelShaderMonjori extends LPPanelShaderDirect {
    long startTime;

    public final CompoundParameter graininess = new CompoundParameter(
        "graininess", 50.0, 10.0, 100.0).setDescription("graininess")
            .setPolarity(LXParameter.Polarity.BIPOLAR);
    public final CompoundParameter pace = new CompoundParameter(
        "pace", 30.0, 20.0, 80.0).setDescription("speed of movement")
            .setPolarity(LXParameter.Polarity.BIPOLAR);
    public final CompoundParameter twist = new CompoundParameter("twist", 10.0,
        0.0, 100.0).setDescription("twistiness")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public LPPanelShaderMonjori(LX lx) {
        super(lx);

        startTime = System.currentTimeMillis();

        String shaderPath = MediaLibrary.getCanonicalContentPath(lx, "shaders");
        this.shader = LXStudioApp.instance
            .loadShader(shaderPath + "/monjori.glsl");

        addParameter("graininess", this.graininess);
        addParameter("pace", this.pace);
        addParameter("twist", this.twist);
    }

    @Override
    public void beforeDraw(final PGraphics pg) {
        super.beforeDraw(pg);
        pg.textureMode(PConstants.NORMAL);
        shader.set("time",
            (float) ((System.currentTimeMillis() - startTime) / 1000.0));
        shader.set("resolution", (float) (pg.width), (float) (pg.height));
        shader.set("graininess", (float) (graininess.getValuef()));
        shader.set("pace", (float) (pace.getValuef()));
        shader.set("twist", (float) (twist.getValuef()));
    }
}
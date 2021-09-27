package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.app.media.MediaLibrary;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.CompoundParameter;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * 
 * Stolen from https://github.com/genekogan/Processing-Shader-Examples
 */
public class LPPanelShaderNebula extends LPPanelShaderDirect {
    long startTime;
            
    public final CompoundParameter starspeed = new CompoundParameter("starspeed", 50.0,
        0.0, 100.0).setDescription("Speed of stars")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public LPPanelShaderNebula(LX lx) {
        super(lx);

        startTime = System.currentTimeMillis();

        String shaderPath = MediaLibrary.getCanonicalContentPath(lx, "shaders");
        this.shader = LXStudioApp.instance
            .loadShader(shaderPath + "/nebula.glsl");

        addParameter("starspeed", this.starspeed);
    }

    @Override
    public void beforeDraw(final PGraphics pg) {
        super.beforeDraw(pg);
        pg.textureMode(PConstants.NORMAL);
        shader.set("time", (float) ((System.currentTimeMillis() - startTime) / 1000.0));
        shader.set("resolution", (float) (pg.width), (float) (pg.height));
        shader.set("starspeed", (float) (50));


        shader.set("starspeed", (float) (starspeed.getValuef()));
    }
}
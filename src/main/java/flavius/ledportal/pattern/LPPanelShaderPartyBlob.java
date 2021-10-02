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
public class LPPanelShaderPartyBlob extends LPPanelShaderDirect {
    long startTime;

    public final CompoundParameter depth = new CompoundParameter("depth", 1.0,
        0.0, 2.0).setDescription("Depth of blobbiness")
            .setPolarity(LXParameter.Polarity.BIPOLAR);
            
    public final CompoundParameter rate = new CompoundParameter("rate", 1.0,
        0.0, 2.0).setDescription("Rate of blobbiness (kinda size)")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public LPPanelShaderPartyBlob(LX lx) {
        super(lx);

        startTime = System.currentTimeMillis();

        String shaderPath = MediaLibrary.getCanonicalContentPath(lx, "shaders");
        this.shader = LXStudioApp.instance
            .loadShader(shaderPath + "/party_blob.glsl");

        addParameter("depth", this.depth);
        addParameter("rate", this.rate);
    }

    @Override
    public void beforeDraw(final PGraphics pg) {
        super.beforeDraw(pg);
        pg.textureMode(PConstants.NORMAL);
        shader.set("time",
            (float) ((System.currentTimeMillis() - startTime) / 1000.0));
        shader.set("resolution", (float) (pg.width), (float) (pg.height));
        shader.set("depth", (float) (depth.getValuef()));
        shader.set("rate", (float) (rate.getValuef()));
    }
}
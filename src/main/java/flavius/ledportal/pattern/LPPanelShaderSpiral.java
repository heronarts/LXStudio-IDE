package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.app.media.MediaLibrary;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PConstants;
import processing.core.PGraphics;


public class LPPanelShaderSpiral extends LPPanelShaderDirect {
    long startTime;

    public final CompoundParameter depth = new CompoundParameter("depth", 1.0,
        0.0, 10.0).setDescription("Depth of spirals (number of wraps)")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public final CompoundParameter rate = new CompoundParameter("rate", 1.0,
        0.0, 2.0).setDescription("Rate of movement")
            .setPolarity(LXParameter.Polarity.BIPOLAR);

    public LPPanelShaderSpiral(LX lx) {
        super(lx);

        startTime = System.currentTimeMillis();

        String shaderPath = MediaLibrary.getCanonicalContentPath(lx, "shaders");
        this.shader = LXStudioApp.instance
          .loadShader(shaderPath + "/time_ferrets.glsl");

        addParameter("depth", this.depth);
        addParameter("rate", this.rate);
    }

    @Override
    public void beforeDraw(final PGraphics pg) {
        if (shader == null) return;
        super.beforeDraw(pg);
        pg.textureMode(PConstants.NORMAL);
        float time = (float) ((System.currentTimeMillis() - startTime) / 1000.0);
        shader.set("time", time);
        shader.set("resolution", (float) (pg.width), (float) (pg.height));
        // logger.info(String.format("time %f, res %d x %d", time, pg.width, pg.height));
        shader.set("depth", (float) (depth.getValuef()));
        shader.set("rate", (float) (rate.getValuef()));
    }
}

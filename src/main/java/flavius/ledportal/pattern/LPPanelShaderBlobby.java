package flavius.ledportal.pattern;

import heronarts.lx.LX;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.app.media.MediaLibrary;
import processing.core.PConstants;
import processing.core.PGraphics;

public class LPPanelShaderBlobby extends LPPanelShaderDirect {
    long startTime;

    public LPPanelShaderBlobby(LX lx) {
        super(lx);

        startTime = System.currentTimeMillis();

        String shaderPath = MediaLibrary.getCanonicalContentPath(lx, "shaders");
        this.shader = LXStudioApp.instance
            .loadShader(shaderPath + "/blobby.glsl");
    }

    @Override
    public void beforeDraw(final PGraphics pg) {
        super.beforeDraw(pg);
        pg.textureMode(PConstants.NORMAL);
        long time = (System.currentTimeMillis() - startTime);
        logger.info(String.format("nowMillis, %f", (float) (time / 1000.0)));
        shader.set("time", (float) (time / 1000.0));
        shader.set("resolution", (float) (pg.width), (float) (pg.height));
        shader.set("depth", (float) (1));
        shader.set("rate", (float) (1));
    }
}
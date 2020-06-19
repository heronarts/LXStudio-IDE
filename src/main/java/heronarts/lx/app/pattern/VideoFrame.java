package heronarts.lx.app.pattern;

import flavius.ledportal.LPMeshable;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.app.LXStudioApp;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;
import processing.core.PVector;

import java.util.logging.Logger;


@LXCategory(LXCategory.TEXTURE)
public class VideoFrame extends LXPattern {
    private static final Logger logger = Logger.getLogger(VideoFrame.class.getName());

	public VideoFrame(LX lx){
        super(lx);
	}

    public String getAuthor() {
        return "dev.laserphile.com";
    }

	float u;
	float v;
	int pixelValue;
	boolean firstRun = true;

	public void run(double deltaMs) {
        if (LXStudioApp.videoFrame == null) {
            return;
        }
		for (LXPoint point : model.points ) {
			PVector uiPosition = new PVector(point.x, point.y, point.z);
			PVector worldPosition = LPMeshable.pixelWorldTransform(uiPosition);
			PVector flattenedPosition = LPMeshable.coordinateTransform(LXStudioApp.flattener, worldPosition);
			u = (flattenedPosition.x - LXStudioApp.flatBounds[0][0]) / (LXStudioApp.flatBounds[0][1] - LXStudioApp.flatBounds[0][0]);
			v = (flattenedPosition.y - LXStudioApp.flatBounds[1][0]) / (LXStudioApp.flatBounds[1][1] - LXStudioApp.flatBounds[1][0]);
			pixelValue = LXStudioApp.videoFrame.get((int)(LXStudioApp.videoFrame.width * u), (int)(LXStudioApp.videoFrame.height * v));
			if (firstRun && point.index < 10) {
				logger.fine(String.format(
					"point[%d] at %s -> %s -> %s has u %7.3f , v %7.3f, %7x",
					point.index,
					LPMeshable.formatPVector(uiPosition),
					LPMeshable.formatPVector(worldPosition),
					LPMeshable.formatPVector(flattenedPosition),
					u, v, pixelValue));
			}
			setColor(point.index, pixelValue);
		}
		firstRun = false;
	}
}

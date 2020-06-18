package flavius.ledportal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import processing.data.JSONObject;
import processing.data.JSONArray;
import processing.core.PMatrix3D;
import processing.core.PVector;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;



public class LPSimConfig {
	private static final Logger logger = Logger.getLogger(LPMeshable.class.getName());
    public List<LPPanel> panels = new ArrayList<LPPanel>();
    public List<LPStructure> structures = new ArrayList<LPStructure>();
    public List<LPStructure> debugStructures = new ArrayList<LPStructure>();

    //   public String activeModel = "data/models/dome_render_6_5_LEDs_Iso_1220_Single_ALL_PANELS.json";
    //   public String activeModel = "data/models/dome_render_6_5_LEDs_Iso_1220_ALL_PANELS.json";
    public String activeModel = "data/models/dome_render_6_5_Dome_ALL_PANELS.json";
    public String[] activeStructures =  {
        "data/structures/dome_render_6_5_Dome_EDGES.json",
        "data/structures/dome_render_6_5_Left_Stack_FACES.json"
    };
    public float[] screencapBounds; // = new float[]{ 0, 0, 1, 1 };
    public String activeMovie; // = "data/media/Steamed Hams.mp4";
    public String activeImage; // = "data/media/test_broadcast.jpg";

    public LPSimConfig () {}
    public void updateFromJSONObject(final JSONObject jsonConfig) {
        if(jsonConfig.hasKey("panels")) {
            final JSONArray panelList = jsonConfig.getJSONArray("panels");
            for(int i = 0; i < panelList.size(); i++) {
				final LPPanel panel = new LPPanel();
                this.panels.add(panel.updateFromJSONObject(panelList.getJSONObject(i)));
            }
        }
        if(jsonConfig.hasKey("structures")) {
            final JSONArray structureList = jsonConfig.getJSONArray("structures");
            for(int i = 0; i < structureList.size(); i++) {
				final LPStructure structure = new LPStructure();
                this.structures.add(structure.updateFromJSONObject(structureList.getJSONObject(i)));
            }
        }
	}

	public PVector getWorldCentroid() {
		final List<PVector> centroids = new ArrayList<PVector>();
		for(final LPPanel panel : this.panels) {
			centroids.add(panel.getWorldCentroid());
		}
		return LPMeshable.getCentroid(centroids);
	}

	public PVector getWorldNormal() {
		final List<PVector> normals = new ArrayList<PVector>();
		for(final LPPanel panel : this.panels) {
			normals.add(panel.getWorldNormal());
		}
		return LPMeshable.getCentroid(normals);
	}

	public PMatrix3D getWorldFlattener() {
		final PVector centroid = getWorldCentroid();
		final PVector normal = getWorldNormal();
		final PMatrix3D flattener = LPMeshable.getFlattener(centroid, normal);
		return flattener;
	}

	public PMatrix3D getWorldUnflattener() {
		final PVector centroid = getWorldCentroid();
		final PVector normal = getWorldNormal();
		final PMatrix3D flattener = LPMeshable.getUnflattener(centroid, normal);
		return flattener;
	}

    public LXModel getModel() {
        List<LXPoint> points = new ArrayList<LXPoint>();
        for (LPPanel panel : this.panels) {
            for(PVector worldPixel : panel.getWorldPixels()) {
                points.add(new LXPoint(worldPixel.x, worldPixel.y, worldPixel.z));
            }
        }
        return new LXModel(points);
    }

	/**
	 * Flatten the panels in the model, and determine the bounds in the x and y axes
	 * @return
	 */
	public float[][] getModelBounds() {
		final List<PVector> modelPoints = new ArrayList<PVector>();

		for(final LPPanel panel : panels) {
			for(final PVector vertex: panel.getWorldVertices()) {
				modelPoints.add(vertex);
			}
		}

		logger.info(String.format(
			"points: %s", LPMeshable.formatPVectorList(modelPoints)));

		return LPMeshable.getAxisBounds(modelPoints);
	}

	/**
	 * Flatten the panels in the model, and determine the bounds in the x and y axes
	 * @return
	 */
	public float[][] getModelFlatBounds() {
		final PMatrix3D flattener = getWorldFlattener();
		final List<PVector> modelFlattenedPoints = new ArrayList<PVector>();

		for(final LPPanel panel : panels) {
			for(final PVector vertex: panel.getWorldVertices()) {
				modelFlattenedPoints.add(LPMeshable.coordinateTransform(flattener, vertex));
			}
		}

		logger.info(String.format(
			"flattened points: %s", LPMeshable.formatPVectorList(modelFlattenedPoints)));

		return LPMeshable.getAxisBounds(modelFlattenedPoints);
	}
}

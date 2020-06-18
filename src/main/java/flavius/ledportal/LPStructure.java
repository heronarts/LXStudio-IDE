package flavius.ledportal;

import java.util.List;
import processing.data.JSONObject;
import processing.core.PVector;

public class LPStructure extends LPMeshable {
	public String type;

    public LPStructure updateFromJSONObject(JSONObject jsonConfig) {
		super.updateFromJSONObject(jsonConfig);
		if(jsonConfig.hasKey("type")) {
			this.type = jsonConfig.getString("type");
		}
        return this;
	}

    public LPStructure updateFromPlaneDebugPoints(List<PVector> points) {
		for(PVector point: points) {
			this.vertices.add(point);
			this.edges.add(new int[]{0, this.vertices.size() - 1});
		}
        return this;
	}

	public LPStructure updateFromPolygon(List<PVector> vertices) {
		for(PVector vertex: vertices) {
			this.vertices.add(vertex);
			int size = this.vertices.size();
			if(size >= 2) {
				this.edges.add(new int[]{size - 2, size - 1});
			}
		}
		this.edges.add(new int[]{this.vertices.size() - 1, 0});
        return this;
	}
}

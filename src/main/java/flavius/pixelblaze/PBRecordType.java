package flavius.pixelblaze;

public enum PBRecordType {
	SET_CHANNEL_WS2812(1), DRAW_ALL(2), SET_CHANNEL_APA102_DATA(3), SET_CHANNEL_APA102_CLOCK(4);
	public final int value;
	private PBRecordType(int value) {
		this.value = value;
	}
};

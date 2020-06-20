package flavius.pixelblaze;

import flavius.pixelblaze.util.ByteUtils;

public enum PBColorOrder {
	RGBW(0, 1, 2, 3),
	RGBV(0, 1, 2, -1),
	RGB(0, 1, 2)
	;
	public byte colorOrder;
	public final byte numElements;
	public static final int[] colorIdxLookup = new int[]{2, 1, 0, 3};
	private PBColorOrder(byte colorOrder, int numElements) {
		this.colorOrder = colorOrder;
		this.numElements = (byte) numElements;
	}
	private PBColorOrder(int redi, int greeni, int bluei, int whitei, int numElements) {
		this((byte) 0, numElements);
		int i=0;
		for(int c: new int[]{redi, greeni, bluei, whitei}) {
			this.colorOrder |= (byte)(c & 0b11) << (i++ * 2);
		}
	}
	private PBColorOrder(int redi, int greeni, int bluei, int whitei) {
		this(redi, greeni, bluei, whitei, 4);
	}
	private PBColorOrder(int redi, int greeni, int bluei) {
		this(redi, greeni, bluei, 0, 3);
	}

	public byte[] colorBytes(int color, byte[] glut) {
		byte[] result = new byte[this.numElements];
		for(int colorIdx=0; colorIdx<this.numElements; colorIdx++) {
			int index = (this.colorOrder >> (2 * colorIdx)) & 0b11;
      result[index] = glut[ByteUtils.asUint8(color >> (8 * colorIdxLookup[colorIdx]) & ByteUtils.uint8Max)];
		}
    return result;
	}

	// TODO: write separate colorBytes for APA102
}

package flavius.pixelblaze;

import flavius.pixelblaze.util.ByteUtils;
// import java.util.logging.Logger;

/**
 * PBMessageFactoryWS281X
 */
public class PBMessageFactoryWS281X extends PBMessageFactoryData{
	// private static final Logger logger = Logger.getLogger(PBMessageFactoryWS281X.class.getName());
	public PBMessageFactoryWS281X(final PBColorOrder colorOrder, int channel, int[] indexBuffer) {
		super(PBRecordType.SET_CHANNEL_WS2812, channel, indexBuffer, 4, colorOrder);
	}
	@Override
	protected int writeBody(byte[] message, int offset, int[] indexBuffer, int[] colors) {
		this.validate(indexBuffer);
		int i=offset;
		// logger.fine(String.format(
		// 	"numElements: 0x%02x\ncolorOrders: 0x%02x\n",
		// 	this.colorOrder.numElements,
		// 	this.colorOrder.colorOrder
		// ));
		message[i++] = (byte) this.colorOrder.numElements;
		message[i++] = (byte) this.colorOrder.colorOrder;
		// logger.fine(String.format("pixels: 0x%04x\n", indexBuffer.length));
		for(byte b : ByteUtils.uint16LEBytes(indexBuffer.length)) {
			message[i++] = b;
		}
		for(int colorIdx : indexBuffer) {
			for(byte b : this.colorOrder.colorBytes(colors[colorIdx])) {
				message[i++] = b;
			}
		}
		return (i - offset);
	}
}

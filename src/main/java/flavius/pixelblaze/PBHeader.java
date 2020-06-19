package flavius.pixelblaze;

// import java.util.logging.Logger;

public class PBHeader {
	// private static final Logger logger = Logger.getLogger(PBHeader.class.getName());
	public static final int size = 6;
	public static final String magic = "UPXL";
	public int channel;
	public PBRecordType recordType;
	public PBHeader(final int channel, final PBRecordType recordType) {
		this.channel = channel;
		this.recordType = recordType;
	}
	public byte[] toBytes() {
		final byte[] message = new byte[size];
		int i=0;
		// logger.fine(String.format("magic: %s\n", magic));
		for(char c : magic.toCharArray()) {
			message[i++] = (byte) c;
		}
		// logger.fine(String.format("channel: %d\n", this.channel));
		message[i++] = (byte) this.channel;
		// logger.fine(String.format(
		// 	"recordtype: %d (%s)\n", this.recordType.value, this.recordType.name()));
		message[i++] = (byte) this.recordType.value;

		return message;
	}
}

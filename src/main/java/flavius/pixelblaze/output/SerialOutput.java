package flavius.pixelblaze.output;

import java.util.HashMap;
import processing.serial.Serial;
import processing.core.PApplet;
import heronarts.lx.output.LXOutput;
import heronarts.lx.LX;
// import java.util.logging.Logger;

abstract class SerialOutput extends LXOutput {
	// private static final Logger logger = Logger.getLogger(SerialOutput.class.getName());
	public static HashMap<String, Serial> ports = new HashMap<String, Serial>();
	protected String serialPort;

	public SerialOutput(LX lx, PApplet parent, String serialPort, int baudRate) {
		super(lx, serialPort);
		this.serialPort = serialPort;
		if(!ports.containsKey(serialPort)) ports.put(serialPort, new Serial(parent, serialPort, baudRate));
	}

	public void write(byte[] message) {
		// int i = 0;
		// for (byte b: message) {
		// 	logger.fine(String.format("message[%03d] = 0x%02x\n", i++, b));
		// }
		ports.get(this.serialPort).write(message);
	}
}
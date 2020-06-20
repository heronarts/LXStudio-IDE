package flavius.pixelblaze.util;

/**
 * ByteUtils
 */
public class ByteUtils {
  public static long uint32Max = 0xffffffffL;
  public static long uint16Max = 0xffffL;
  public static long uint8Max = 0xffL;
  public static int uint32Bytes = 4;
  public static int uint16Bytes = 2;
  public static int uint8Bytes = 1;

  public static byte[] uintNLEBytes(int N, long value) {
    byte[] result = new byte[N];
    for (int i = 0; i < N; i++) {
      result[i] = (byte) ((value >> (i * 8)) & uint8Max);
    }
    return result;
  }

  public static byte[] uint16LEBytes(long value) {
    return uintNLEBytes(uint16Bytes, value);
  }

  public static byte[] uint16LEBytes(int value) {
    return uint16LEBytes((long) value);
  }

  public static byte[] uint16LEBytes(byte value) {
    return uint16LEBytes((long) value);
  }

  public static byte[] uint32LEBytes(long value) {
    return uintNLEBytes(uint32Bytes, value);
  }

  public static byte[] uint32LEBytes(int value) {
    return uint32LEBytes((long) value);
  }

  public static byte[] uint32LEBytes(byte value) {
    return uint32LEBytes((long) value);
  }

  public static byte asByte(long value) {
    return (byte) (Math.toIntExact(value & uint8Max));
  }

  public static byte asByte(int value) {
    return asByte((long) value);
  }

  public static byte asByte(byte value) {
    return asByte((long) value & uint8Max);
  }

  public static int asUint8(long value) {
    return (int) (value & uint8Max);
  }

  public static int asUint8(int value) {
    return asUint8((long) value);
  }

  public static int asUint8(byte value) {
    return asUint8((long) value & uint8Max);
  }

  public static long asUint32(long value) {
    return (value & uint32Max);
  }

  public static long asUint32(int value) {
    return asUint32((long) value);
  }

  public static long asUint32(byte value) {
    return asUint32((long) value & uint8Max);
  }
}

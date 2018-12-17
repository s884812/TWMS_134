package tools;

public class BitTools {
	public static final int getShort(final byte array[], final int index) {
		int ret = array[index];
		ret &= 0xFF;
		ret |= ((int) (array[index + 1]) << 8) & 0xFF00;
		return ret;
	}

	public static final String getString(final byte array[], final int index, final int length) {
		char[] cret = new char[length];
		for (int x = 0; x < length; x++) {
			cret[x] = (char) array[x + index];
		}
		return String.valueOf(cret);
	}

	public static final String getMapleString(final byte array[], final int index) {
		final int length = ((int) (array[index]) & 0xFF) | ((int) (array[index + 1] << 8) & 0xFF00);
		return BitTools.getString(array, index + 2, length);
	}

	public static final byte rollLeft(final byte in, final int count) {

		int tmp = (int) in & 0xFF;

		tmp = tmp << (count % 8);
		return (byte) ((tmp & 0xFF) | (tmp >> 8));
	}

	public static final byte rollRight(final byte in, final int count) {

		int tmp = (int) in & 0xFF;
		tmp = (tmp << 8) >>> (count % 8);

		return (byte) ((tmp & 0xFF) | (tmp >>> 8));
	}

	public static final byte[] multiplyBytes(final byte[] in, final int count, final int mul) {
		byte[] ret = new byte[count * mul];
		for (int x = 0; x < count * mul; x++) {
			ret[x] = in[x % count];
		}
		return ret;
	}

	public static final int doubleToShortBits(final double d) {
		long l = Double.doubleToLongBits(d);
		return (int) (l >> 48);
	}
}
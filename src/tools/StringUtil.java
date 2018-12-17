package tools;

public class StringUtil {

	public static final String getLeftPaddedStr(final String in, final char padchar, final int length) {
		StringBuilder builder = new StringBuilder(length);
		for (int x = in.getBytes().length; x < length; x++) {
			builder.append(padchar);
		}
		builder.append(in);
		return builder.toString();
	}

	public static final String getRightPaddedStr(final String in, final char padchar, final int length) {
		StringBuilder builder = new StringBuilder(in);
		for (int x = in.getBytes().length; x < length; x++) {
			builder.append(padchar);
		}
		return builder.toString();
	}

	public static final String joinStringFrom(final String arr[], final int start) {
		return joinStringFrom(arr, start, " ");
	}

	public static final String joinStringFrom(final String arr[], final int start, final String sep) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < arr.length; i++) {
			builder.append(arr[i]);
			if (i != arr.length - 1) {
				builder.append(sep);
			}
		}
		return builder.toString();
	}

	public static final String makeEnumHumanReadable(final String enumName) {
		StringBuilder builder = new StringBuilder(enumName.length() + 1);
		for (String word : enumName.split("_")) {
			if (word.length() <= 2) {
				builder.append(word); // assume that it's an abbrevation
			} else {
				builder.append(word.charAt(0));
				builder.append(word.substring(1).toLowerCase());
			}
			builder.append(' ');
		}
		return builder.substring(0, enumName.length());
	}

	public static final int countCharacters(final String str, final char chr) {
		int ret = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == chr) {
				ret++;
			}
		}
		return ret;
	}
}
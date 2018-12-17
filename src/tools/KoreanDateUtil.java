package tools;

import java.util.Date;
import java.util.SimpleTimeZone;

public class KoreanDateUtil {
	private final static int ITEM_YEAR2000 = -1085019342;
	private final static long REAL_YEAR2000 = 946681229830l;
	private final static int QUEST_UNIXAGE = 27111908;
	private final static long FT_UT_OFFSET = 116444736000000000L; // 100 nsseconds from 1/1/1601 -> 1/1/1970

	public static final long getTempBanTimestamp(final long realTimestamp) {
		// long time = (realTimestamp / 1000);//seconds
		return ((realTimestamp * 10000) + FT_UT_OFFSET);
	}

	public static final int getItemTimestamp(final long realTimestamp) {
		final int time = (int) ((realTimestamp - REAL_YEAR2000) / 1000 / 60); // convert to minutes
		return (int) (time * 35.762787) + ITEM_YEAR2000;
	}

	public static final int getQuestTimestamp(final long realTimestamp) {
		final int time = (int) (realTimestamp / 1000 / 60); // convert to minutes
		return (int) (time * 0.1396987) + QUEST_UNIXAGE;
	}

	public static boolean isDST() {
		return SimpleTimeZone.getDefault().inDaylightTime(new Date());
	}

	public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
		if (isDST()) {
			timeStampinMillis -= 3600000L;
		}
		long time;
		if (roundToMinutes) {
			time = (timeStampinMillis / 1000 / 60) * 600000000;
		} else {
			time = timeStampinMillis * 10000;
		}
		return time + FT_UT_OFFSET;
	}
}
package tools;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtil {
	private CollectionUtil() {
		// Shouldn't use collection util, might wanted to change in furture version
	}

	public static <T> List<T> copyFirst(List<T> list, int count) {
		List<T> ret = new ArrayList<T>(list.size() < count ? list.size() : count);
		int i = 0;
		for (T elem : list) {
			ret.add(elem);
			if (i++ > count) {
				break;
			}
		}
		return ret;
	}
}
package com.verint.textanalytics.dal.darwin.vtasyntax.utils;

import java.util.List;

/**
 * Created by EZlotnik on 3/29/2016.
 */
public final class CollectionUtils {

	private CollectionUtils() {

	}

	/**
	 * Is collection empty.
	 * @param <T>
	 *            type of collection
	 * @param collection
	 *            collection
	 * @return wether is collection has any elements
	 */
	public static <T> Boolean isEmpty(List<T> collection) {
		if (collection == null) {
			return true;
		} else {
			return collection.size() == 0;
		}
	}

	/**
	 * Checks if array is empty.
	 * @param array array to check
	 * @param <T> type of array elements
	 * @return indication wether array is empty
	 */
	public static <T> Boolean isEmpty(T[] array) {
		if (array == null) {
			return true;
		} else {
			return (array.length == 0);
		}
	}
}

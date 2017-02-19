package com.verint.textanalytics.common.utils;

import jersey.repackaged.com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Collection Utils.
 * @author EZlotnik
 *
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

	/**
	 * Partition a list into numerous lists by size.
	 * @param list list to partition
	 * @param size size of each chunk
	 * @param <T> type of list element
	 * @return list of chunks
	 */
	public static <T> List<List<T>> partition(List<T> list, int size) {
		return Lists.partition(list, size);
	}
}
